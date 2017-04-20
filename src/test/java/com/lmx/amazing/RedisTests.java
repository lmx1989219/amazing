package com.lmx.amazing;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * java系统<br>
 * &nbsp;日志级别error
 * &nbsp;处理1000*7次读写操作（覆盖kv list hash） 1900ms 1949ms
 * &nbsp;读2000*3次耗时1945ms<br>
 * redis系统<br>
 * &nbsp;处理1000*7次读写操作（覆盖kv list hash） 耗时1266ms 1675ms
 * &nbsp;读2000*3次耗时2200ms<br>
 * 性能比拼接近于等价，争取超过redis！fighting！！
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MainApplication.class)
@Slf4j
public class RedisTests {

    @Autowired
    RedisTemplate<String, String> template;

    @Test
    public void testR() throws Exception {
        for (int i = 10000; i < 12000; ++i) {
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
