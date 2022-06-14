package moe.dazecake.arklightscloudbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.entity.LogEntity;
import moe.dazecake.arklightscloudbackend.util.DynamicInfo;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;

@Tag(name = "任务接口")
@ResponseBody
@RestController
public class TaskController {

    @Resource
    private DynamicInfo dynamicInfo;

    @Resource
    private LogController logController;

    @Operation(summary = "获取任务")
    @GetMapping("/getTask")
    public Result<AccountEntity> getTask(String deviceToken) {
        Result<AccountEntity> result = new Result<>();

        LogEntity logEntity = new LogEntity();
        logEntity.setLevel("INFO");


        //任务上锁
        if (!dynamicInfo.getFreeTaskList().isEmpty()) {
            var account = dynamicInfo.getFreeTaskList().get(0);
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
            result.setData(account);
        } else {
            result.setData(null);
        }

        //记录日志
        logController.addLog(logEntity, deviceToken);

        result.setCode(200)
                .setMsg("success");

        return result;
    }

    @Operation(summary = "完成任务上报")
    @PostMapping("/completeTask")
    public Result<String> completeTask(String deviceToken,String imageUrl) {
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
                .setName(account.getName())
                .setPassword(account.getPassword())
                .setTime(LocalDateTime.now());
        logController.addLog(logEntity, deviceToken);

        //移除队列
        dynamicInfo.getLockTaskList().remove(deviceToken);

        result.setCode(200)
                .setMsg("success")
                .setData("null");

        return result;
    }

    @Operation(summary = "任务失败上报")
    @PostMapping("/failTask")
    public Result<String> failTask(String deviceToken,String imageUrl) {
        Result<String> result = new Result<>();

        var account = dynamicInfo.getLockTaskList().get(deviceToken).keySet().iterator().next();

        //记录日志
        LogEntity logEntity = new LogEntity();
        logEntity.setLevel("WARNING")
                .setTaskType(account.getTaskType())
                .setTitle("任务失败")
                .setDetail("")
                .setImageUrl(imageUrl)
                .setFrom(deviceToken)
                .setName(account.getName())
                .setPassword(account.getPassword())
                .setTime(LocalDateTime.now());
        logController.addLog(logEntity, deviceToken);

        var map = dynamicInfo.getLockTaskList().get(deviceToken);
        map.forEach(
                (accountEntity, localDateTime) -> dynamicInfo.getFreeTaskList().add(accountEntity)
        );
        dynamicInfo.getLockTaskList().remove(deviceToken);

        result.setCode(200)
                .setMsg("success")
                .setData("null");

        return result;
    }
}
