package com.lmx.amazing.messagebus;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.netty4.BulkReply;
import redis.netty4.MultiBulkReply;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    Map<String, List<ChannelHandlerContext>> subscribers = new ConcurrentHashMap<>();
    BlockingQueue<Message> messages = new LinkedBlockingQueue<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static public class Message {
        String topic, msg;
    }

    public void regSubscriber(ChannelHandlerContext channel, String topic) {
        if (!subscribers.containsKey(topic)) {
            subscribers.put(topic, new ArrayList<ChannelHandlerContext>());
        }
        subscribers.get(topic).add(channel);
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
                    String topic = me.getTopic();
                    for (ChannelHandlerContext ch : subscribers.get(topic)) {
                        log.info("notify channel: {} ,msg: {}", ch.toString(), me.getMsg());
                        ch.channel().write(new BulkReply(me.getMsg().getBytes()));
                        ch.channel().flush();
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
