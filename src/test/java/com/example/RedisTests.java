package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
public class RedisTests {

    @Autowired
    RedisTemplate<String, String> template;

    @Test
    public void testR() throws Exception {
        for (int i = 0; i < 1000; ++i) {
            //template.opsForValue().set("aa" + i, "b" + i);
            System.out.println("k=aa" + i + ",v=" + template.opsForValue().get("aa" + i));
            //template.opsForList().leftPush("list", "1");
            //System.out.println("list=" + template.opsForList().range("list", 0, -1));
            //template.opsForHash().put("user200" + i, "age", "25");
            //template.opsForHash().put("user200" + i, "sex", "男");
            //System.out.println("hv=" + template.opsForHash().get("user200" + i, "age"));
        }
    }

}
