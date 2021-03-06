package com.example;

import com.example.search.store.DataHelper;
import com.example.search.store.DataMedia;
import com.example.search.store.IndexHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;

/**
 * 基于内存读写操作,数据可持久,零延迟
 * Created by lmx on 2017/4/14.
 */
@Component
@Slf4j
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
            log.error("init store file error", e);
        }
    }

    public synchronized void write(String request) {
        try {
            ByteBuffer b = ByteBuffer.allocateDirect(128);
            int length = request.getBytes().length;
            b.putInt(length);
            b.put(request.getBytes("utf8"));
            b.flip();
            DataHelper dh = store.add(b);
            ih.add(dh);
        } catch (Exception e) {
            log.error("write data error", e);
        }
    }

    public String read(String request) {
        try {
            long start = System.currentTimeMillis();
            String resp = new String(store.get(ih.kv.get(request)), "utf8");
            log.info("key={},value={} cost={}ms", request, resp, (System.currentTimeMillis() - start));
            return resp;
        } catch (Exception e) {
            log.error("read data error", e);
        }
        return null;
    }
}
