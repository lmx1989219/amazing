package com.example;

import com.example.messagebus.BusHelper;
import com.example.redis.RedisProtocolAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    SimpleKV sk;
    BusHelper busHelper;
    ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

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
                String command = RedisProtocolAdapter.builder(msg);
                if(command.equals(""))
                    ctx.writeAndFlush("+" + "ok"+"\r\n");
                String op = command.split("#")[0];
                String kv = command.split("#")[1];
                switch (op.toLowerCase()) {
                    case "set":
                        sk.write(kv);
                        ctx.writeAndFlush("+" + "ok"+"\r\n");
                        break;
                    case "get":
                        String v = sk.read(kv.split(":")[0]);
                        int k = v.length();
                        ctx.writeAndFlush("$" + k + "" + "\r\n" + v+"\r\n");
                        break;
                }

                /*char funcode = msg.charAt(0);
                if (funcode == 'w') {
                    String seq = msg.substring(2, msg.lastIndexOf("|"));
                    String req = msg.substring(msg.lastIndexOf("|")+1);
                    sk.write(req);
                    ctx.writeAndFlush(seq + "|ok" + "\n");
                } else if (funcode == 'q') {
                    String seq = msg.substring(2, msg.lastIndexOf("|"));
                    String req = msg.substring(msg.lastIndexOf("|")+1);
                    ctx.writeAndFlush(seq + "|" + sk.read(req) + "\n");
                } else if (funcode == 'p') {// pub
                    String req = msg.substring(2);
                    BusHelper.Message ms = new BusHelper.Message();
                    String[] s = req.split(":");
                    ms.setMsg(s[1]);
                    ms.setTopic(s[0]);
                    busHelper.pubMsg(ms);
                } else if (funcode == 's') {// sub
                    String req = msg.substring(2);
                    String[] s = req.split(":");
                    busHelper.regSubscriber(ctx, s[0]);
                } else
                ctx.writeAndFlush("unkown command");*/
            }
        });
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
