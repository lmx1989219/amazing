package com.example.sdk;

import com.example.messagebus.BusHelper;

/**
 * Created by Administrator on 2017/4/16.
 */
public interface SubcriberHandler {

    public void onMessage(String msg);
}
