package com.example;

import com.example.search.store.DataMedia;
import com.example.search.store.IndexHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("recv=" + msg);
        char funcode = msg.charAt(0);
        String req = msg.substring(2);
        if (funcode == 'w') {
            SimpleKV.write(req);
            ctx.writeAndFlush("ok");
        } else if (funcode == 'q') {
            ctx.writeAndFlush(SimpleKV.read(req));
        }
        ctx.writeAndFlush("unkown command");
        //ctx.writeAndFlush(msg.split(",")[0] + ",server 1 say ok");
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
