package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;

@Configuration
//@EnableScheduling
@Slf4j
public class TaskJob implements SchedulingConfigurer {
	@Value("${url.db}")
	String url;
	@Autowired
	RedisTemplate<String, String> redis;

	@Bean
	Producer newProducer() {
		return new Producer();
	}

	class Producer {
		@Scheduled(fixedRate = 1000)
		public void producer() {
			log.debug("push request in redis");
			redis.opsForList().rightPush("request:finance", System.currentTimeMillis() + "");
			log.info("cur producer thread = {} ", Thread.currentThread());
		}
	}

	@Bean
	Consumer newConsumer() {
		return new Consumer();
	}

	class Consumer {
		@Scheduled(fixedRate = 5000)
		public void consumer() {
			log.debug("pop request in redis");
			redis.opsForList().leftPop("request:finance");
			log.info("cur consumer thread ={}", Thread.currentThread());
		}
	}

	@PostConstruct
	void start() {
		log.info("redis conn {}", redis.getConnectionFactory().getConnection());
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(Executors.newScheduledThreadPool(2));
	}
}
