package com.example.search.store;

import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * 数据存储区
 * Created by lmx on 2017/4/13.
 */
public class DataMedia extends BaseMedia {

    public DataMedia(String fileName) throws Exception {
        super(fileName);
    }

    @Data
    static public class DataHelper {
        String key;
        int pos;
        int length;
    }

    public DataHelper add(ByteBuffer b) throws Exception {
        int pos = buffer.position();
        int k = b.getInt();
        byte[] re = new byte[k];
        b.get(re);
        String s = new String(re, "utf8");
        b.rewind();
        buffer.put(b);
        DataHelper dh = new DataHelper();
        String str = s.split(splitter)[0];
        dh.key = str;
        dh.pos = pos + 4;
        dh.length = re.length;
        return dh;
    }

    public byte[] get(DataHelper dh) {
        buffer.position(dh.pos);
        byte[] data = new byte[dh.length];
        buffer.get(data);
        buffer.rewind();
        return data;
    }

    static String splitter = ":";

    public static void main(String args[]) {
        try {
            DataMedia store = new DataMedia("user");
            IndexHelper ih = new IndexHelper("keyIndex");
            for (int i = 0; i < 1000 * 100; ++i) {
                ByteBuffer b = ByteBuffer.allocateDirect(128);
                String req = i + " house" + splitter + "上海虹桥机场T1航站楼，有很多乘客在打酱油";
                int length = req.getBytes().length;
                b.putInt(length);
                b.put(req.getBytes("utf8"));
                b.flip();
                DataHelper dh = store.add(b);
                ih.add(dh);
            }
            for (Map.Entry<String, DataMedia.DataHelper> e : ih.kv.entrySet()) {
                System.out.println(e.getKey() + "" + e.getValue() + "\n data=" + new String(store.get(e.getValue()), "utf8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
