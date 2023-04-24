package moe.dazecake.inquisition;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({"com.gitee.sunchenbin.mybatis.actable.dao.*", "moe.dazecake.inquisition.mapper"})
@ComponentScan(basePackages = {"com.gitee.sunchenbin.mybatis.actable.manager.*", "moe.dazecake.inquisition.*"})
@ServletComponentScan
public class InquisitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(InquisitionApplication.class, args);
    }

}


