package moe.dazecake.inquisition.service.impl;

import com.zjiecode.wxpusher.client.bean.Message;
import moe.dazecake.inquisition.controller.LogController;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.service.TaskService;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    LogController logController;

    @Resource
    EmailServiceImpl emailService;

    @Resource
    WXPusherServiceImpl wxPusherService;

    @Resource
    HttpServiceImpl httpService;

    @Resource
    AccountMapper accountMapper;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${wx-pusher.enable}")
    boolean enableWxPusher;


    //检查是否处于时间激活区间，如果是，则返回true，否则返回false
    @Override
    public boolean checkActivationTime(AccountEntity account) {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

        switch (dayOfWeek) {
            case 1:
                if (account.getActive().getMonday().isEnable()) {

                    if (account.getActive().getMonday().getDetail().isEmpty()) {
                        break;
                    } else {
                        //遍历非激活时间区间
                        for (int i1 = 0; i1 < account.getActive().getMonday().getDetail().size(); i1++) {
                            String[] time = account.getActive().getMonday().getDetail().get(i1).split("-");

                            //处于非激活时间内
                            if (TimeUtil.isInTime(time[0], time[1])) {
                                //不通过
                                return false;
                            }
                        }
                    }

                } else {
                    return false;
                }
                break;
            case 2:
                if (account.getActive().getTuesday().isEnable()) {

                    if (account.getActive().getTuesday().getDetail().isEmpty()) {
                        break;
                    } else {
                        //遍历非激活时间区间
                        for (int i1 = 0; i1 < account.getActive().getTuesday().getDetail().size(); i1++) {
                            String[] time = account.getActive().getTuesday().getDetail().get(i1).split("-");

                            //处于非激活时间内
                            if (TimeUtil.isInTime(time[0], time[1])) {
                                //不通过
                                return false;
                            }
                        }
                    }

                } else {
                    return false;
                }
                break;
            case 3:
                if (account.getActive().getWednesday().isEnable()) {

                    if (account.getActive().getWednesday().getDetail().isEmpty()) {
                        break;
                    } else {
                        //遍历非激活时间区间
                        for (int i1 = 0; i1 < account.getActive().getWednesday().getDetail().size(); i1++) {
                            String[] time = account.getActive().getWednesday().getDetail().get(i1).split("-");

                            //处于非激活时间内
                            if (TimeUtil.isInTime(time[0], time[1])) {
                                //不通过
                                return false;
                            }
                        }
                    }

                } else {
                    return false;
                }
                break;
            case 4:
                if (account.getActive().getThursday().isEnable()) {

                    if (account.getActive().getThursday().getDetail().isEmpty()) {
                        break;
                    } else {
                        //遍历非激活时间区间
                        for (int i1 = 0; i1 < account.getActive().getThursday().getDetail().size(); i1++) {
                            String[] time = account.getActive().getThursday().getDetail().get(i1).split("-");

                            //处于非激活时间内
                            if (TimeUtil.isInTime(time[0], time[1])) {
                                //不通过
                                return false;
                            }
                        }
                    }

                } else {
                    return false;
                }
                break;
            case 5:
                if (account.getActive().getFriday().isEnable()) {

                    if (account.getActive().getFriday().getDetail().isEmpty()) {
                        break;
                    } else {
                        //遍历非激活时间区间
                        for (int i1 = 0; i1 < account.getActive().getFriday().getDetail().size(); i1++) {
                            String[] time = account.getActive().getFriday().getDetail().get(i1).split("-");

                            //处于非激活时间内
                            if (TimeUtil.isInTime(time[0], time[1])) {
                                //不通过
                                return false;
                            }
                        }
                    }

                } else {
                    return false;
                }
                break;
            case 6:
                if (account.getActive().getSaturday().isEnable()) {

                    if (account.getActive().getSaturday().getDetail().isEmpty()) {
                        break;
                    } else {
                        //遍历非激活时间区间
                        for (int i1 = 0; i1 < account.getActive().getSaturday().getDetail().size(); i1++) {
                            String[] time = account.getActive().getSaturday().getDetail().get(i1).split("-");

                            //处于非激活时间内
                            if (TimeUtil.isInTime(time[0], time[1])) {
                                //不通过
                                return false;
                            }
                        }
                    }

                } else {
                    return false;
                }
                break;
            case 0:
                if (account.getActive().getSunday().isEnable()) {

                    if (account.getActive().getSunday().getDetail().isEmpty()) {
                        break;
                    } else {
                        //遍历非激活时间区间
                        for (int i1 = 0; i1 < account.getActive().getSunday().getDetail().size(); i1++) {
                            String[] time = account.getActive().getSunday().getDetail().get(i1).split("-");

                            //处于非激活时间内
                            if (TimeUtil.isInTime(time[0], time[1])) {
                                //不通过
                                return false;
                            }
                        }
                    }

                } else {
                    return false;
                }
                break;
        }
        return true;
    }

    @Override
    public boolean checkFreeze(AccountEntity account) {
        if (dynamicInfo.getFreezeTaskList().containsKey(account.getId())) {
            //检测是否结束冻结
            if (dynamicInfo.getFreezeTaskList().get(account.getId()).isBefore(LocalDateTime.now())) {
                dynamicInfo.getFreezeTaskList().remove(account.getId());
                //解冻，不在冻结状态
                return false;
            }
            //仍处于冻结
            return true;

        } else {
            //不在冻结状态
            return false;
        }
    }

    @Override
    public void lockTask(String deviceToken, AccountEntity account) {
        LocalDateTime localDateTime = LocalDateTime.now();
        var lockTask = new LockTask();
        lockTask.setDeviceToken(deviceToken);
        switch (account.getTaskType()) {
            case "daily":
                lockTask.setAccount(account);
                lockTask.setExpirationTime(localDateTime.plusHours(2));
                break;
            case "rogue":
                lockTask.setAccount(account);
                lockTask.setExpirationTime(localDateTime.plusHours(48));
                break;
        }
        dynamicInfo.getLockTaskList().add(lockTask);
    }

    @Override
    public void log(String deviceToken, AccountEntity account, String level, String title,
                    String content, String imgUrl) {
        LogEntity logEntity = new LogEntity();
        String type = "";
        if (Objects.equals(account.getTaskType(), "daily")) {
            type = "每日";
        } else if (Objects.equals(account.getTaskType(), "rogue")) {
            type = "肉鸽";
        }

        String detail =
                "[" + type + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + type +
                        content;

        logEntity.setLevel(level)
                .setTaskType(account.getTaskType())
                .setTitle(title)
                .setDetail(detail)
                .setImageUrl(imgUrl)
                .setFrom(deviceToken)
                .setServer(account.getServer())
                .setName(account.getName())
                .setAccount(account.getAccount())
                .setTime(LocalDateTime.now());

        logController.addLog(logEntity, deviceToken);
    }

    @Override
    public void messagePush(AccountEntity account, String title, String content) {
        //微信推送
        if (enableWxPusher && account.getNotice().getWxUID().getEnable()) {
            wxPusherService.push(Message.CONTENT_TYPE_MD,
                    "# " + title + "\n\n" +
                            content,
                    account.getNotice().getWxUID().getText(),
                    null);
        }

        //邮件推送
        if (enableMail && account.getNotice().getMail().getEnable()) {
            emailService.sendSimpleMail(account.getNotice().getMail().getText(), title,
                    content);
        }
    }

    @Override
    public void errorHandle(AccountEntity account, String deviceToken, String type) {

        switch (type) {
            case ("lineBusy"): {
                dynamicInfo.getFreeTaskList().add(account);
                dynamicInfo.getLockTaskList().removeIf(e -> e.getDeviceToken().equals(deviceToken));
                dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
                break;
            }
            case ("accountError"): {
                if (account.getServer() == 0) {
                    if (httpService.isOfficialAccountWork(account.getAccount(), account.getPassword())) {
                        dynamicInfo.getFreeTaskList().add(account);
                        dynamicInfo.getLockTaskList().removeIf(e -> e.getDeviceToken().equals(deviceToken));
                    } else {
                        forceHaltTask(account);
                        account.setFreeze(1);
                        accountMapper.updateById(account);
                        dynamicInfo.getUserSanList().remove(account.getId());
                        messagePush(account, "账号异常", "您的账号密码有误，请在面板更新正确的账号密码，否则托管将无法继续进行");
                    }
                } else if (account.getServer() == 1) {
                    if (httpService.isBiliAccountWork(account.getAccount(), account.getPassword())) {
                        if (account.getBLimitDevice().size() <= 2) {
                            forceHaltTask(account);
                            account.setFreeze(1);
                            account.setBLimit(0);
                            account.getBLimitDevice().clear();
                            accountMapper.updateById(account);
                            dynamicInfo.getUserSanList().remove(account.getId());
                            messagePush(account, "账号异常", "您近期登陆的设备较多，已被B服限制登陆，请立即修改密码并于面板更新密码,否则托管将无法继续进行");
                        } else {
                            account.setBLimit(1);
                            accountMapper.updateById(account);
                            dynamicInfo.getFreeTaskList().add(account);
                            dynamicInfo.getLockTaskList().removeIf(e -> e.getDeviceToken().equals(deviceToken));
                        }
                    } else {
                        forceHaltTask(account);
                        account.setFreeze(1);
                        accountMapper.updateById(account);
                        dynamicInfo.getUserSanList().remove(account.getId());
                        messagePush(account, "账号异常", "您的账号密码有误，请在面板更新正确的账号密码，否则托管将无法继续进行");
                    }
                }
            }
            default: {
                //强停
                account.setFreeze(1);
                accountMapper.updateById(account);
                dynamicInfo.getUserSanList().remove(account.getId());
                dynamicInfo.getLockTaskList().removeIf(e -> e.getDeviceToken().equals(deviceToken));
                break;
            }
        }
    }

    @Override
    public void forceHaltTask(AccountEntity account) {
        //清除等待队列
        dynamicInfo.getFreeTaskList().remove(account);

        //清除上锁队列
        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(account.getId())) {
                dynamicInfo.getHaltList().add(lockTask.getDeviceToken());
                dynamicInfo.getLockTaskList().remove(lockTask);
                break;
            }
        }

        //清除冻结队列
        dynamicInfo.getFreezeTaskList().remove(account.getId());
    }
}
