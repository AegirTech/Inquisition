package moe.dazecake.inquisition.utils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.AdminMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.model.dto.chinac.ChinacPhoneEntity;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.AdminEntity;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.service.impl.ChinacServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static moe.dazecake.inquisition.utils.JWTUtils.SECRET;

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

    @Resource
    ChinacServiceImpl chinacService;

    @Value("${inquisition.secret:}")
    String secret;

    @Value("${inquisition.chinac.enableAutoDeviceManage:false}")
    boolean enableAutoDeviceManage;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("【审判庭初始化】 执行中...");
        File file = new File(System.getProperty("user.dir") + File.separator + "config" + File.separator + "data.json");
        if (file.exists()) {
            log.info("【审判庭初始化】 检测到数据文件，正在读取...");
            Gson gson = new Gson();
            dynamicInfo.load(gson.fromJson(new BufferedReader(new FileReader(file)), MemoryInfo.class));

            log.info("【审判庭初始化】 读取完成");
        } else {
            log.info("【审判庭初始化】 未检测到数据文件，正在初始化...");
            //检查admin表是否有数据
            List<AdminEntity> adminEntities = adminMapper.selectList(null);
            if (adminEntities.size() == 0) {
                AdminEntity adminEntity = new AdminEntity();
                adminEntity.setUsername("root");
                adminEntity.setPassword("7966fd2201810e386e8407feaf09b4ea");
                adminEntity.setPermission("root");
                adminMapper.insert(adminEntity);
                log.info("【审判庭初始化】 初始化管理员账号: root");
                log.info("【审判庭初始化】 初始化管理员密码: 123456");
            }

            var devices = deviceMapper.selectList(
                    Wrappers.<DeviceEntity>lambdaQuery()
                            .eq(DeviceEntity::getDelete, 0)
            );
            devices.forEach(
                    device -> {
                        dynamicInfo.getDeviceStatusMap().put(device.getDeviceToken(), 0);
                        dynamicInfo.getDeviceCounterMap().put(device.getDeviceToken(), 1);
                    }
            );
            if (enableAutoDeviceManage) {
                log.info("【审判庭初始化】 同步Chinac设备");
                var chinacDeviceList = chinacService.queryAllDeviceList();
                for (ChinacPhoneEntity chinacPhone : chinacDeviceList) {
                    if (!chinacPhone.getPayType().equals("PREPAID")) {
                        continue;
                    }
                    if (deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                            .eq(DeviceEntity::getDeviceToken, chinacPhone.getId())) == null) {
                        var newDevice = new DeviceEntity();
                        Instant instant = Instant.ofEpochMilli(chinacPhone.getDueTime());
                        ZoneId zone = ZoneId.systemDefault();
                        newDevice.setDeviceName(chinacPhone.getName())
                                .setDeviceToken(chinacPhone.getId())
                                .setRegion(chinacPhone.getRegion())
                                .setExpireTime(LocalDateTime.ofInstant(instant, zone))
                                .setDelete(0)
                                .setChinac(1);
                        deviceMapper.insert(newDevice);
                        log.info("【审判庭初始化】 同步设备 " + newDevice.getDeviceToken());
                        dynamicInfo.getDeviceStatusMap().put(newDevice.getDeviceToken(), 0);
                        dynamicInfo.getDeviceCounterMap().put(newDevice.getDeviceToken(), 1);
                    }
                }
            }
            for (AccountEntity account : accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                    .eq(AccountEntity::getDelete, 0)
                    .eq(AccountEntity::getFreeze, 0)
                    .eq(AccountEntity::getTaskType, "daily")
                    .ge(AccountEntity::getExpireTime, LocalDateTime.now())
            )) {
                dynamicInfo.setUserSanZero(account.getId());
            }

        }
        if (!secret.equals("")) {
            SECRET = secret;
        } else {
            SECRET = RandomStringUtils.randomAlphabetic(16);
            log.info("【审判庭初始化】 已生成随机 secret: " + SECRET);
            log.info("【审判庭初始化】 建议将 secret 写入配置文件中");
        }

        log.info("【审判庭初始化】 初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("【审判庭关闭】 正在保存数据...");
        Gson gson = new Gson();
        String str = gson.toJson(dynamicInfo.dump());
        try {
            var printWriter = new PrintWriter(System.getProperty("user.dir") + File.separator + "config" + File.separator + "data.json");
            printWriter.write(str);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        log.info("【审判庭关闭】 数据保存完毕");
        log.info("【审判庭关闭】 服务端已正常关闭");
    }
}
