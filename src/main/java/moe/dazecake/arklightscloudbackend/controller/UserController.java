package moe.dazecake.arklightscloudbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.entity.HeartBeatEntity;
import moe.dazecake.arklightscloudbackend.entity.TaskEntity;
import moe.dazecake.arklightscloudbackend.service.Impl.UserServiceImpl;
import moe.dazecake.arklightscloudbackend.util.DynamicInfo;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Objects;

@Tag(name = "用户接口")
@ResponseBody
@RestController
public class UserController {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private DynamicInfo dynamicInfo;

    @Operation(summary = "心跳协议")
    @PostMapping("/heartBeat")
    public Result<TaskEntity> postHeartBeat(@RequestBody HeartBeatEntity heartBeat) {
        Result<TaskEntity> result = new Result<>();

        if (dynamicInfo.getDeviceStatusMap().get(heartBeat.getDeviceToken()) == null) {
            //动态上线
            dynamicInfo.getDeviceStatusMap().put(heartBeat.getDeviceToken(), 1);
        } else {

            //状态更新
            if (!Objects.equals(heartBeat.getStatus(), dynamicInfo.getDeviceStatusMap().get(heartBeat.getDeviceToken()))) {
                dynamicInfo.getDeviceStatusMap().put(heartBeat.getDeviceToken(), heartBeat.getStatus());
            }

            //检查操作队列
            if (!dynamicInfo.getOperateList().get(heartBeat.getDeviceToken()).isEmpty()) {
                result.setCode(201);
                result.setMsg("There are remote operations waiting to be completed");
                //下发操作任务
                dynamicInfo.getOperateList().get(heartBeat.getDeviceToken()).forEach(
                        operateMap -> operateMap.forEach(
                                (code, task) -> {
                                    HashMap<Integer, String> data = new HashMap<>();
                                    data.put(code, task);
                                    result.getData().getList().add(data);
                                }
                        )
                );
                //清空队列
                dynamicInfo.getOperateList().clear();
                return result;
            }

        }

        //无操作心跳
        result.setCode(200);
        result.setMsg("success");
        result.setData(null);
        return result;
    }

    @Operation(summary = "通过deviceToken获取所属账户配置")
    @GetMapping("/getDeviceAccountConfig")
    public Result<AccountEntity> getDeviceAccountConfig(String deviceToken) {
        return userService.getDeviceAccountConfig(deviceToken);
    }
}
