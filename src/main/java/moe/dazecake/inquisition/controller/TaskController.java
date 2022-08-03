package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zjiecode.wxpusher.client.bean.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.service.impl.EmailServiceImpl;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import moe.dazecake.inquisition.service.impl.WXPusherServiceImpl;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Result;
import moe.dazecake.inquisition.util.TimeUtil;
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
    WXPusherServiceImpl wxPusherService;

    @Resource
    TaskServiceImpl taskService;

    @Value("${spring.mail.to}")
    String to;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${wx-pusher.enable}")
    boolean enableWxPusher;

    @Operation(summary = "获取任务")
    @GetMapping("/getTask")
    public Result<AccountEntity> getTask(String deviceToken) {
        Result<AccountEntity> result = new Result<>();

        //重复请求检查
        if (dynamicInfo.getLockTaskList().containsKey(deviceToken)) {
            return result.setCode(201)
                    .setMsg("success")
                    .setData(dynamicInfo.getLockTaskList().get(deviceToken).keySet().iterator().next());
        }

        //任务上锁
        if (!dynamicInfo.getFreeTaskList().isEmpty()) {
            AccountEntity account = new AccountEntity();
            int i;

            //检查任务是否达到下发标准
            for (i = 0; i < dynamicInfo.getFreeTaskList().size(); i++) {
                account = dynamicInfo.getFreeTaskList().get(i);

                //时间检查，不在激活区间则跳转到下一个判断
                if (!taskService.checkActivationTime(account)) {
                    continue;
                }

                //冻结判断，不处于冻结状态则返回任务
                if (!taskService.checkFreeze(account)) {
                    break;
                }
            }

            //检查是已经遍历完整个列表
            if (i == dynamicInfo.getFreeTaskList().size()) {
                //没有可用的任务
                return result.setCode(200)
                        .setMsg("success")
                        .setData(null);
            }

            //移出等待队列
            dynamicInfo.getFreeTaskList().remove(account);

            //任务上锁，同时分配强制超时期限
            taskService.lockTask(deviceToken, account);

            //记录日志
            taskService.log(deviceToken, account);

            //推送消息
            taskService.messagePush(account);

            return result.setCode(200)
                    .setMsg("success")
                    .setData(account);

        } else {
            return result.setCode(200)
                    .setMsg("success")
                    .setData(null);
        }
    }

    @Operation(summary = "完成任务上报")
    @PostMapping("/completeTask")
    public Result<String> completeTask(String deviceToken, String imageUrl) {
        Result<String> result = new Result<>();


        var account = dynamicInfo.getLockTaskList().get(deviceToken).keySet().iterator().next();

        //记录日志
        LogEntity logEntity = new LogEntity();
        logEntity.setLevel("INFO")
                .setTaskType(account.getTaskType())
                .setTitle("任务完成")
                .setDetail("")
                .setImageUrl(imageUrl)
                .setFrom(deviceToken)
                .setServer(account.getServer())
                .setName(account.getName())
                .setPassword(account.getPassword())
                .setTime(LocalDateTime.now());
        logController.addLog(logEntity, deviceToken);

        //微信推送
        if (enableWxPusher && account.getNotice().getWxUID().getEnable()) {
            wxPusherService.push(Message.CONTENT_TYPE_MD,
                    "# 作战完成\n\n" +
                            "可登陆控制面板查看作战详情",
                    account.getNotice().getWxUID().getText(),
                    null);
        }

        //邮件推送
        if (enableMail && account.getNotice().getMail().getEnable()) {
            emailService.sendSimpleMail(account.getNotice().getMail().getText(), "作战完成",
                    "可登陆控制面板查看作战详情");
        }

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
        dynamicInfo.getLockTaskList().remove(deviceToken);

        result.setCode(200)
                .setMsg("success")
                .setData("null");

        return result;
    }

    @Operation(summary = "任务失败上报")
    @PostMapping("/failTask")
    public Result<String> failTask(String deviceToken, String type, String imageUrl) {
        Result<String> result = new Result<>();

        var account = dynamicInfo.getLockTaskList().get(deviceToken).keySet().iterator().next();

        //记录日志
        LogEntity logEntity = new LogEntity();
        logEntity.setLevel("WARN")
                .setTaskType(account.getTaskType())
                .setTitle("任务失败")
                .setDetail("")
                .setImageUrl(imageUrl)
                .setFrom(deviceToken)
                .setServer(account.getServer())
                .setName(account.getName())
                .setPassword(account.getPassword())
                .setTime(LocalDateTime.now());

        logController.addLog(logEntity, deviceToken);

        var map = dynamicInfo.getLockTaskList().get(deviceToken);
        map.forEach(
                (accountEntity, localDateTime) -> dynamicInfo.getFreeTaskList().add(accountEntity)
        );
        dynamicInfo.getLockTaskList().remove(deviceToken);

        if (account.getTaskType().equals("rogue") && type.equals("lineBusy")) {
            dynamicInfo.getFreezeTaskList().put(account.getId(), LocalDateTime.now().plusHours(1));
        }

        //微信推送
        if (enableWxPusher && account.getNotice().getWxUID().getEnable()) {
            wxPusherService.push(Message.CONTENT_TYPE_MD,
                    "# 作战失败\n\n" +
                            "可登陆控制面板查看作战详情",
                    account.getNotice().getWxUID().getText(),
                    null);
        }

        //邮件推送
        if (enableMail && account.getNotice().getMail().getEnable()) {
            emailService.sendSimpleMail(account.getNotice().getMail().getText(), "作战失败",
                    "可登陆控制面板查看作战详情");
        }

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

        Iterator<AccountEntity> iterator = dynamicInfo.getFreeTaskList().iterator();

        Long minIndex = 0L;

        while (iterator.hasNext()) {
            AccountEntity account = iterator.next();
            if (minIndex > account.getId()) {
                minIndex = account.getId();
            }
        }

        minIndex--;

        accountEntity.setId(minIndex);

        dynamicInfo.getFreeTaskList().add(0, accountEntity);

        return result;
    }

    @Login
    @Operation(summary = "临时移除任务")
    @PostMapping("/tempRemoveTask")
    public Result<String> tempRemoveTask(Long id) {
        Result<String> result = new Result<>();

        var iterator = dynamicInfo.getFreeTaskList().iterator();
        while (iterator.hasNext()) {
            var account = iterator.next();
            if (Objects.equals(account.getId(), id)) {
                iterator.remove();
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
    public Result<HashMap<String, HashMap<String, Object>>> showLockTaskList() {
        Result<HashMap<String, HashMap<String, Object>>> result = new Result<>();
        result.setData(new HashMap<>());
        dynamicInfo.getLockTaskList().forEach(
                (deviceToken, infoMap) -> infoMap.forEach(
                        (accountEntity, localDateTime) -> {
                            result.getData().put(deviceToken, new HashMap<>());
                            result.getData().get(deviceToken).put("account", accountEntity);
                            result.getData().get(deviceToken).put("time", localDateTime);
                        }
                )
        );

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
        if (dynamicInfo.getLockTaskList().get(deviceToken) != null) {
            dynamicInfo.getFreeTaskList()
                    .add(dynamicInfo.getLockTaskList().get(deviceToken).keySet().iterator().next());
            dynamicInfo.getLockTaskList().remove(deviceToken);

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
            dynamicInfo.getLockTaskList().forEach(
                    (deviceToken, map) -> dynamicInfo.getFreeTaskList().add(map.keySet().iterator().next()
                    )
            );
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