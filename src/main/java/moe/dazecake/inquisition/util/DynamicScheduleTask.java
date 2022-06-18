package moe.dazecake.inquisition.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.controller.LogController;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.DeviceEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

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

    @Resource
    LogController logController;

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
                                                .ge(AccountEntity::getExpireTime, LocalDateTime.now())
                                )
                        );
                    }

                    //记录日志
                    LogEntity logEntity = new LogEntity();
                    AtomicReference<String> detail = new AtomicReference<>("");
                    if (dynamicInfo.getFreeTaskList().isEmpty()) {
                        detail.set("有 " + dynamicInfo.getFreeTaskList().size() + " 个任务尚未完成\n");
                        dynamicInfo.getFreeTaskList().forEach(
                                accountEntity -> detail.set((detail +
                                        accountEntity.getTaskType() +
                                        "\t" +
                                        accountEntity.getServer()).equals("0") ? "官服" : "B服" +
                                        "\t" +
                                        accountEntity.getAccount()
                                )
                        );
                    } else {
                        detail.set("已刷新 " + dynamicInfo.getFreeTaskList().size() + " 个任务\n");
                    }

                    logEntity.setLevel("INFO")
                            .setTaskType("system")
                            .setTitle("任务列表刷新")
                            .setDetail(detail.get())
                            .setFrom("system")
                            .setTime(LocalDateTime.now());
                    logController.addLog(logEntity, "system");

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

                                //记录日志
                                LogEntity logEntity = new LogEntity();
                                logEntity.setLevel("WARNING")
                                        .setTaskType("system")
                                        .setTitle("设备离线")
                                        .setDetail("设备token: " + token)
                                        .setFrom(token)
                                        .setTime(LocalDateTime.now());
                                logController.addLog(logEntity, "system");

                                dynamicInfo.getCounter().put(token, -1);

                            } else {
                                if (num > 0) {
                                    --num;
                                    dynamicInfo.getCounter().put(token, num);
                                }
                            }
                        }
                ),
                triggerContext -> new CronTrigger("0/5 * * * * ?").nextExecutionTime(triggerContext)
        );
        //任务超时检测
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("任务超时检测");
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

                                //记录日志
                                LogEntity logEntity = new LogEntity();
                                logEntity.setLevel("WARNING")
                                        .setTaskType(account.getTaskType())
                                        .setTitle("任务超时")
                                        .setDetail("")
                                        .setFrom(token)
                                        .setName(account.getName())
                                        .setPassword(account.getPassword())
                                        .setTime(LocalDateTime.now());
                                logController.addLog(logEntity, "system");

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
                    log.info("设备过期检测");
                    LocalDateTime nowTime = LocalDateTime.now();
                    var deviceList = deviceMapper.selectList(null);
                    deviceList.forEach(
                            (device) -> {
                                if (nowTime.isAfter(device.getExpireTime()) && device.getDelete() == 0) {
                                    device.setDelete(1);
                                    deviceMapper.updateById(device);
                                    log.info("已过期设备: " + device.getId() + "--" + device.getDeviceToken());

                                    //记录日志
                                    LogEntity logEntity = new LogEntity();
                                    logEntity.setLevel("WARNING")
                                            .setTaskType("system")
                                            .setTitle("设备过期")
                                            .setDetail("")
                                            .setFrom(device.getDeviceToken())
                                            .setTime(LocalDateTime.now());
                                    logController.addLog(logEntity, "system");

                                }
                            }
                    );
                },
                triggerContext -> new CronTrigger("* 0/10 * * * ?").nextExecutionTime(triggerContext)
        );
        //设备载入刷新
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("设备载入刷新");
                    var devices = deviceMapper.selectList(
                            Wrappers.<DeviceEntity>lambdaQuery()
                                    .eq(DeviceEntity::getDelete, 0)
                                    .ge(DeviceEntity::getExpireTime, LocalDateTime.now())
                    );
                    devices.forEach(
                            device -> {
                                if (!dynamicInfo.getDeviceStatusMap().containsKey(device.getDeviceToken())) {
                                    dynamicInfo.getDeviceStatusMap().put(device.getDeviceToken(), 0);
                                    dynamicInfo.getCounter().put(device.getDeviceToken(), 1);
                                }
                            }
                    );
                },
                triggerContext -> new CronTrigger("* 0/5 * * * ?").nextExecutionTime(triggerContext)
        );
    }
}
