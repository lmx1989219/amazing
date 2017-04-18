package com.example.redis.procol;

/**
 * Created by lmx on 2017/4/18.
 */
public abstract class BaseProto {
    public static enum OP {
        SET, GET, HSET, HGET, LPUSH, LRANGE
    }

    OP op;
}
