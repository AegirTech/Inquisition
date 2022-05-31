package moe.dazecake.arklightscloudbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.entity.AccountConfigEntity;
import moe.dazecake.arklightscloudbackend.entity.HeartBeatEntity;
import moe.dazecake.arklightscloudbackend.entity.TaskEntity;
import moe.dazecake.arklightscloudbackend.service.Impl.UserServiceImpl;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;

@Tag(name = "用户接口")
@ResponseBody
@RestController
public class UserController {

    @Resource
    private UserServiceImpl userService;

    @Operation(summary = "心跳协议")
    @PostMapping("/heartBeat")
    public Result<TaskEntity> postHeartBeat(@RequestBody HeartBeatEntity heartBeat) {
        Result<TaskEntity> result = new Result<>();


        return result;
    }

    @Operation(summary = "通过deviceToken获取所属账户配置")
    @GetMapping("/getDeviceAccountConfig")
    public Result<AccountConfigEntity> getDeviceAccountConfig(String deviceToken) {
        return userService.getDeviceAccountConfig(deviceToken);
    }
}
