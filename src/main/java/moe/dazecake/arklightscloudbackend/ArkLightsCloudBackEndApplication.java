package moe.dazecake.arklightscloudbackend;

import moe.dazecake.arklightscloudbackend.util.DynamicInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArkLightsCloudBackEndApplication {

    public static void main(String[] args) {
        var ac = SpringApplication.run(ArkLightsCloudBackEndApplication.class, args);
        //初始化
        ac.getBean(DynamicInfo.class).initInfo();
    }

}
