package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.service.impl.EmailServiceImpl;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "任务接口")
@ResponseBody
@RestController
public class TaskController {

    @Resource
    private DynamicInfo dynamicInfo;

    @Resource
    private LogController logController;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    EmailServiceImpl emailService;

    @Resource
    TaskServiceImpl taskService;

    @Value("${spring.mail.to}")
    String to;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Operation(summary = "获取任务")
    @GetMapping("/getTask")
    public synchronized Result<AccountEntity> getTask(String deviceToken) {
        Result<AccountEntity> result = new Result<>();

        //重复请求检查
        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getDeviceToken().equals(deviceToken)) {
                return result.setCode(201)
                        .setMsg("success")
                        .setData(lockTask.getAccount());
            }
        }

        //任务上锁
        if (!dynamicInfo.getFreeTaskList().isEmpty()) {
            AccountEntity account = new AccountEntity();

            //检查任务是否达到下发标准
            var iterator = dynamicInfo.getFreeTaskList().iterator();
            var hit = false;
            while (iterator.hasNext()) {
                account = iterator.next();

                //时间检查，不在激活区间则跳转到下一个判断
                if (!taskService.checkActivationTime(account)) {
                    iterator.remove();
                    continue;
                }

                //B服限制检查
                if (account.getBLimit() == 1 && !account.getBLimitDevice().contains(deviceToken)) {
                    continue;
                }

                //重复分配任务检查
                AccountEntity finalAccount = account;
                if (dynamicInfo.getLockTaskList().stream()
                        .anyMatch(lockTask -> lockTask.getAccount().getId().equals(finalAccount.getId()))) {
                    iterator.remove();
                    continue;
                }

                //冻结判断，不处于冻结状态则返回任务
                if (!taskService.checkFreeze(account)) {
                    hit = true;
                    break;
                }
            }

            //检查是已经遍历完整个列表
            if (!hit) {
                //没有可用的任务
                return result.setCode(200)
                        .setMsg("没有可用任务")
                        .setData(null);
            }

            //任务上锁，同时分配强制超时期限
            taskService.lockTask(deviceToken, account);

            //记录日志
            taskService.log(deviceToken, account, "INFO", "任务开始", "任务开始", null);

            //推送消息
            taskService.messagePush(account, "任务开始", "请勿强行顶号，强行顶号将导致轮空");

            //移出等待队列
            iterator.remove();

            //理智归零
            dynamicInfo.getUserSanList().put(account.getId(), 0);


            return result.setCode(200)
                    .setMsg("success")
                    .setData(account);

        } else {
            return result.setCode(200)
                    .setMsg("待分配队列为空")
                    .setData(null);
        }
    }

    @Operation(summary = "完成任务上报")
    @PostMapping("/completeTask")
    public Result<String> completeTask(String deviceToken, String imageUrl) {
        Result<String> result = new Result<>();


        var account =
                dynamicInfo.getLockTaskList().stream().filter(e -> e.getDeviceToken().equals(deviceToken)).findFirst()
                        .orElseThrow().getAccount();

        //检查B服限制新增设备
        if (account.getServer() == 1 && account.getBLimit() == 0 && !account.getBLimitDevice().contains(deviceToken)) {
            account.getBLimitDevice().add(deviceToken);
            accountMapper.updateById(account);
        }

        //记录日志
        taskService.log(deviceToken, account, "INFO", "任务完成", "任务完成", imageUrl);

        //推送消息
        taskService.messagePush(account, "任务完成", "任务完成，可登陆面板查看作战结果");

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

    @Operation(summary = "任务失败上报")
    @PostMapping("/failTask")
    public Result<String> failTask(String deviceToken, String type, String imageUrl) {
        Result<String> result = new Result<>();

        var account = dynamicInfo.getLockTaskList().stream().filter(e -> e.getDeviceToken().equals(deviceToken))
                .findFirst()
                .orElseThrow().getAccount();

        //记录日志
        taskService.log(deviceToken, account, "WARN", "任务失败", "任务失败,请登陆面板查看失败原因: " + type, imageUrl);

        //异常处理
        taskService.errorHandle(account, deviceToken, type);

        //推送消息
        taskService.messagePush(account, "任务失败", "任务失败，请登陆面板查看失败原因");

        result.setCode(200)
                .setMsg("success")
                .setData("null");

        return result;
    }

    @Login
    @Operation(summary = "临时插队任务")
    @PostMapping("/tempAddTask")
    public Result<String> tempAddTask(@RequestBody AccountEntity accountEntity) {
        Result<String> result = new Result<>();

        Long minIndex = 0L;

        for (AccountEntity account : dynamicInfo.getFreeTaskList()) {
            if (minIndex > account.getId()) {
                minIndex = account.getId();
            }
        }

        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (minIndex > lockTask.getAccount().getId()) {
                minIndex = lockTask.getAccount().getId();
            }
        }

        minIndex--;

        accountEntity.setId(minIndex);

        dynamicInfo.getFreeTaskList().add(0, accountEntity);

        return result.setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @Login
    @Operation(summary = "临时移除任务")
    @PostMapping("/tempRemoveTask")
    public Result<String> tempRemoveTask(Long id) {
        Result<String> result = new Result<>();

        for (AccountEntity account : dynamicInfo.getFreeTaskList()) {
            if (Objects.equals(account.getId(), id)) {
                taskService.forceHaltTask(account, true);
                return result.setCode(200)
                        .setMsg("success")
                        .setData(null);
            }
        }

        return result.setCode(404)
                .setMsg("not found")
                .setData(null);
    }

    @Login
    @Operation(summary = "临时冻结任务")
    @PostMapping("/tempFreezeTask")
    public Result<String> tempFreezeTask(Long id, String expirationTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(expirationTime, DateTimeFormatter.ofPattern("yyyy-MM-dd" +
                "'T'HH:mm:ss.SSS'Z'"));
        dynamicInfo.getFreeTaskList().forEach(
                accountEntity -> {
                    if (Objects.equals(accountEntity.getId(), id)) {
                        dynamicInfo.getFreezeTaskList().put(id, localDateTime);
                    }
                }
        );
        return new Result<String>()
                .setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @Login
    @Operation(summary = "查询待分配任务列表")
    @GetMapping("/showFreeTaskList")
    public Result<ArrayList<AccountEntity>> showTaskList() {
        Result<ArrayList<AccountEntity>> result = new Result<>();

        return result.setCode(200)
                .setMsg("success")
                .setData(dynamicInfo.getFreeTaskList());
    }

    @Login
    @Operation(summary = "查询已分配任务列表")
    @GetMapping("/showLockTaskList")
    public Result<ArrayList<LockTask>> showLockTaskList() {
        Result<ArrayList<LockTask>> result = new Result<>();
        result.setData(dynamicInfo.getLockTaskList());

        return result.setCode(200)
                .setMsg("success");
    }

    @Login
    @Operation(summary = "查询已冻结任务列表")
    @GetMapping("/showFreezeTaskList")
    public Result<HashMap<Long, LocalDateTime>> showFreezeTaskList() {
        return new Result<HashMap<Long, LocalDateTime>>()
                .setCode(200)
                .setMsg("success")
                .setData(dynamicInfo.getFreezeTaskList());
    }

    @Login
    @Operation(summary = "立即强制从数据库刷新任务")
    @PostMapping("/forceRefreshFreeTaskList")
    public Result<String> forceRefreshFreeTaskList() {
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
        LogEntity logEntity = new LogEntity();

        logEntity.setLevel("INFO")
                .setTaskType("system")
                .setTitle("强制任务列表刷新")
                .setDetail("审判官强制刷新了任务队列")
                .setFrom("system")
                .setTime(LocalDateTime.now());

        logController.addLog(logEntity, "system");

        return new Result<String>().setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @Login
    @Operation(summary = "立即强制释放一设备的上锁任务")
    @PostMapping("/forceUnlockOneTask")
    public Result<String> forceUnlockOneTask(String deviceToken) {
        if (dynamicInfo.getLockTaskList().removeIf(lockTask -> lockTask.getDeviceToken().equals(deviceToken))) {
            //记录日志
            LogEntity logEntity = new LogEntity();
            logEntity.setLevel("INFO")
                    .setTaskType("system")
                    .setTitle("强制解锁")
                    .setDetail("审判官强制解锁释放了一个任务")
                    .setFrom("system")
                    .setTime(LocalDateTime.now());
            logController.addLog(logEntity, "system");

        } else {
            return new Result<String>().setCode(404)
                    .setMsg("not found")
                    .setData(null);
        }

        return new Result<String>().setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @Login
    @Operation(summary = "立即强制释放整个上锁队列")
    @PostMapping("/forceUnlockTaskList")
    public Result<String> forceUnlockTaskList() {
        if (dynamicInfo.getLockTaskList().size() != 0) {
            dynamicInfo.getLockTaskList().clear();

            //记录日志
            LogEntity logEntity = new LogEntity();
            logEntity.setLevel("INFO")
                    .setTaskType("system")
                    .setTitle("强制解锁")
                    .setDetail("审判官强制解锁释放整个上锁队列")
                    .setFrom("system")
                    .setTime(LocalDateTime.now());
            logController.addLog(logEntity, "system");

            return new Result<String>().setCode(200)
                    .setMsg("success")
                    .setData(null);
        } else {
            return new Result<String>().setCode(200)
                    .setMsg("The lock list is empty")
                    .setData(null);
        }
    }

}