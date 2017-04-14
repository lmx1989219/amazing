package com.example;

import com.example.search.store.DataMedia;
import com.example.search.store.IndexHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    SimpleKV sk;

    public NettyServerHandler(SimpleKV simpleKV) {
        this.sk = simpleKV;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("recv msg ={}", msg);
        char funcode = msg.charAt(0);
        String req = msg.substring(2);
        if (funcode == 'w') {
            sk.write(req);
            ctx.writeAndFlush("ok");
        } else if (funcode == 'q') {
            ctx.writeAndFlush(sk.read(req));
        } else
            ctx.writeAndFlush("unkown command");
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
