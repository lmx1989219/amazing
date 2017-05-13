package com.example;

import com.example.messagebus.BusHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.File;
import java.nio.charset.Charset;

@Component
@Order(value = 1)
public class NettyServer implements ApplicationContextAware {

    static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    Channel channel;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    @Value("${rpcServer.host:0.0.0.0}")
    String host;

    @Value("${rpcServer.ioThreadNum:4}")
    int ioThreadNum;

    @Value("${rpcServer.backlog:1024}")
    int backlog;

    @Value("${rpcServer.port:16990}")
    int port;
    @Autowired
    SimpleKV simpleKV;
    @Autowired
    BusHelper busHelper;

    @PostConstruct
    public void start() throws Exception {
        logger.info("begin to start rpc server");
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(ioThreadNum);
        final SSLEngine engine = ContextSSLFactory.getSslContext().createSSLEngine();
        engine.setUseClientMode(false) ;
        //false为单向认证，true为双向认证
        engine.setNeedClientAuth(true) ;
//        final SslContext sslCtx = SslContextBuilder.forServer(new File("sChat.cer"), new File("sChat.jks")).build();
//        SelfSignedCertificate ssc = new SelfSignedCertificate();
//        final SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
//                .build();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, backlog)
                .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(/*sslCtx.newHandler(socketChannel.alloc())*/new SslHandler(engine)).addLast(new LineBasedFrameDecoder(1024)).addLast("decoder", new StringDecoder())
                                .addLast("encoder", new StringEncoder(Charset.forName("utf8"))).addLast(new NettyServerHandler(simpleKV, busHelper));
                    }
                });

        serverBootstrap.bind(host, port).channel();

        logger.info("NettyRPC server listening on port {}", port);
    }

    @PreDestroy
    public void stop() {
        logger.info("destroy server resources");
        if (null == channel) {
            logger.error("server channel is null");
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
        bossGroup = null;
        workerGroup = null;
        channel = null;
    }

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    }
}
