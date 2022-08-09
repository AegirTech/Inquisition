package moe.dazecake.inquisition.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zjiecode.wxpusher.client.bean.Message;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.controller.LogController;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.DeviceEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.service.impl.EmailServiceImpl;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import moe.dazecake.inquisition.service.impl.WXPusherServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Resource
    EmailServiceImpl emailService;

    @Resource
    WXPusherServiceImpl wxPusherService;

    @Resource
    TaskServiceImpl taskService;

    @Value("${cron:'0 0 4,12,20 * * ?'}")
    String cron;

    @Value("${spring.mail.to}")
    String to;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //每日任务刷新
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("正在刷新任务列表: " + LocalDateTime.now().toLocalTime());
                    if (!dynamicInfo.getFreeTaskList().isEmpty()) {
                        //无daily任务，追加
                        var hasDaily = false;
                        for (var accountEntity : dynamicInfo.getFreeTaskList()) {
                            if (accountEntity.getTaskType().equals("daily")) {
                                hasDaily = true;
                                break;
                            }
                        }
                        if (hasDaily) {
                            emailService.sendSimpleMail(to, "设备数量不足警告",
                                    "设备数量不足，造成了最多可能 " + dynamicInfo.getFreeTaskList().size() +
                                            " 个任务被强制清除，请注意及时增加设备");
                            //清除所有daily任务
                            dynamicInfo.getFreeTaskList()
                                    .removeIf(accountEntity -> accountEntity.getTaskType().equals("daily"));
                        }
                    }
                    accountMapper.selectList(
                            Wrappers.<AccountEntity>lambdaQuery()
                                    .eq(AccountEntity::getDelete, 0)
                                    .eq(AccountEntity::getFreeze, 0)
                                    .eq(AccountEntity::getTaskType, "daily")
                                    .ge(AccountEntity::getExpireTime, LocalDateTime.now())
                    ).forEach(accountEntity -> {
                        if (taskService.checkActivationTime(accountEntity)) {
                            dynamicInfo.getFreeTaskList().add(accountEntity);
                        }
                    });

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
                            if (num > 0 || num > -60 && num < 0) {
                                --num;
                                dynamicInfo.getCounter().put(token, num);
                            } else if (num == 0) {
                                dynamicInfo.getDeviceStatusMap().put(token, 0);
                                log.warn("设备离线: " + token);

                                dynamicInfo.getCounter().put(token, -1);
                            } else if (num == -60) {
                                //重连超时提示
                                var device = deviceMapper.selectOne(
                                        Wrappers.<DeviceEntity>lambdaQuery()
                                                .eq(DeviceEntity::getDeviceToken, token)
                                );

                                //记录日志
                                LogEntity logEntity = new LogEntity();
                                logEntity.setLevel("WARN")
                                        .setTaskType("system")
                                        .setTitle("设备离线")
                                        .setDetail("设备名称: " + device.getDeviceName() + "\n" +
                                                "设备token: " + device.getDeviceToken() + "\n"
                                        )
                                        .setFrom(token)
                                        .setTime(LocalDateTime.now());
                                logController.addLog(logEntity, "system");

                                if (enableMail) {
                                    //邮件通知
                                    String emailStr = "设备名称: " + device.getDeviceName() + "\n"
                                            + "设备token: " + device.getDeviceToken() + "\n"
                                            + "时间: " + LocalDateTime.now() + "\n";

                                    emailService.sendSimpleMail(to, "设备离线", emailStr);
                                }

                                //更新设备状态
                                dynamicInfo.getCounter().put(token, -61);
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
                                logEntity.setLevel("WARN")
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
                triggerContext -> new CronTrigger("0 0/5 * * * ?").nextExecutionTime(triggerContext)
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
                                    logEntity.setLevel("WARN")
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
                triggerContext -> new CronTrigger("0 0/10 * * * ?").nextExecutionTime(triggerContext)
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
                triggerContext -> new CronTrigger("0 0/5 * * * ? ").nextExecutionTime(triggerContext)
        );
        //账号过期检测
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("账号过期检测");
                    var finalTime = LocalDateTime.now().plusDays(7);
                    var accountList = accountMapper.selectList(null);
                    accountList.forEach(
                            (account) -> {
                                if (finalTime.isAfter(account.getExpireTime()) && LocalDateTime.now()
                                        .isBefore(account.getExpireTime()) && account.getDelete() == 0) {
                                    var msg = "您的托管账号将于" + account.getExpireTime()
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "过期，记得及时续费哦。";

                                    //邮件推送
                                    if (enableMail && account.getNotice().getMail().getEnable()) {
                                        emailService.sendSimpleMail(account.getNotice().getMail().getText(),
                                                "【明日方舟】托管续费提醒",
                                                msg);
                                    }

                                    //微信推送
                                    if (enableWxPusher && account.getNotice().getWxUID().getEnable()) {
                                        wxPusherService.push(Message.CONTENT_TYPE_MD,
                                                msg,
                                                account.getNotice().getWxUID().getText(),
                                                null
                                        );
                                    }
                                }
                            }
                    );
                },
                triggerContext -> new CronTrigger("0 0 20 * * ?").nextExecutionTime(triggerContext)
        );
        //每日刷新次数更新
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("每日刷新次数更新");
                    var accountList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                            .eq(AccountEntity::getRefresh, 0)
                    );
                    accountList.forEach(
                            (account) -> {
                                account.setRefresh(1);
                                accountMapper.updateById(account);
                            }
                    );
                },
                triggerContext -> new CronTrigger("0 0 0 * * ?").nextExecutionTime(triggerContext)
        );
    }
}
