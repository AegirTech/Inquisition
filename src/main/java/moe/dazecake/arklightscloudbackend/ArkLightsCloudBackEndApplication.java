package moe.dazecake.arklightscloudbackend;

import moe.dazecake.arklightscloudbackend.util.DynamicInfo;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArkLightsCloudBackEndApplication {

    public static void main(String[] args) {
        var ac = SpringApplication.run(ArkLightsCloudBackEndApplication.class, args);
        ac.getBean(DynamicInfo.class).initInfo();
    }

}
