package moe.dazecake.inquisition.service.impl;

import com.zjiecode.wxpusher.client.bean.Message;
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
import java.util.*;

@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    LogServiceImpl logService;

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

        if (logEntity.getDetail().contains("高级资深干员")) {
            messagePush(account, "公开招募标签提醒", "恭喜你获得了高级资深干员tag，快去看看吧！");
        } else if (logEntity.getDetail().contains("资深干员")) {
            messagePush(account, "公开招募标签提醒", "恭喜你获得了资深干员tag，快去看看吧！");
        }

        logService.addLog(logEntity, deviceToken);
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
                forceHaltTask(account, false);
                dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
                dynamicInfo.getFreeTaskList().add(account);
                break;
            }
            case ("accountError"): {
                if (account.getServer() == 0) {
                    if (httpService.isOfficialAccountWork(account.getAccount(), account.getPassword())) {
                        forceHaltTask(account, false);
                        dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
                        dynamicInfo.getFreeTaskList().add(account);
                    } else {
                        forceHaltTask(account, false);
                        account.setFreeze(1);
                        accountMapper.updateById(account);
                        dynamicInfo.getUserSanList().remove(account.getId());
                        dynamicInfo.getUserMaxSanList().remove(account.getId());
                        messagePush(account, "账号异常", "您的账号密码有误，请在面板更新正确的账号密码，否则托管将无法继续进行");
                    }
                } else if (account.getServer() == 1) {
                    if (httpService.isBiliAccountWork(account.getAccount(), account.getPassword())) {
                        if (account.getBLimitDevice().size() <= 2) {
                            forceHaltTask(account, false);
                            account.setFreeze(1);
                            account.setBLimit(0);
                            account.getBLimitDevice().clear();
                            accountMapper.updateById(account);
                            dynamicInfo.getUserSanList().remove(account.getId());
                            dynamicInfo.getUserMaxSanList().remove(account.getId());
                            messagePush(account, "账号异常", "您近期登陆的设备较多，已被B服限制登陆，请立即修改密码并于面板更新密码,否则托管将无法继续进行");
                        } else {
                            account.setBLimit(1);
                            accountMapper.updateById(account);
                            forceHaltTask(account, false);
                            dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
                            dynamicInfo.getFreeTaskList().add(account);
                        }
                    } else {
                        forceHaltTask(account, false);
                        account.setFreeze(1);
                        accountMapper.updateById(account);
                        dynamicInfo.getUserSanList().remove(account.getId());
                        dynamicInfo.getUserMaxSanList().remove(account.getId());
                        messagePush(account, "账号异常", "您的账号密码有误，请在面板更新正确的账号密码，否则托管将无法继续进行");
                    }
                }
            }
            default: {
                messagePush(account, "账号异常", "您的存在异常，请立即联系管理员协助排查，否则托管将无法继续进行");
                forceHaltTask(account, false);
                break;
            }
        }
    }

    @Override
    public void forceHaltTask(AccountEntity account, boolean isHalt) {
        //清除等待队列
        dynamicInfo.getFreeTaskList().remove(account);

        //清除上锁队列
        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(account.getId())) {
                if (isHalt) {
                    dynamicInfo.getHaltList().add(lockTask.getDeviceToken());
                }
                dynamicInfo.getLockTaskList().remove(lockTask);
                break;
            }
        }

        //清除冻结队列
        dynamicInfo.getFreezeTaskList().remove(account.getId());
    }

    @Override
    public void calculatingSan() {
        //检查两个表是否存在不同步的情况
        dynamicInfo.getUserSanList().forEach((k, v) -> {
            if (!dynamicInfo.getUserMaxSanList().containsKey(k)) {
                dynamicInfo.getUserMaxSanList().put(k, 135);
            }
        });

        //获取迭代器
        Iterator<Map.Entry<Long, Integer>> entryIterator = dynamicInfo.getUserSanList().entrySet().iterator();

        //遍历所有用户
        while (entryIterator.hasNext()) {
            Long id = entryIterator.next().getKey();

            var account = accountMapper.selectById(id);

            //检查是否已删除
            if (account.getDelete() == 1) {
                entryIterator.remove();
                dynamicInfo.getUserMaxSanList().remove(id);
                continue;
            }

            //检查是否已冻结
            if (account.getFreeze() == 1) {
                entryIterator.remove();
                dynamicInfo.getUserMaxSanList().remove(id);
                continue;
            }

            //检查是否已到期
            if (account.getExpireTime().isBefore(LocalDateTime.now())) {
                entryIterator.remove();
                dynamicInfo.getUserMaxSanList().remove(id);
                messagePush(account, "到期提醒", "您的账号已到期，作战已暂停，若仍需托管请及时续费");
                continue;
            }

            //递增用户理智
            dynamicInfo.getUserSanList().put(id, dynamicInfo.getUserSanList().get(id) + 1);

            //检查是否到达阈值 阈值为最大值-40
            if (dynamicInfo.getUserSanList().get(id) >= dynamicInfo.getUserMaxSanList().get(id) - 40) {

                //检查待分配队列中是否有重复任务
                dynamicInfo.getFreeTaskList().removeIf(accountEntity -> accountEntity.getId().equals(account.getId()));

                //加入待分配队列
                dynamicInfo.getFreeTaskList().add(account);

                //归零理智
                dynamicInfo.getUserSanList().put(id, 0);

                messagePush(account, "等待分配作战服务器", "您的理智已达到 " + dynamicInfo.getUserSanList().get(id) +
                        "，等待分配作战服务器中，分配完成后将会自动开始作战");
            }

            //检查是否到达提醒阈值 阈值为最大值-45
            if (dynamicInfo.getUserSanList().get(id) == dynamicInfo.getUserMaxSanList().get(id) - 45) {
                messagePush(account, "作战预告", "您的账号最快将在30" +
                        "分钟后开始作战，若您当前仍在线，请注意合理把握时间，避免被强制下线\n\n" +
                        "若您需要轮空本次作战，请前往面板-->设置-->冻结，手动冻结账号来进行轮空\n\n" +
                        "当前理智: " +
                        dynamicInfo.getUserSanList().get(id) +
                        "/" +
                        dynamicInfo.getUserMaxSanList().get(id) + "\n\n" +
                        "(可能存在误差，仅供参考)");
            }

        }

    }
}
