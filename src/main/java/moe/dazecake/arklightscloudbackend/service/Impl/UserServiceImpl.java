package moe.dazecake.arklightscloudbackend.service.Impl;

import moe.dazecake.arklightscloudbackend.entity.AccountConfigEntity;
import moe.dazecake.arklightscloudbackend.service.UserService;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public Result<AccountConfigEntity> getDeviceAccountConfig(String deviceToken) {
        return null;
    }
}
