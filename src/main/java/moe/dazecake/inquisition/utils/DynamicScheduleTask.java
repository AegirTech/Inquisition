package moe.dazecake.inquisition.utils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.service.impl.ChinacServiceImpl;
import moe.dazecake.inquisition.service.impl.LogServiceImpl;
import moe.dazecake.inquisition.service.impl.MessageServiceImpl;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
    LogServiceImpl logService;

    @Resource
    MessageServiceImpl messageService;

    @Resource
    TaskServiceImpl taskService;

    @Resource
    ChinacServiceImpl chinacService;

    @Value("${spring.mail.to:}")
    String to;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;

    @Value("${inquisition.chinac.enableAutoDeviceManage:false}")
    boolean enableAutoDeviceManage;

    @Value("${inquisition.chinac.maxPlayerInDevice:25}")
    Integer maxPlayerInDevice;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //队列巡检
        taskRegistrar.addTriggerTask(
                () -> {
                    //log.info("正在巡检队列: " + LocalDateTime.now().toLocalTime());
                    //检查等待队列中是否存在重复项，若存在删除多余的重复项
                    LinkedHashSet<Long> set = new LinkedHashSet<>(dynamicInfo.getWaitUserList());
                    dynamicInfo.setWaitUserList(new ArrayList<>(set));
                },
                triggerContext -> new CronTrigger("0 */1 * * * *").nextExecutionTime(triggerContext)
        );
        //理智刷新
        taskRegistrar.addTriggerTask(
                () -> {
                    //log.info("正在刷新用户理智: " + LocalDateTime.now().toLocalTime());
                    taskService.calculatingSan();
                },
                triggerContext -> new CronTrigger("0 */6 * * * *").nextExecutionTime(triggerContext)
        );
        //设备离线监控
        taskRegistrar.addTriggerTask(
                () -> {
                    for (java.util.Map.Entry<String, Integer> count : dynamicInfo.getDeviceCounterMap().entrySet()) {

                        var token = count.getKey();
                        var num = count.getValue();

                        --num;
                        dynamicInfo.getDeviceCounterMap().put(token, num);

                        if (num == 0) {
                            dynamicInfo.getDeviceStatusMap().put(token, 0);
//                            log.warn("设备离线: " + token);
                        } else if (num == -60) {
                            //重连超时提示
                            var device = deviceMapper.selectOne(
                                    Wrappers.<DeviceEntity>lambdaQuery()
                                            .eq(DeviceEntity::getDeviceToken, token)
                            );

                            //记录日志
                            logService.logWarn("设备离线", "设备名称: " + device.getDeviceName() + "\n" +
                                    "设备token: " + device.getDeviceToken() + "\n");

                            //邮件通知
                            messageService.pushAdmin("[审判庭] 设备离线", "设备名称: " + device.getDeviceName() + "\n"
                                    + "设备token: " + device.getDeviceToken() + "\n"
                                    + "时间: " + LocalDateTime.now() + "\n");

                        } else if (num == 86400) {
                            //超时24h，移除设备
                            dynamicInfo.getDeviceStatusMap().remove(token);
                            dynamicInfo.getDeviceCounterMap().remove(token);

                            var device = deviceMapper.selectOne(
                                    Wrappers.<DeviceEntity>lambdaQuery()
                                            .eq(DeviceEntity::getDeviceToken, token)
                            );
                            device.setDelete(1);
                            deviceMapper.updateById(device);

                            //记录日志
                            logService.logWarn("设备移除", "设备名称: " + device.getDeviceName() + "\n" +
                                    "设备token: " + device.getDeviceToken() + "\n");

                            //邮件通知
                            messageService.pushAdmin("[审判庭] 设备移除", "设备名称: " + device.getDeviceName() + "\n"
                                    + "设备token: " + device.getDeviceToken() + "\n"
                                    + "时间: " + LocalDateTime.now() + "\n");
                        }
                    }
                },
                triggerContext -> new CronTrigger("0/5 * * * * ?").nextExecutionTime(triggerContext)
        );
        //任务超时检测
        taskRegistrar.addTriggerTask(
                () -> {
                    //log.info("任务超时检测");
                    LocalDateTime nowTime = LocalDateTime.now();
                    int num = 0;
                    synchronized (dynamicInfo.getWorkUserList()) {
                        for (Long worker : dynamicInfo.getWorkUserList()) {
                            if (!dynamicInfo.getWorkUserInfoMap().containsKey(worker)) {
                                continue;
                            }
                            if (dynamicInfo.getWorkUserExpireTime(worker).isBefore(nowTime)) {
                                //记录日志
                                logService.logWarn("任务超时", "");
                                taskService.forceHaltTask(worker);
                                num++;
                            }
                        }
                    }
                    if (num > 0) {
                        log.info("【审判庭】 已处理超时任务数: " + num);
                    }
                },
                triggerContext -> new CronTrigger("0 0/5 * * * ?").nextExecutionTime(triggerContext)
        );
        //账号过期检测
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("【审判庭】 账号过期检测");
                    var finalTime = LocalDateTime.now().plusDays(7);
                    var accountList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                            .lt(AccountEntity::getExpireTime, finalTime)
                            .gt(AccountEntity::getExpireTime, LocalDateTime.now())
                            .eq(AccountEntity::getDelete, 0));
                    accountList.forEach(
                            (account) -> {
                                log.info("【临期账号】: " + account.getName() + "\t" + account.getAccount());
                                var msg = "您的托管账号将于" + account.getExpireTime()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "过期，记得及时续费哦。";

                                messageService.push(account, "【明日方舟】托管续费提醒", msg);
                            }
                    );
                },
                triggerContext -> new CronTrigger("0 0 20 * * ?").nextExecutionTime(triggerContext)
        );
        //账号冻结检测
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("【审判庭】 账号冻结检测");
                    var accountList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                            .gt(AccountEntity::getExpireTime, LocalDateTime.now())
                            .eq(AccountEntity::getFreeze, 1)
                            .eq(AccountEntity::getDelete, 0));
                    accountList.forEach(
                            (account) -> {
                                log.info("【冻结账号】: " + account.getName() + "\t" + account.getAccount());
                                var msg = "您的账号仍处于冻结状态，若非手动冻结请及时检查账号状态，避免浪费账号托管时长";

                                messageService.push(account, "【明日方舟】账号冻结提醒", msg);

                            }
                    );
                },
                triggerContext -> new CronTrigger("0 0 20 * * ?").nextExecutionTime(triggerContext)
        );
        //每日刷新次数更新
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("【审判庭】 每日刷新次数更新");
                    var accountList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                            .le(AccountEntity::getRefresh, 0)
                            .eq(AccountEntity::getDelete, 0)
                            .ge(AccountEntity::getExpireTime, LocalDateTime.now())
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
        //动态设备管理
        taskRegistrar.addTriggerTask(
                () -> {
                    if (!enableAutoDeviceManage) {
                        return;
                    }
                    log.info("【审判庭】 动态设备增加");
                    var payedUserList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                            .ge(AccountEntity::getExpireTime, LocalDateTime.now())
                            .eq(AccountEntity::getDelete, 0));
                    var deviceList = deviceMapper.selectList(Wrappers.<DeviceEntity>lambdaQuery()
                            .eq(DeviceEntity::getDelete, 0));
                    if (deviceList.size() < payedUserList.size() / maxPlayerInDevice) {
                        var newDevice = chinacService.createDevice(
                                "cn-jsha-cloudphone-3",
                                "805321",
                                "PREPAID",
                                0,
                                1,
                                null, null, null);
                        if (newDevice == null) {
                            messageService.pushAdmin("[审判庭] 设备增加失败提醒", "设备增加失败，请检查平台余额是否充足");
                            return;
                        }
                        SimpleDateFormat format = new SimpleDateFormat("MM_dd");
                        String time = format.format(new Date().getTime());
                        deviceMapper.insert(new DeviceEntity()
                                .setDeviceName("审判庭_" + time)
                                .setRegion("")
                                .setDeviceToken(newDevice.get(0))
                                .setDelete(0)
                        );
                        String text = "激活用户数量: " + payedUserList.size() + "\n" +
                                "设备数量: " + deviceList.size() + "\n" +
                                "已为您自动增添新设备，请留意扣费信息";
                        messageService.pushAdmin("[审判庭] 设备增加提醒", text);
                    }
                    log.info("【审判庭】 设备自动续费");

                    //检测多余设备跳过续费 最多允许冗余设备数量: 2
                    var overNum = (payedUserList.size() - deviceList.size() * maxPlayerInDevice) / maxPlayerInDevice;
                    //过滤手动添加设备
                    deviceList.removeIf(device -> device.getChinac() != 1);
                    if (overNum > 2) {
                        for (int i = 0; i < overNum; i++) {
                            Iterator<DeviceEntity> iterator = deviceList.iterator();
                            var flagDevice = iterator.next();
                            while (iterator.hasNext()) {
                                var device = iterator.next();
                                if (flagDevice.getExpireTime().isBefore(device.getExpireTime())) {
                                    flagDevice = device;
                                }
                            }
                            deviceList.remove(flagDevice);
                        }
                    }
                    for (DeviceEntity device : deviceList) {
                        if (device.getExpireTime().isBefore(LocalDateTime.now().plusDays(7)) && device.getChinac() == 1) {
                            if (chinacService.renewDevice(device.getRegion(), device.getDeviceToken(), 1)) {
                                String text = "续费设备: " + device.getDeviceName() + "\n" +
                                        "已为您自动续费，请留意扣费信息";
                                messageService.pushAdmin("[审判庭] 设备续费提醒", text);
                            } else {
                                String text = "自动续费失败，请检查平台余额是否充足";
                                messageService.pushAdmin("[审判庭] 设备续费失败提醒", text);
                            }
                            break;
                        }
                    }
                },
                triggerContext -> new CronTrigger("0 0 20 * * ?").nextExecutionTime(triggerContext)
        );
        //异常账号检测
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("【异常账号检测】 检测开始");
                    var accountList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                            .eq(AccountEntity::getFreeze, 0)
                            .eq(AccountEntity::getDelete, 0)
                            .ge(AccountEntity::getExpireTime, LocalDateTime.now())
                    );
                    accountList.forEach(
                            (account) -> {
                                if (!dynamicInfo.getUserSanInfoMap().containsKey(account.getId())) {
                                    log.info("【异常账号检测】 异常账号: " + account.getAccount() + " " + account.getAccount());
                                    dynamicInfo.setUserSan(account.getId(), 135, 135);
                                }
                            }
                    );
                    log.info("【异常账号检测】 已完成所有异常账号自动检修");
                },
                triggerContext -> new CronTrigger("0 0 4 * * ?").nextExecutionTime(triggerContext)
        );
    }
}
