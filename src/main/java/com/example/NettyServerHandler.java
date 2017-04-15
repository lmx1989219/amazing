package com.example;

import com.example.messagebus.BusHelper;
import com.example.search.store.DataMedia;
import com.example.search.store.IndexHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
	SimpleKV sk;
	BusHelper busHelper;
	ExecutorService es = Executors.newFixedThreadPool(4 * 2);

	public NettyServerHandler(SimpleKV simpleKV, BusHelper busHelper) {
		this.sk = simpleKV;
		this.busHelper = busHelper;
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
		es.submit(new Runnable() {
			@Override
			public void run() {
				log.info("recv msg {}", msg);
				char funcode = msg.charAt(0);
				String req = msg.substring(2);
				if (funcode == 'w') {
					sk.write(req);
					ctx.writeAndFlush("ok");
				} else if (funcode == 'q') {
					ctx.writeAndFlush(sk.read(req) + "\n");
				} else if (funcode == 'p') {// pub
					BusHelper.Message ms = new BusHelper.Message();
					String[] s = req.split(":");
					ms.setMsg(s[1]);
					ms.setTopic(s[0]);
					busHelper.pubMsg(ms);
				} else if (funcode == 's') {// sub
					String[] s = req.split(":");
					busHelper.regSubscriber(ctx, s[0]);
				} else
					ctx.writeAndFlush("unkown command");
			}
		});
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}
