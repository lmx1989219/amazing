package com.example.redis;

/**
 * Created by Administrator on 2017/4/17.
 */
public class RedisProtocolAdapter {

    /**
     * *3
     * $3
     * SET
     * $1
     * a
     * $1
     * b
     *
     * @param request
     * @return
     */
    public static String builder(String request) {
        String[] req = request.split("\r\n");
        String first = req[0];
        String second = req[1];
        String op = req[2];
        if (req.length > 3) {
            String fourth = req[3];
            String k = req[4];
            if (req.length > 5) {
                String five = req[5];
                String v = req[6];
                return op + "#" + k + ":" + v;
            } else {
                return op + "#" + k;
            }
        } else
            return "";
    }

}
