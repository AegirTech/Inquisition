package moe.dazecake.arklightscloudbackend.service;

import moe.dazecake.arklightscloudbackend.entity.AccountConfigEntity;
import moe.dazecake.arklightscloudbackend.util.Result;

public interface UserService {
    Result<AccountConfigEntity> getDeviceAccountConfig(String deviceToken);
}
