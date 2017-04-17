package com.example;

import com.example.messagebus.BusHelper;
import com.example.sdk.ClientSdk;
import com.example.sdk.SubcriberHandler;
import org.junit.Test;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = DemoApplication.class)
public class DemoApplicationTests {

//    @Autowired
    ClientSdk clientSdk = new ClientSdk("47.88.35.216",16980);

    @Test
    public void testSDK() throws  Exception{
        clientSdk.init();
        clientSdk.setSubcriberHandler(new SubcriberHandler() {
            @Override
            public void onMessage(String msg) {
                System.out.println("on message :"+msg);
            }
        });
        clientSdk.set("1002", "跨境结算洒洒的");
        clientSdk.get("1002");
        clientSdk.registSub("amazing");
        Thread.sleep(1000);
        clientSdk.pub(BusHelper.Message.builder().topic("amazing").msg("来看看看看叫姐姐是").build());
        Thread.sleep(3000);
    }

}
