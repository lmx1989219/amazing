package com.lmx.amazing.redis.procol;

/**
 * Created by lmx on 2017/4/18.
 */
public abstract class BaseProto {
    public static enum OP {
        SET, GET, HSET, HGET, LPUSH, LRANGE
    }

    OP op;
    String key;
    boolean hasArgs = false;
}
