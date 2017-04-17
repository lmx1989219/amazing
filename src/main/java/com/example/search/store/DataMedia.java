package com.example.search.store;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * 数据存储区
 * Created by lmx on 2017/4/13.
 */
public class DataMedia extends BaseMedia {

    public DataMedia(String fileName, int size) throws Exception {
        super(fileName, size);
    }

    public DataHelper add(ByteBuffer b) throws Exception {
        int pos = 0;
        if ((pos = buffer.getInt()) != 0)
            buffer.position(pos);
        else
            buffer.position(4);
        int k = b.getInt();
        byte[] re = new byte[k];
        b.get(re);
        String s = new String(re, "utf8");
        String k_ = s.split(splitter)[0];
        String v = s.split(splitter)[1];
        byte[] v_ = v.getBytes("utf8");
        buffer.putInt(v_.length);
        buffer.put(v_);
        DataHelper dh = new DataHelper();
        dh.key = k_;
        dh.pos = pos == 0 ? 4 + 4 : pos + 4;
        dh.length = v_.length;
        int curPos = buffer.position();
        buffer.position(0);
        buffer.putInt(curPos);//head 4 byte in last postion
        buffer.rewind();
        return dh;
    }

    public DataHelper addList(ByteBuffer b) throws Exception {
        int k = b.getInt();
        byte[] re = new byte[k];
        b.get(re);
        String s = new String(re, "utf8");
        String k_ = s.split(splitter)[0];
        String v = s.split(splitter)[1];
        byte[] v_ = v.getBytes("utf8");
        int curPost = 0;
        if (IndexHelper.list.containsKey(k_)) {
            int size = IndexHelper.list.get(k_).size();
            DataHelper d = IndexHelper.list.get(k_).get(size - 1);
            curPost = d.getPos() + d.getLength();
        }
        buffer.position(curPost);
        buffer.putInt(v_.length);
        buffer.put(v_);
        DataHelper dh = new DataHelper();
        dh.key = k_;
        dh.pos = curPost + 4;
        dh.length = v_.length;
        dh.type = "list";
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
            DataMedia store = new DataMedia("listData", 1);
            IndexHelper ih = new IndexHelper("keyIndex", 1);
            ih.recoverIndex();
            for (int i = 0; i < 5; ++i) {
                ByteBuffer b = ByteBuffer.allocateDirect(128);
                String req = " house" + splitter + "上海虹桥机场T1航站楼，有很多乘客在打酱油" + i;
                int length = req.getBytes().length;
                b.putInt(length);
                b.put(req.getBytes("utf8"));
                b.flip();
                DataHelper dh = store.addList(b);
                ih.add(dh);
            }
            for (Map.Entry<String, List<DataHelper>> e : ih.list.entrySet()) {
                for (DataHelper l : e.getValue()) {
                    System.out.println(e.getKey() + "\n data=" + new String(store.get(l), "utf8"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
