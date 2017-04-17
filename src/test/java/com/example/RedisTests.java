package com.example;

import com.example.sdk.ClientSdk;
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
    public void testSDK() throws Exception {
        template.opsForValue().set("aa", "b");
        System.out.println("k=aa,v="+template.opsForValue().get("aa"));
    }

}
