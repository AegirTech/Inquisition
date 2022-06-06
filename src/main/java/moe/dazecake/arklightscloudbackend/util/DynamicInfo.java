package moe.dazecake.arklightscloudbackend.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.entity.DeviceEntity;
import moe.dazecake.arklightscloudbackend.mapper.AccountMapper;
import moe.dazecake.arklightscloudbackend.mapper.DeviceMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicInfo {

    @Resource
    AccountMapper accountMapper;

    @Resource
    DeviceMapper deviceMapper;

    HashMap<String, Integer> deviceStatusMap = new HashMap<>();

    ArrayList<AccountEntity> freeTaskList = new ArrayList<>();

    HashMap<String, HashMap<AccountEntity, LocalDateTime>> lockTaskList = new HashMap<>();

    HashMap<String, ArrayList<HashMap<Integer, String>>> operateList = new HashMap<>();

    HashMap<String, Integer> counter = new HashMap<>();

    public void initInfo() {
        var devices = deviceMapper.selectList(
                Wrappers.<DeviceEntity>lambdaQuery()
                        .eq(DeviceEntity::getDelete, 0));
        devices.forEach(
                device-> {
                    deviceStatusMap.put(device.getDeviceToken(), 0);
                    counter.put(device.getDeviceToken(), 1);
                }
        );

        freeTaskList = (ArrayList<AccountEntity>) accountMapper.selectList(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getDelete, 0));

    }

}
