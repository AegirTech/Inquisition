package moe.dazecake.inquisition;

import moe.dazecake.inquisition.service.impl.ChinacServiceImpl;
import moe.dazecake.inquisition.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@SpringBootTest(classes = InquisitionApplication.class)
@RunWith(SpringRunner.class)
class InquisitionApplicationTests {

    @Autowired
    private ChinacServiceImpl chinacService;

    @Resource
    EmailServiceImpl emailService;

    @Test
    void logAllChinacDevice() {
        System.out.println(chinacService.queryAllDeviceList());
    }

    @Test
    void testChinacBuy() {
        chinacService.createDevice("cn-jsha-cloudphone-3",
                "i-bb15bm9sg1y696",
                "ONDEMAND",
                805321,
                null, null, null, null);

    }

    @Test
    void testChinacGetDeviceList() {
        System.out.println(chinacService.queryAllDeviceList());
    }

    @Test
    void testString() {
        SimpleDateFormat format = new SimpleDateFormat("MM_dd");
        String time = format.format(new Date().getTime());
        System.out.println("格式化结果0：" + time);
    }

    @Test
    void getDeviceControlInfo() {
        var ids = new ArrayList<String>();
        ids.add("cp-5n15bmbsaz7460");
        System.out.println(chinacService.getDeviceControlInfo(ids, "cn-jsha-cloudphone-3"));
    }

    @Test
    void getScreenshot() {
        var ids = new ArrayList<String>();
        ids.add("cp-5n15bmbsaz7460");
        HashMap<String, String> deviceScreenshot = chinacService.getDeviceScreenshot(ids, "cn-jsha-cloudphone-3");
        System.out.println(deviceScreenshot);
    }

    @Test
    void testRemoteControl() {
        System.out.println(chinacService.getDeviceRemoteControlUrl(
                "cn-jsha-cloudphone-3",
                "cp-5n15bmbsaz7460",
                30,
                false,
                false,
                null
        ));
    }

    @Test
    void testMail() {
        System.out.println("测试邮件");
        emailService.sendSimpleMail("1936260102@qq.com", "test", "test");
        System.out.println("测试邮件over");
    }
}
