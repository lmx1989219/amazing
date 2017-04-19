package com.example.redis.procol;

import com.fasterxml.jackson.databind.ser.Serializers;

/**
 * Created by lmx on 2017/4/18.
 */
public class ListSetProto extends Serializers.Base {
    String[] value;
    boolean isRight = false;//默认lpush
}
