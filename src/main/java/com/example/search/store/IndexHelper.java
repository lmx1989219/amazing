package com.example.search.store;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 索引存储区
 * Created by lmx on 2017/4/14.
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class IndexHelper extends BaseMedia {
    public static Map<String, DataHelper> kv = new ConcurrentHashMap<>();
    public static Map<String, List<DataHelper>> list = new ConcurrentHashMap<>();

    public IndexHelper(String fileName, int size) throws Exception {
        super(fileName, size);
    }


    public void add(DataHelper dh) throws Exception {
        if (dh == null) return;
        int indexPos = 0;
        if ((indexPos = buffer.getInt()) != 0)
            buffer.position(indexPos);
        else
            buffer.position(4);
        String key = dh.key;
        byte[] keyBytes = key.getBytes("utf8");
        int pos = dh.pos;
        buffer.putInt(keyBytes.length);
        buffer.put(keyBytes);

        String type = dh.type;
        byte[] typeBytes = type.getBytes("utf8");
        buffer.putInt(typeBytes.length);
        buffer.put(typeBytes);

        buffer.putInt(pos);
        buffer.putInt(dh.length);

        int curPos = buffer.position();
        buffer.position(0);
        buffer.putInt(curPos);//head 4 byte in last postion
        buffer.rewind();

        kv.put(key, dh);
        if (dh.getType().equals("list")) {
            if (!list.containsKey(key)) {
                list.put(key, new ArrayList<DataHelper>());
            }
            list.get(key).add(dh);
        }
    }


    public void recoverIndex() throws Exception {
        boolean first = true;
        buffer.position(4);
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

            int typeLength = buffer.getInt();
            byte[] typeBytes = new byte[typeLength];
            buffer.get(typeBytes);
            String type = new String(typeBytes, "utf8");

            int dataIndex = buffer.getInt();
            int dataLength = buffer.getInt();
            DataHelper dh = new DataHelper();
            dh.key = key;
            dh.pos = dataIndex;
            dh.length = dataLength;
            dh.type = type;
            kv.put(key, dh);
            if (dh.getType().equals("list")) {
                if (!list.containsKey(key)) {
                    list.put(key, new ArrayList<DataHelper>());
                }
                list.get(key).add(dh);
            }
        }
        log.info("recover data index size: {}", kv.size());
    }
/*
    public static void main(String args[]) {
        try {
            IndexHelper ih = new IndexHelper("keyIndex");
            DataMedia store = new DataMedia("user");
            ih.recoverIndex();
            for (Map.Entry<String, DataHelper> e : ih.kv.entrySet()) {
                System.out.println(e.getKey() + "" + e.getValue() + "\n data=" + new String(store.get(e.getValue()), "utf8"));
            }
//            ih.clean();
//            store.clean();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}
