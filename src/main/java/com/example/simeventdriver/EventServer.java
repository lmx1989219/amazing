/*
package com.example.simeventdriver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventServer {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(16990);
        ExecutorService es = Executors.newFixedThreadPool(8);
        while (true) {
            final Socket s = ss.accept();
            es.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream in = s.getInputStream();
                        byte[] re = new byte[256];
                        int len = in.read(re);
                        if (len == -1)
                            return;
                        byte[] re_ = new byte[len];
                        System.arraycopy(re, 0, re_, 0, len);
                        System.out.println("recv=" + new String(re_));
                        OutputStream os = s.getOutputStream();
                        os.write("hello:dwt,this is` server 2".getBytes());
                        os.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }


    }
}
*/
