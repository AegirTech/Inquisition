package moe.dazecake.arklightscloudbackend.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.mapper.AccountMapper;
import moe.dazecake.arklightscloudbackend.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Configuration
@EnableScheduling
public class DynamicScheduleTask implements SchedulingConfigurer {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    AccountMapper accountMapper;

    @Resource
    DeviceMapper deviceMapper;

    @Value("${cron}")
    String cron;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //每日任务刷新
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("正在刷新任务列表: " + LocalDateTime.now().toLocalTime());
                    if (dynamicInfo.getFreeTaskList().isEmpty()) {
                        dynamicInfo.getFreeTaskList().addAll(
                                accountMapper.selectList(
                                        Wrappers.<AccountEntity>lambdaQuery()
                                                .eq(AccountEntity::getDelete, 0)
                                                .eq(AccountEntity::getTaskType, "daily")
                                )
                        );
                    }
                },
                triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext)
        );
        //设备离线监控
        taskRegistrar.addTriggerTask(
                () -> dynamicInfo.getCounter().forEach(
                        (token, num) -> {
                            if (num == 0) {
                                dynamicInfo.getDeviceStatusMap().put(token, 0);
                                log.warn("设备离线: " + token);
                            } else {
                                --num;
                                dynamicInfo.getCounter().put(token, num);
                            }
                        }
                ),
                triggerContext -> new CronTrigger("0/5 * * * * ?").nextExecutionTime(triggerContext)
        );
        //任务超时检测
        taskRegistrar.addTriggerTask(
                () -> {
                    LocalDateTime nowTime = LocalDateTime.now();
                    HashMap<String, AccountEntity> exceedMap = new HashMap<>();
                    dynamicInfo.getLockTaskList().forEach(
                            (token, map) -> map.forEach(
                                    (account, time) -> {
                                        if (nowTime.isAfter(time)) {
                                            exceedMap.put(token, account);
                                        }
                                    }
                            )
                    );
                    exceedMap.forEach(
                            (token, account) -> {
                                dynamicInfo.getLockTaskList().remove(token);
                                dynamicInfo.getFreeTaskList().add(account);
                            }
                    );
                    if (exceedMap.size() > 0) {
                        log.info("已处理超时任务数: " + exceedMap.size());
                    }
                },
                triggerContext -> new CronTrigger("* 0/5 * * * ?").nextExecutionTime(triggerContext)
        );
        //设备过期检测
        taskRegistrar.addTriggerTask(
                () -> {
                    LocalDateTime nowTime = LocalDateTime.now();
                    var deviceList = deviceMapper.selectList(null);
                    deviceList.forEach(
                            (device) -> {
                                if (nowTime.isAfter(device.getExpireTime()) && device.getDelete() == 0) {
                                    device.setDelete(1);
                                    deviceMapper.updateById(device);
                                    log.info("已过期设备: " + device.getId() + "--" + device.getDeviceToken());
                                }
                            }
                    );
                },
                triggerContext -> new CronTrigger("* 0/10 * * * ?").nextExecutionTime(triggerContext)
        );
    }
}
