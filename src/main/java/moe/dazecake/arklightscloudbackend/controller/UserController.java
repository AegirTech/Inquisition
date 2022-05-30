package moe.dazecake.arklightscloudbackend.controller;

import moe.dazecake.arklightscloudbackend.entity.AccountConfigEntity;
import moe.dazecake.arklightscloudbackend.service.Impl.UserServiceImpl;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;

public class UserController {

    @Resource
    private UserServiceImpl userService;

    /**
     * 通过deviceToken获取所属账户配置
     *
     * @param deviceToken 设备token
     * @return moe.dazecake.arklightscloudbackend.util.Result<moe.dazecake.arklightscloudbackend.entity.AccountConfigEntity>
     * @author DazeCake
     * @date 2022/5/30 21:29
     */
    @GetMapping("/getDeviceAccountConfig")
    public Result<AccountConfigEntity> getDeviceAccountConfig(String deviceToken) {
        return userService.getDeviceAccountConfig(deviceToken);
    }
}
