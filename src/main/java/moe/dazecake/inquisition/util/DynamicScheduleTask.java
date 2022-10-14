package moe.dazecake.inquisition.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zjiecode.wxpusher.client.bean.Message;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.controller.LogController;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.DeviceEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.entity.TaskDateSet.LockTask;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;

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

    @Value("${spring.mail.to}")
    String to;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //队列巡检
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("正在巡检队列: " + LocalDateTime.now().toLocalTime());
                    //检查等待队列中是否存在重复项，若存在删除多余的重复项
                    LinkedHashSet<AccountEntity> set = new LinkedHashSet<>(dynamicInfo.freeTaskList);
                    dynamicInfo.freeTaskList = new ArrayList<>(set);
                },
                triggerContext -> new CronTrigger("0 */1 * * * *").nextExecutionTime(triggerContext)
        );
        //理智刷新
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("正在刷新用户理智: " + LocalDateTime.now().toLocalTime());
                    taskService.calculatingSan();
                },
                triggerContext -> new CronTrigger("0 */6 * * * *").nextExecutionTime(triggerContext)
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
                    int num = 0;
                    for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
                        if (lockTask.getExpirationTime().isBefore(nowTime)) {
                            //记录日志
                            LogEntity logEntity = new LogEntity();
                            logEntity.setLevel("WARN")
                                    .setTaskType(lockTask.getAccount().getTaskType())
                                    .setTitle("任务超时")
                                    .setDetail("")
                                    .setFrom(lockTask.getDeviceToken())
                                    .setName(lockTask.getAccount().getName())
                                    .setPassword(lockTask.getAccount().getPassword())
                                    .setTime(LocalDateTime.now());
                            logController.addLog(logEntity, "system");

                            taskService.forceHaltTask(lockTask.getAccount());
                            num++;
                        }
                    }
                    if (num > 0) {
                        log.info("已处理超时任务数: " + num);
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
