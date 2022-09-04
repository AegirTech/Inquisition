package moe.dazecake.inquisition.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.AdminEntity;
import moe.dazecake.inquisition.entity.DeviceEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.AdminMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class RunScript implements ApplicationRunner {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    AccountMapper accountMapper;

    @Resource
    DeviceMapper deviceMapper;

    @Resource
    AdminMapper adminMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("审判庭初始化中...");
        File file = new File("config" + File.separator + "data.json");
        if (file.exists()) {
            log.info("检测到数据文件，正在读取...");
            Gson gson = new Gson();
            dynamicInfo = gson.fromJson(new BufferedReader(new FileReader(file)), DynamicInfo.class);
            log.info("读取完成");
        } else {
            log.info("未检测到数据文件，正在初始化...");
            //检查admin表是否有数据
            List<AdminEntity> adminEntities = adminMapper.selectList(null);
            if (adminEntities.size() == 0) {
                AdminEntity adminEntity = new AdminEntity();
                adminEntity.setUsername("root");
                adminEntity.setPassword("7966fd2201810e386e8407feaf09b4ea");
                adminEntity.setPermission("root");
                adminMapper.insert(adminEntity);
            }

            var devices = deviceMapper.selectList(
                    Wrappers.<DeviceEntity>lambdaQuery()
                            .eq(DeviceEntity::getDelete, 0)
                            .ge(DeviceEntity::getExpireTime, LocalDateTime.now())
            );
            devices.forEach(
                    device -> {
                        dynamicInfo.getDeviceStatusMap().put(device.getDeviceToken(), 0);
                        dynamicInfo.getCounter().put(device.getDeviceToken(), 1);
                    }
            );
            for (AccountEntity account : accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                    .eq(AccountEntity::getDelete, 0)
                    .eq(AccountEntity::getFreeze, 0)
                    .eq(AccountEntity::getTaskType, "daily")
                    .ge(AccountEntity::getExpireTime, LocalDateTime.now())
            )) {
                dynamicInfo.getUserSanList().put(account.getId(), 0);
                dynamicInfo.getUserMaxSanList().put(account.getId(), 135);
            }
            log.info("审判庭初始化完成");
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("正在保存数据...");
        Gson gson = new Gson();
        String str = gson.toJson(dynamicInfo);
        try {
            var printWriter = new PrintWriter("config" + File.separator + "data.json");
            printWriter.write(str);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        log.info("数据保存完毕");
    }
}
