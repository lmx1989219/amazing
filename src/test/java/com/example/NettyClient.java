package com.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * Function: Netty Client sync invoker
 *
 * @author limingxin@lifang.com
 * @Date 2016年12月21日 下午2:43:22
 *
 * @see
 */
public class NettyClient {
	public static void main(String[] args) {
		EventLoopGroup workerGroup = new NioEventLoopGroup(4);
		Bootstrap client = new Bootstrap();
		client.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast("decoder", new StringDecoder())
						.addLast("encoder", new StringEncoder(Charset.forName("utf8")))
						.addLast(new ChannelInboundHandlerAdapter() {
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								String re = (String) msg;
								System.out.println(re);
								String[] r_ = re.split(",");
								Long seq = Long.parseLong(r_[0]);
								waiters.get(seq).setResp(seq + "--->" + r_[1]);
								waiters.remove(seq);
							}

							public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
								cause.printStackTrace();
								ctx.close();
							}
						});
			}
		});
		try {
			ExecutorService es = Executors.newFixedThreadPool(4);
			final AtomicLong seq = new AtomicLong(1);
			final Channel ch = client.connect("0.0.0.0", 16990).sync().channel();
			for (;;) {
				if (seq.get() == 2 << 8)
					return;
				// Thread.sleep(1);
				es.submit(new Runnable() {

					@Override
					public void run() {
						long s = seq.getAndIncrement();
						SyncFuture sf = new SyncFuture();
						waiters.put(s, sf);
						// System.out.println("req=" + s + ",hello j");
						ch.writeAndFlush(s + ",hello j");
						try {
							System.out.println("resp=" + sf.get());
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
				});
			}
		} catch (Exception e) {
		} finally {
			workerGroup.shutdownGracefully();
		}
	}

	static Map<Long, SyncFuture> waiters = new ConcurrentHashMap<>();

	static class SyncFuture implements java.util.concurrent.Future<String> {
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
