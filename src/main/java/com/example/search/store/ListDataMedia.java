package com.example.search.store;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据存储区
 * Created by lmx on 2017/4/13.
 */
public class ListDataMedia extends BaseMedia {

    public ListDataMedia(String fileName, int size) throws Exception {
        super(fileName, size);
    }

    public DataHelper add(ByteBuffer b) throws Exception {
        int pos = buffer.position();
        int k = b.getInt();
        byte[] re = new byte[k];
        b.get(re);
        String s = new String(re, charSet);
        String k_ = s.split(splitter)[0];
        String v = s.split(splitter)[1];
        byte[] v_ = v.getBytes("utf8");
        if (!IndexHelper.kv.containsKey(k_)) {
            buffer.putInt(1);
            //add list availible bytes
            buffer.putInt(v_.length);
            buffer.putInt(v_.length);
            buffer.put(v_);
            DataHelper dh = new DataHelper();
            dh.key = k_;
            dh.pos = pos;
            dh.length = v_.length;
            return dh;
        } else {
            int curPos = IndexHelper.kv.get(k_).getPos();
            buffer.position(curPos);
            int size = buffer.getInt();
            buffer.position(buffer.position() - 4);
            buffer.putInt(size + 1);
            int length = buffer.getInt();
            buffer.position(buffer.position() - 4);
            int len = v_.length + length;
            buffer.putInt(len);
            buffer.position(len);
            buffer.putInt(v_.length);
            buffer.put(v_);
            return null;
        }
    }

    public List<byte[]> get(DataHelper dh) {
        List<byte[]> list = new ArrayList<>();
        buffer.position(dh.pos);
        int size = buffer.getInt();
        int length = buffer.getInt();
        int curLength = 0;
        while (size > 0) {
            if (curLength >= length)
                break;
            if (curLength != 0)
                buffer.position(length);
            int elementLength = buffer.getInt();
            byte[] data = new byte[elementLength];
            buffer.get(data);
            list.add(data);
            curLength += data.length;
        }
        buffer.rewind();
        return list;
    }

    /*public static void main(String args[]) {
        try {
            ListDataMedia store = new ListDataMedia("listData", 1);
            IndexHelper ih = new IndexHelper("keyIndex", 1);
            //ih.recoverIndex();
            for (int i = 0; i < 1 * 100; ++i) {
                ByteBuffer b = ByteBuffer.allocateDirect(128);
                String req = " house" + splitter + "上海虹桥机场T1航站楼，有很多乘客在打酱油";
                int length = req.getBytes().length;
                b.putInt(length);
                b.put(req.getBytes("utf8"));
                b.flip();
                DataHelper dh = store.add(b);
                ih.add(dh);
            }
            for (Map.Entry<String, DataHelper> e : ih.kv.entrySet()) {
                for (byte[] data : store.get(e.getValue())) {
                    System.out.println("\n element=" + new String(data, "utf8"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/

}
