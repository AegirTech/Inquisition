package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.mapper.mapstruct.AccountConvert;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.log.AddLogDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.model.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.service.intf.TaskService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import moe.dazecake.inquisition.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    DeviceMapper deviceMapper;

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    LogServiceImpl logService;

    @Resource
    MessageServiceImpl messageService;

    @Resource
    HttpServiceImpl httpService;

    @Resource
    AccountMapper accountMapper;

    @Resource
    EmailServiceImpl emailService;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${spring.mail.to:}")
    String to;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;


    @Override
    public Result<AccountDTO> getTask(String deviceToken) {
        //设备合法性检查
        if (deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDeviceToken, deviceToken)) == null) {
            return Result.unauthorized("设备未授权");
        }

        //重复请求检查
        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getDeviceToken().equals(deviceToken)) {
                return Result.repeatSuccess(AccountConvert.INSTANCE.toAccountDTO(lockTask.getAccount()), "重复获取");
            }
        }

        //任务上锁
        if (!dynamicInfo.getFreeTaskList().isEmpty()) {
            var account = new AccountEntity();

            //检查任务是否达到下发标准
            var iterator = dynamicInfo.getFreeTaskList().iterator();
            var hit = false;
            while (iterator.hasNext()) {
                account = iterator.next();

                //时间检查，不在激活区间则跳转到下一个判断
                if (!checkActivationTime(account)) {
                    iterator.remove();
                    continue;
                }

                //B服限制检查
                if (account.getServer() == 1 && account.getBLimitDevice().size() != 0) {
                    var usedDeviceToken = account.getBLimitDevice().get(0);
                    if (!Objects.equals(usedDeviceToken, deviceToken)) {
                        if (dynamicInfo.getDeviceStatusMap().containsKey(usedDeviceToken)) {
                            continue;
                        } else {
                            account.getBLimitDevice().clear();
                            accountMapper.updateById(account);
                        }
                    }
                }

                //重复分配任务检查
                AccountEntity finalAccount = account;
                if (dynamicInfo.getLockTaskList().stream()
                        .anyMatch(lockTask -> lockTask.getAccount().getId().equals(finalAccount.getId()))) {
                    iterator.remove();
                    continue;
                }

                //冻结判断，不处于冻结状态则返回任务
                if (!checkFreeze(account)) {
                    hit = true;
                    break;
                }
            }

            //检查是已经遍历完整个列表
            if (!hit) {
                //没有可用的任务
                return Result.success("没有可用的任务");
            }

            //任务上锁，同时分配强制超时期限
            log.info("任务上锁");
            lockTask(deviceToken, account);

            //记录日志
            log(deviceToken, account, "INFO", "任务开始", "任务开始", null);

            //推送消息
            messageService.push(account, "任务开始", "请勿强行顶号，强行顶号将导致轮空");

            //移出等待队列
            iterator.remove();

            //理智归零
            dynamicInfo.getUserSanList().put(account.getId(), 0);


            return Result.success(AccountConvert.INSTANCE.toAccountDTO(account), "获取成功");

        } else {
            return Result.success("待分配队列为空");
        }
    }

    @Override
    public Result<String> completeTask(String deviceToken, String imageUrl) {
        Result<String> result = new Result<>();


        var account =
                dynamicInfo.getLockTaskList().stream().filter(e -> e.getDeviceToken().equals(deviceToken)).findFirst()
                        .orElseThrow().getAccount();

        //检查B服限制新增设备
        if (account.getServer() == 1 && account.getBLimitDevice().size() == 0) {
            account.getBLimitDevice().add(deviceToken);
            accountMapper.updateById(account);
        }

        //记录日志
        log(deviceToken, account, "INFO", "任务完成", "任务完成", imageUrl);

        //推送消息
        messageService.push(account, "任务完成", "任务完成，可登陆面板查看作战结果");

        //管理员推送消息推送
        if (account.getTaskType().equals("rogue") && enableMail) {
            //发送邮件通知
            String msg = "<p>肉鸽任务已完成<p>\n" +
                    "<p>用户名称: " + account.getName() + "<p>\n" +
                    "<p>用户账号: " + account.getAccount() + "<p>\n" +
                    "<p>服务器: " + account.getServer() + "<p>\n" +
                    "<img src=\"" + imageUrl + "\" alt=\"screenshots\">";
            emailService.sendHtmlMail(to, "肉鸽任务完成", msg);
        }

        //移除队列
        dynamicInfo.getLockTaskList().removeIf(lockTask -> lockTask.getDeviceToken().equals(deviceToken));

        result.setCode(200)
                .setMsg("success")
                .setData("null");

        return result;
    }

    @Override
    public Result<String> failTask(String deviceToken, String type, String imageUrl) {
        Result<String> result = new Result<>();

        var account = dynamicInfo.getLockTaskList().stream().filter(e -> e.getDeviceToken().equals(deviceToken))
                .findFirst()
                .orElseThrow().getAccount();

        //记录日志
        log(deviceToken, account, "WARN", "任务失败", "任务失败,请登陆面板查看失败原因: " + type, imageUrl);

        //移除队列
        dynamicInfo.getLockTaskList().removeIf(lockTask -> lockTask.getDeviceToken().equals(deviceToken));

        //异常处理
        errorHandle(account, deviceToken, type);

        //推送消息
        messageService.push(account, "任务失败", "任务失败，请登陆面板查看失败原因");

        result.setCode(200)
                .setMsg("success")
                .setData("null");

        return result;
    }

    @Override
    public Result<String> tempInsertTask(Long id) {
        Result<String> result = new Result<>();

        dynamicInfo.getFreeTaskList().forEach(account -> {
            if (account.getId().equals(id)) {
                dynamicInfo.getFreeTaskList().remove(account);
                dynamicInfo.getFreeTaskList().add(0, account);
            }
        });

        return result.setCode(200)
                .setMsg("插队成功")
                .setData(null);
    }

    @Override
    public Result<String> tempRemoveTask(Long id) {
        Result<String> result = new Result<>();

        for (AccountEntity account : dynamicInfo.getFreeTaskList()) {
            if (Objects.equals(account.getId(), id)) {
                forceHaltTask(id);
                return result.setCode(200)
                        .setMsg("成功移出队列")
                        .setData(null);
            }
        }

        return result.setCode(404)
                .setMsg("未找到该账号")
                .setData(null);
    }

    @Override
    public Result<String> forceLoadAllTask() {
        dynamicInfo.getFreeTaskList().clear();
        dynamicInfo.getFreeTaskList().addAll(
                accountMapper.selectList(
                        Wrappers.<AccountEntity>lambdaQuery()
                                .eq(AccountEntity::getDelete, 0)
                                .eq(AccountEntity::getFreeze, 0)
                                .eq(AccountEntity::getTaskType, "daily")
                                .ge(AccountEntity::getExpireTime, LocalDateTime.now())
                )
        );

        //记录日志
        logService.logInfo("任务列表刷新", "管理员强制刷新了任务队列");

        return new Result<String>().setCode(200)
                .setMsg("载入成功")
                .setData(null);
    }

    @Override
    public Result<String> forceUnlockOneTask(String deviceToken) {
        if (dynamicInfo.getLockTaskList().removeIf(lockTask -> lockTask.getDeviceToken().equals(deviceToken))) {
            //记录日志
            logService.logInfo("强制解锁", "管理员强制解锁释放了一个任务");

        } else {
            return new Result<String>().setCode(404)
                    .setMsg("未找到任务")
                    .setData(null);
        }

        return new Result<String>().setCode(200)
                .setMsg("解锁成功")
                .setData(null);
    }

    @Override
    public Result<String> forceUnlockTaskList() {
        if (dynamicInfo.getLockTaskList().size() != 0) {
            dynamicInfo.getLockTaskList().clear();

            //记录日志
            logService.logInfo("强制解锁", "管理员强制解锁释放整个上锁队列");

        }
        return Result.success("强制解锁成功");
    }

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
            case "rogue2":
                lockTask.setAccount(account);
                lockTask.setExpirationTime(localDateTime.plusHours(72));
                break;
        }
        dynamicInfo.getLockTaskList().add(lockTask);
    }

    @Override
    public void log(String deviceToken, AccountEntity account, String level, String title,
                    String content, String imgUrl) {
        var addLogDTO = new AddLogDTO();
        String type = "";
        if (Objects.equals(account.getTaskType(), "daily")) {
            type = "每日";
        } else if (Objects.equals(account.getTaskType(), "rogue")) {
            type = "肉鸽";
        }

        String detail =
                "[" + type + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + type +
                        content;

        addLogDTO.setLevel(level)
                .setTaskType(account.getTaskType())
                .setTitle(title)
                .setDetail(detail)
                .setImageUrl(imgUrl)
                .setFrom(deviceToken)
                .setServer(account.getServer())
                .setName(account.getName())
                .setAccount(account.getAccount());

        logService.addLog(addLogDTO, false);
    }

    @Override
    public void errorHandle(AccountEntity account, String deviceToken, String type) {

        switch (type) {
            case ("lineBusy"): {
                dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
                dynamicInfo.getFreeTaskList().add(account);
                break;
            }
            case ("accountError"): {
                if (account.getServer() == 0) {
                    if (httpService.isOfficialAccountWork(account.getAccount(), account.getPassword())) {
                        dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
                        dynamicInfo.getFreeTaskList().add(account);
                    } else {
                        account.setFreeze(1);
                        accountMapper.updateById(account);
                        dynamicInfo.getUserSanList().remove(account.getId());
                        dynamicInfo.getUserMaxSanList().remove(account.getId());
                        messageService.push(account, "账号异常", "您的账号密码有误，请在面板更新正确的账号密码，否则托管将无法继续进行");
                    }
                } else if (account.getServer() == 1) {
                    if (httpService.isBiliAccountWork(account.getAccount(), account.getPassword())) {
                        messageService.push(account, "账号异常", "您近期登陆的设备较多，已被B服限制登陆，请立即修改密码并于面板更新密码,否则托管可能将无法继续进行");
                    } else {
                        account.setFreeze(1);
                        accountMapper.updateById(account);
                        dynamicInfo.getUserSanList().remove(account.getId());
                        dynamicInfo.getUserMaxSanList().remove(account.getId());
                        messageService.push(account, "账号异常", "您的账号密码有误，请在面板更新正确的账号密码，否则托管将无法继续进行");
                    }
                }
            }
            default: {
                dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
                dynamicInfo.getFreeTaskList().add(account);
                break;
            }
        }
    }

    @Override
    public void forceHaltTask(Long id) {
        for (AccountEntity freeTask : dynamicInfo.getFreeTaskList()) {
            if (freeTask.getId().equals(id)) {
                dynamicInfo.getFreeTaskList().remove(freeTask);
                break;
            }
        }
        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(id)) {
                dynamicInfo.getLockTaskList().remove(lockTask);
                dynamicInfo.getHaltList().add(lockTask.getDeviceToken());
                break;
            }
        }
        dynamicInfo.getFreezeTaskList().remove(id);
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
                messageService.push(account, "到期提醒", "您的账号已到期，作战已暂停，若仍需托管请及时续费");
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

                messageService.push(account, "等待分配作战服务器", "您的理智已达到 " + dynamicInfo.getUserSanList().get(id) +
                        "，等待分配作战服务器中，分配完成后将会自动开始作战");

                //归零理智
                dynamicInfo.getUserSanList().put(id, 0);
            }

            //检查是否到达提醒阈值 阈值为最大值-45
            if (dynamicInfo.getUserSanList().get(id) == dynamicInfo.getUserMaxSanList().get(id) - 45) {
                messageService.push(account, "作战预告", "您的账号最快将在30" +
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
