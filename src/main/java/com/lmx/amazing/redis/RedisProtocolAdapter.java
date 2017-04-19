package com.lmx.amazing.redis;

import java.util.List;

/**
 * Created by Administrator on 2017/4/17.
 */
public class RedisProtocolAdapter {
    static public final String QUIT = "*1\r\n$4\r\nQUIT";
    static public final String CONNECT_OK = "*1\r\n$7\r\nCOMMAND";
    static public final String OK = "+ok\r\n";
    static public final String FAIL = "-Error unsupport command\r\n";
    static public final String LIST_OK = ":1\r\n";

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
    public static String builderReq(String request) {
        String[] req = request.split("\r\n");
        String first = req[0];
        String second = req[1];
        String op = req[2];
        if (req.length > 3) {
            String fourth = req[3];
            String k = req[4];
            if (req.length > 5) {
                if (req.length > 7) {
                    String args1 = req[6];
                    String args2 = req[8];
                    return op + "#" + k + ":" + args1 + ":" + args2;
                }
                String v = req[6];
                return op + "#" + k + ":" + v;
            } else {
                return op + "#" + k;
            }
        } else
            return "";
    }


    /**
     * "$1\r\na\r\n"
     * single resp
     *
     * @param str
     * @return
     */
    public static String builderResp(String str) {
        int len = str.length();
        return "$" + len + "\r\n" + str + "\r\n";
    }

    /**
     * "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n"
     * multi resp
     *
     * @param strs
     * @return
     */
    public static String builderResp(List<String> strs) {
        int len = strs.size();
        if (len == 0)
            return "*0\r\n";
        StringBuffer b = new StringBuffer("*");
        b.append(len);
        b.append("\r\n");
        for (String s : strs) {
            b.append("$");
            b.append(s.length());
            b.append("\r\n");
            b.append(s);
            b.append("\r\n");
        }
        return b.toString();
    }

}
