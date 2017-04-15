package com.example.messagebus;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2017/4/15.
 */
@Slf4j
@Component
public class BusHelper {
    Map<String, ChannelHandlerContext> subscriber = new ConcurrentHashMap<>();
    BlockingQueue<Message> messages = new LinkedBlockingQueue<>();

    @Data
    static public class Message {
        String topic, msg;
    }

    public void regSubscriber(ChannelHandlerContext channel, String topic) {
        subscriber.put(channel.toString() + "#" + topic
                , channel);
        log.info("register subscriber {}", channel.channel().toString());
    }

    public void pubMsg(Message m) {
        log.info("pub a event {}", m.toString());
        messages.add(m);
    }

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Message me = messages.take();
                    for (Map.Entry<String, ChannelHandlerContext> channel : subscriber.entrySet()) {
                        String k = channel.getKey();
                        if ((me.getTopic()).equals(k.split("#")[2])) {
                            log.info("notify channel {}", channel.getValue().toString());
                            channel.getValue().writeAndFlush(me.toString());
                        } else {
                            log.info("no found subscriber");
                        }
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    });

    @PostConstruct
    public void init() {
        t.start();
    }
}
