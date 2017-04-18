package com.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * java系统
 *  日志级别error
 *  处理1000*7次读写操作（覆盖kv list hash） 耗时3072ms 3100ms
 *  读4000*3次耗时19s
 * redis系统
 *  处理1000*7次读写操作（覆盖kv list hash） 耗时3122ms 2930ms
 *  读4000*3次耗时19s
 *
 *  性能比拼接近于等价，争取超过redis！fighting！！
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
@Slf4j
public class RedisTests {

    @Autowired
    RedisTemplate<String, String> template;

    @Test
    public void testR() throws Exception {
        for (int i = 0; i < 4000; ++i) {
            //template.opsForValue().set("aa" + i, "b" + i);
            log.debug("k=aa" + i + ",v=" + template.opsForValue().get("aa" + i));
            //template.opsForList().leftPush("list", "1");
            log.debug("list=" + template.opsForList().range("list", 0, -1));
            //template.opsForHash().put("user200" + i, "age", "25");
            //template.opsForHash().put("user200" + i, "sex", "男");
            log.debug("hv=" + template.opsForHash().get("user200" + i, "age"));
        }
    }

}
