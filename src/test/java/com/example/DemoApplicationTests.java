package com.example;

import com.example.messagebus.BusHelper;
import com.example.sdk.SubcriberHandler;
import org.junit.Test;

public class DemoApplicationTests {


    @Test
    public void testSDK() {
        ClientSdk clientSdk = new ClientSdk();
        try {

            clientSdk.init();
            clientSdk.setSubcriberHandler(new SubcriberHandler() {
                @Override
                public void onMessage(String msg) {
                    System.out.println("on message :" + msg);
                }
            });
            clientSdk.set("1001", "张无忌");
            clientSdk.get("1001");
            clientSdk.registSub("amazing");
            clientSdk.pub(BusHelper.Message.builder().topic("amazing").msg("hello world").build());
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientSdk.shutDown();
        }

    }

}
