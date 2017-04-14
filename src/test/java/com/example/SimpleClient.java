package com.example;

import java.io.IOException;
import java.net.Socket;

public class SimpleClient {
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 16990);
            int size = 1;
            for (int i = 0; i < size; ++i) {
                s.getOutputStream().write(("w|user" + i + ":哈哈哈" + i).getBytes());
                s.getOutputStream().flush();
                byte[] resp = new byte[128];
                int len = s.getInputStream().read(resp);
                byte[] dest = new byte[len];
                System.arraycopy(resp, 0, dest, 0, len);
                System.out.println(new String(dest));
            }

            for (int i = 0; i < size; ++i) {
                s.getOutputStream().write(("q|user" + i + "").getBytes());
                s.getOutputStream().flush();
                byte[] resp = new byte[128];
                int len = s.getInputStream().read(resp);
                byte[] dest = new byte[len];
                System.arraycopy(resp, 0, dest, 0, len);
                System.out.println(new String(dest));
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
