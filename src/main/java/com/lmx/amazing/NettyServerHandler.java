package com.lmx.amazing;

import com.lmx.amazing.messagebus.BusHelper;
import com.lmx.amazing.redis.RedisProtocolAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    SimpleKV sk;
    SimpleList sl;
    SimpleHash sh;
    BusHelper busHelper;

    public NettyServerHandler(SimpleKV simpleKV, BusHelper busHelper, SimpleList sl, SimpleHash sh) {
        this.sk = simpleKV;
        this.sl = sl;
        this.sh = sh;
        this.busHelper = busHelper;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
        log.info("recv msg {}", msg);
        if (msg.equals(RedisProtocolAdapter.QUIT)) {
            ctx.writeAndFlush(RedisProtocolAdapter.OK);
            return;
        }
        String command = RedisProtocolAdapter.builderReq(msg);
        if (command.equals("")) {
            ctx.writeAndFlush(RedisProtocolAdapter.FAIL);
            return;
        }
        if (command.equals(RedisProtocolAdapter.CONNECT_OK)) {
            ctx.writeAndFlush(RedisProtocolAdapter.OK);
            return;
        }
        String op = command.split("#")[0];
        String kv = command.split("#")[1];
        switch (op.toLowerCase()) {
            case "set":
                sk.write(kv);
                ctx.writeAndFlush(RedisProtocolAdapter.OK);
                break;
            case "get":
                String v = sk.read(kv.split(":")[0]);
                ctx.writeAndFlush(RedisProtocolAdapter.builderResp(v));
                break;
            case "lpush":
                sl.write(kv);
                ctx.writeAndFlush(RedisProtocolAdapter.LIST_OK);
                break;
            case "lrange":
                String[] args = kv.split(":");
                List<String> listV = sl.read(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                ctx.writeAndFlush(RedisProtocolAdapter.builderResp(listV));
                break;
            case "hset":
                String[] args_ = kv.split(":");
                sh.write(args_[0], args_[1] + ":" + args_[2]);
                ctx.writeAndFlush(RedisProtocolAdapter.LIST_OK);
                break;
            case "hget":
                String[] hargs_ = kv.split(":");
                String listV_ = sh.read(hargs_[0], hargs_[1]);
                ctx.writeAndFlush(RedisProtocolAdapter.builderResp(listV_));
                break;
            default:
                ctx.writeAndFlush(RedisProtocolAdapter.FAIL);
                break;
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
        //ctx.close();
    }

}
