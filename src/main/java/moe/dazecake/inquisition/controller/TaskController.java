package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.service.impl.EmailServiceImpl;
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

    @Value("${spring.mail.to}")
    String to;

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Operation(summary = "获取任务")
    @GetMapping("/getTask")
    public Result<AccountEntity> getTask(String deviceToken) {
        Result<AccountEntity> result = new Result<>();

        if (dynamicInfo.getLockTaskList().containsKey(deviceToken)) {
            return result.setCode(201)
                    .setMsg("success")
                    .setData(dynamicInfo.getLockTaskList().get(deviceToken).keySet().iterator().next());
        }

        LogEntity logEntity = new LogEntity();
        logEntity.setLevel("INFO");


        //任务上锁
        if (!dynamicInfo.getFreeTaskList().isEmpty()) {
            AccountEntity account = new AccountEntity();
            int i;

            //检查任务是否达到下发标准
            for (i = 0; i < dynamicInfo.getFreeTaskList().size(); i++) {
                account = dynamicInfo.getFreeTaskList().get(i);

                //激活检查
                int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                boolean accessFlag = true;

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
                                        accessFlag = false;
                                        break;
                                    }
                                }
                            }

                        } else {
                            accessFlag = false;
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
                                        accessFlag = false;
                                        break;
                                    }
                                }
                            }

                        } else {
                            accessFlag = false;
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
                                        accessFlag = false;
                                        break;
                                    }
                                }
                            }

                        } else {
                            accessFlag = false;
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
                                        accessFlag = false;
                                        break;
                                    }
                                }
                            }

                        } else {
                            accessFlag = false;
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
                                        accessFlag = false;
                                        break;
                                    }
                                }
                            }

                        } else {
                            accessFlag = false;
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
                                        accessFlag = false;
                                        break;
                                    }
                                }
                            }

                        } else {
                            accessFlag = false;
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
                                        accessFlag = false;
                                        break;
                                    }
                                }
                            }

                        } else {
                            accessFlag = false;
                        }
                        break;
                    default:
                        return new Result<AccountEntity>()
                                .setCode(500)
                                .setMsg("未知错误")
                                .setData(null);

                }

                if (!accessFlag) {
                    continue;
                }

                //冻结检查
                if (dynamicInfo.getFreezeTaskList().containsKey(account.getId())) {

                    //检测是否结束冻结
                    if (dynamicInfo.getFreezeTaskList().get(account.getId()).isBefore(LocalDateTime.now())) {
                        dynamicInfo.getFreezeTaskList().remove(account.getId());
                        break;
                    }

                } else {
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

            dynamicInfo.getFreeTaskList().remove(account);

            //设置分配主机和超时时间
            LocalDateTime localDateTime = LocalDateTime.now();
            HashMap<AccountEntity, LocalDateTime> accountEntityLocalDateTimeHashMap = new HashMap<>();
            switch (account.getTaskType()) {
                case "daily":
                    accountEntityLocalDateTimeHashMap.put(account, localDateTime.plusHours(2));

                    logEntity.setTaskType("daily")
                            .setTitle("任务开始")
                            .setDetail("")//序列化配置
                            .setFrom(deviceToken)
                            .setServer(account.getServer())
                            .setName(account.getName())
                            .setPassword(account.getPassword())
                            .setTime(localDateTime);
                    break;
                case "rogue":
                    accountEntityLocalDateTimeHashMap.put(account, localDateTime.plusHours(48));

                    logEntity.setTaskType("rogue")
                            .setTitle("任务开始")
                            .setDetail("")//序列化配置
                            .setFrom(deviceToken)
                            .setName(account.getName())
                            .setPassword(account.getPassword())
                            .setTime(localDateTime);

                    break;
            }

            dynamicInfo.getLockTaskList().put(deviceToken, accountEntityLocalDateTimeHashMap);

            //记录日志
            logController.addLog(logEntity, deviceToken);

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