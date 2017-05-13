package com.example;

import com.example.messagebus.BusHelper;
import com.example.sdk.SubcriberHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLEngine;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

//@Component
@Slf4j
@Order(value = 2)
public class ClientSdk {
    //    @Autowired(required = false)
    SubcriberHandler subcriberHandler;

    public void setSubcriberHandler(SubcriberHandler subcriberHandler) {
        this.subcriberHandler = subcriberHandler;
    }

    //    @Value("${host:0.0.0.0}")
    String host = "0.0.0.0";
    //    @Value("${port:16379}")
    int port = 16379;
    EventLoopGroup workerGroup = new NioEventLoopGroup(8);
    Bootstrap client = new Bootstrap();
    Channel channel;
    AtomicLong seq = new AtomicLong(0);

    public void shutDown() {
        channel.close();
        workerGroup.shutdownGracefully();

    }

    //@PostConstruct
    public void init() throws Exception {
        final SSLEngine engine = ContextSSLFactory.getSslContext2().createSSLEngine();
        engine.setUseClientMode(true);
//        final SslContext sslCtx = SslContextBuilder.forClient().keyManager(new File("cChat.cer"), new File("cChat.jks")).build();
//        final SslContext sslCtx = SslContextBuilder.forClient()
//                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        client.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(/*sslCtx.newHandler(socketChannel.alloc(), host, port)*/new SslHandler(engine)).addLast(new LineBasedFrameDecoder(1024))
                        .addLast("decoder", new StringDecoder())
                        .addLast("encoder", new StringEncoder(Charset.forName("utf8")))
                        .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                String re = (String) msg;
                                if (re.startsWith("broadcast") && subcriberHandler != null) {
                                    subcriberHandler.onMessage(re);
                                } else {
                                    System.out.println(msg);
                                    String[] r_ = re.split("\\|");
                                    Long seq = Long.parseLong(r_[0]);
                                    waiters.get(seq).setResp(seq + "--->" + r_[1]);
                                    waiters.remove(seq);
                                }
                            }

                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
            }
        });
        ChannelFuture future = client.connect(host, port).sync();
        channel = future.channel();
    }

    public String set(String k, String v) {
        long sequence = seq.getAndIncrement();
        SyncFuture sf = new SyncFuture();
        waiters.put(sequence, sf);
        long start = System.currentTimeMillis();
        //格式：w|seq|key:value
        channel.writeAndFlush("w|" + sequence + "| " + k + ":" + v + "\n");
        try {
            String resp = sf.get();
            System.out.println(
                    "resp=" + resp + " cost=" + (System.currentTimeMillis() - start) + "ms");
            return resp;
        } catch (Exception e) {
            log.error("set error", e);
        }
        return null;
    }

    public String get(String k) {
        long sequence = seq.getAndIncrement();
        SyncFuture sf = new SyncFuture();
        waiters.put(sequence, sf);
        long start = System.currentTimeMillis();
        //格式：q|seq|key
        channel.writeAndFlush("q|" + sequence + "| " + k + "\n");
        try {
            String resp = sf.get();
            System.out.println(
                    "resp=" + resp + " cost=" + (System.currentTimeMillis() - start) + "ms");
            return resp;
        } catch (Exception e) {
            log.error("get error", e);
        }
        return null;
    }

    public void registSub(String topic) {
        channel.writeAndFlush("s|" + topic + "\n");
    }

    public void pub(BusHelper.Message message) {
        channel.writeAndFlush("p|" + message.getTopic() + ":" + message.getMsg() + "\n");
    }

    static Map<Long, SyncFuture> waiters = new ConcurrentHashMap<>();

    static class SyncFuture implements Future<String> {
        CountDownLatch cd = new CountDownLatch(1);
        String resp;
        long seq;

        public void setResp(String resp) {
            this.resp = resp;
            cd.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
            cd.await();
            return this.resp;
        }

        @Override
        public String get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            if (cd.await(timeout, unit))
                return this.resp;
            else
                return null;
        }
    }
}
