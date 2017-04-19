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

/**
 * 基于内存读写key value操作,数据可持久,零延迟
 * Created by lmx on 2017/4/14.
 */
@Component
@Slf4j
public class SimpleList {
    DataMedia store;
    IndexHelper ih;

    @Value("${memorySize:1024}")
    int storeSize;

    @PostConstruct
    public void init() {
        try {
            store = new DataMedia("listData", storeSize);
            ih = new IndexHelper("listIndex", storeSize / 8);
            ih.recoverIndex();
        } catch (Exception e) {
            log.error("init store file error", e);
        }
    }

    public void write(String request) {
        try {
            ByteBuffer b = ByteBuffer.allocateDirect(128);
            int length = request.getBytes().length;
            b.putInt(length);
            b.put(request.getBytes("utf8"));
            b.flip();
            DataHelper dh = store.addList(b);
            ih.add(dh);
        } catch (Exception e) {
            log.error("write list data error", e);
        }
    }

    public List<String> read(String request, int startIdx, int endIdx) {
        try {
            List<String> resp = new ArrayList<>();
            long start = System.currentTimeMillis();
            for (DataHelper l : ih.list.get(request)) {
                resp.add(new String(store.get(l), "utf8"));
            }
            resp = resp.subList(startIdx, endIdx == -1 ? resp.size() : endIdx);
            log.info("key={},value={} cost={}ms", request, resp, (System.currentTimeMillis() - start));
            return resp;
        } catch (Exception e) {
            log.error("read list data error", e);
        }
        return null;
    }
}
