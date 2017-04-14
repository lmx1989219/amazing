package com.example;

import com.example.search.store.DataMedia;
import com.example.search.store.IndexHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;

/**
 * 基于内存读写操作,数据可持久,零延迟
 * Created by lmx on 2017/4/14.
 */
@Component
public class SimpleKV {
    DataMedia store;
    IndexHelper ih;

    @Value("${memorySize:1024}")
    int storeSize;

    @PostConstruct
    public void init() {
        try {
            store = new DataMedia("valueData", storeSize);
            ih = new IndexHelper("keyIndex", storeSize / 8);
            ih.recoverIndex();
        } catch (Exception e) {
        }
    }

    public void write(String request) {
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

    public String read(String request) {
        try {
            String resp = new String(store.get(ih.getKv().get(request)), "utf8");
            System.out.println(resp);
            return resp;
        } catch (Exception e) {
        }
        return null;
    }
}
