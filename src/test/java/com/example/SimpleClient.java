package com.example;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟redis key value操作<br>
 * protocol 纯文本<br>
 * write format[w|key:value]<br>
 * read format [w|key]
 */
public class SimpleClient {
    static int cucurrencyNum = 20;
    static BlockingQueue<Socket> pools = new ArrayBlockingQueue<Socket>(cucurrencyNum);

    static {
        for (int i = 0; i < cucurrencyNum; ++i) {
            try {
                pools.add(new Socket("127.0.0.1", 16980));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(32);

        int size = cucurrencyNum;
        /*final AtomicInteger ac = new AtomicInteger(0);
        for (int i = 0; i < size; ++i) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        int i = ac.incrementAndGet();
                        Socket s = pools.poll();
                        s.getOutputStream().write(("w|user" + i + ":哈哈哈2=" + i + "\n").getBytes());
                        s.getOutputStream().flush();
                        byte[] resp = new byte[128];
                        int len = s.getInputStream().read(resp);
                        byte[] dest = new byte[len];
                        System.arraycopy(resp, 0, dest, 0, len);
                        System.out.println(new String(dest));
                        pools.add(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }*/
        final AtomicInteger ac_ = new AtomicInteger(0);
        for (int i = 0; i < size; ++i)
            es.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket s = pools.poll();
                        int i = ac_.incrementAndGet();
                        long start = System.currentTimeMillis();
                        s.getOutputStream().write(("q|user" + i + "\n").getBytes());
                        s.getOutputStream().flush();
                        byte[] resp = new byte[128];
                        int len = s.getInputStream().read(resp);
                        System.out.println("cost time=" + (System.currentTimeMillis() - start) + "ms");
                        byte[] dest = new byte[len];
                        System.arraycopy(resp, 0, dest, 0, len);
                        System.out.println(new String(dest));
                        //pools.offer(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        es.shutdown();
    }
}
