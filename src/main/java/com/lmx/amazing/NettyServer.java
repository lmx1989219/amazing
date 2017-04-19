package com.lmx.amazing;

import com.lmx.amazing.messagebus.BusHelper;
import com.lmx.amazing.redis.RedisCommandDecoder;
import com.lmx.amazing.redis.RedisReplyEncoder;
import com.lmx.amazing.redis.RedisServer;
import com.lmx.amazing.redis.SimpleRedisServer;
import com.lmx.amazing.redis.datastruct.SimpleHash;
import com.lmx.amazing.redis.datastruct.SimpleKV;
import com.lmx.amazing.redis.datastruct.SimpleList;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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

@Component
@Order(value = 1)
public class NettyServer implements ApplicationContextAware {

    static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    Channel channel;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    @Value("${rpcServer.host:0.0.0.0}")
    String host;

    @Value("${rpcServer.ioThreadNum:1}")
    int ioThreadNum;

    @Value("${rpcServer.backlog:1024}")
    int backlog;

    @Value("${rpcServer.port:16990}")
    int port;
    @Autowired
    SimpleKV simpleKV;
    @Autowired
    BusHelper busHelper;
    @Autowired
    SimpleList sl;
    @Autowired
    SimpleHash sh;

    @PostConstruct
    public void start() throws InterruptedException {
        logger.info("begin to start rpc server..");
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(ioThreadNum);
        final RedisServer redis = new SimpleRedisServer();
        final NettyServerHandler commandHandler = new NettyServerHandler(redis);
        redis.initStore(simpleKV, sl, sh);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .localAddress(port)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new RedisCommandDecoder());
                        p.addLast(new RedisReplyEncoder());
                        p.addLast(commandHandler);
                    }
                });

        channel = serverBootstrap.bind(host, port).sync().channel();

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
