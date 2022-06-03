package moe.dazecake.arklightscloudbackend.service;

import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.util.Result;

public interface UserService {
    Result<AccountEntity> getDeviceAccountConfig(String deviceToken);
}
