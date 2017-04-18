package com.example;

import com.example.search.store.DataHelper;
import com.example.search.store.DataMedia;
import com.example.search.store.IndexHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于内存读写key value操作,数据可持久,零延迟
 * Created by lmx on 2017/4/14.
 */
@Component
@Slf4j
public class SimpleHash {
    DataMedia store;
    IndexHelper ih;

    @Value("${memorySize:1024}")
    int storeSize;

    @PostConstruct
    public void init() {
        try {
            store = new DataMedia("hashData", storeSize);
            ih = new IndexHelper("hashIndex", storeSize / 8);
            ih.recoverIndex();
        } catch (Exception e) {
            log.error("init store file error", e);
        }
    }

    public synchronized void write(String hash, String request) {
        try {
            ByteBuffer b = ByteBuffer.allocateDirect(128);
            int hashL = hash.getBytes().length;
            b.putInt(hashL);
            b.put(hash.getBytes("utf8"));

            int length = request.getBytes().length;
            b.putInt(length);
            b.put(request.getBytes("utf8"));
            b.flip();
            DataHelper dh = store.addHash(b);
            ih.add(dh);
        } catch (Exception e) {
            log.error("write list data error", e);
        }
    }

    public String read(String hash, String field) {
        try {
            List<String> resp = new ArrayList<>();
            long start = System.currentTimeMillis();
            for (Map.Entry<String, DataHelper> e : ih.hash.get(hash).entrySet()) {
                if (e.getKey().equals(field))
                    return new String(store.get(e.getValue()), "utf8");
            }
            log.info("key={},value={} cost={}ms", field, resp, (System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.error("read list data error", e);
        }
        return null;
    }
}
