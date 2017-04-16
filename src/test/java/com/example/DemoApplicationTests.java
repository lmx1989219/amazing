package com.example;

import com.example.messagebus.BusHelper;
import com.example.sdk.ClientSdk;
import com.example.sdk.SubcriberHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
public class DemoApplicationTests {

    @Autowired
    ClientSdk clientSdk;

    @Test
    public void testSDK() throws  Exception{
        clientSdk.setSubcriberHandler(new SubcriberHandler() {
            @Override
            public void onMessage(String msg) {
                System.out.println("on message :"+msg);
            }
        });
        clientSdk.set("1001", "跨境结算洒洒的");
        clientSdk.get("1001");
        clientSdk.registSub("amazing");
        Thread.sleep(1000);
        clientSdk.pub(BusHelper.Message.builder().topic("amazing").msg("来看看看看叫姐姐是").build());
    }

}
