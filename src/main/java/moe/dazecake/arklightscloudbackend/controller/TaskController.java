package moe.dazecake.arklightscloudbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
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

    @Operation(summary = "获取任务")
    @GetMapping("/getDeviceAccountConfig")
    public Result<AccountEntity> getDeviceAccountConfig(String deviceToken) {
        Result<AccountEntity> result = new Result<>();
        result.setCode(200).setMsg("success");

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
                    break;
                case "rogue":
                    accountEntityLocalDateTimeHashMap.put(account, localDateTime.plusHours(48));
                    break;
            }

            dynamicInfo.getLockTaskList().put(deviceToken, accountEntityLocalDateTimeHashMap);
            result.setData(account);
        } else {
            result.setData(null);
        }

        return result;
    }

    @Operation(summary = "完成任务上报")
    @PostMapping("/completeAccountTask")
    public Result<String> completeAccountTask(String deviceToken) {
        Result<String> result = new Result<>();

        //移除队列
        dynamicInfo.getLockTaskList().remove(deviceToken);

        result.setCode(200);
        result.setMsg("success");
        result.setData("null");

        return result;
    }
}
