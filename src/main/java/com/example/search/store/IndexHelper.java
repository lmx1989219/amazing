package com.example.search.store;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 索引存储区
 * Created by lmx on 2017/4/14.
 */
@Data
public class IndexHelper extends BaseMedia {
    Map<String, DataMedia.DataHelper> kv = new ConcurrentHashMap<>();

    public IndexHelper(String fileName, int size) throws Exception {
        super(fileName, size);
    }


    public void add(DataMedia.DataHelper dh) throws Exception {
        buffer.position();
        String key = dh.key;
        byte[] keyBytes = key.getBytes("utf8");
        int pos = dh.pos;
        buffer.putInt(keyBytes.length);
        buffer.put(keyBytes);
        buffer.putInt(pos);
        buffer.putInt(dh.length);
        kv.put(key, dh);
    }


    public void recoverIndex() throws Exception {
        boolean first = true;
        while (buffer.hasRemaining()) {
            int keyLength = buffer.getInt();
            if (first && keyLength <= 0) {
                first = false;
                buffer.rewind();
                break;
            }
            if (keyLength <= 0)
                break;
            byte[] keyBytes = new byte[keyLength];
            buffer.get(keyBytes);
            String key = new String(keyBytes, "utf8");
            int dataIndex = buffer.getInt();
            int dataLength = buffer.getInt();
            DataMedia.DataHelper dh = new DataMedia.DataHelper();
            dh.key = key;
            dh.pos = dataIndex;
            dh.length = dataLength;
            kv.put(key, dh);
        }
    }
/*
    public static void main(String args[]) {
        try {
            IndexHelper ih = new IndexHelper("keyIndex");
            DataMedia store = new DataMedia("user");
            ih.recoverIndex();
            for (Map.Entry<String, DataMedia.DataHelper> e : ih.kv.entrySet()) {
                System.out.println(e.getKey() + "" + e.getValue() + "\n data=" + new String(store.get(e.getValue()), "utf8"));
            }
//            ih.clean();
//            store.clean();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}
