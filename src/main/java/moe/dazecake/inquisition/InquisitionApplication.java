package moe.dazecake.inquisition;

import moe.dazecake.inquisition.util.DynamicInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InquisitionApplication {

    public static void main(String[] args) {
        var ac = SpringApplication.run(InquisitionApplication.class, args);
        //初始化
        ac.getBean(DynamicInfo.class).initInfo();
    }

}
