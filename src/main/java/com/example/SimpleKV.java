package com.example;

import com.example.search.store.DataMedia;
import com.example.search.store.IndexHelper;

import java.nio.ByteBuffer;

/**
 * Created by lmx on 2017/4/14.
 */
public class SimpleKV {
    static DataMedia store;
    static IndexHelper ih;

    static {
        try {
            store = new DataMedia("valueData");
            ih = new IndexHelper("keyIndex");
            ih.recoverIndex();
        } catch (Exception e) {
        }
    }

    public static void write(String request) {
        try {
            ByteBuffer b = ByteBuffer.allocateDirect(128);
            int length = request.getBytes().length;
            b.putInt(length);
            b.put(request.getBytes("utf8"));
            b.flip();
            DataMedia.DataHelper dh = store.add(b);
            ih.add(dh);
        } catch (Exception e) {
        }
    }

    public static String read(String request) {
        try {
            String resp = new String(store.get(ih.getKv().get(request)), "utf8");
            System.out.println(resp);
            return resp;
        } catch (Exception e) {
        }
        return null;
    }
}
