package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
public class DemoApplicationTests {

    @Test
    public void contextLoads() {
    }

    public static class Node {
        int a, b;
        String c, d;
    }

    @SuppressWarnings("null")
    public static void main(String[] args) {
        int k = 0000012;
        System.out.println(k);
        String str = "中华人民";
        try {
            System.out.println(str.getBytes("utf8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
