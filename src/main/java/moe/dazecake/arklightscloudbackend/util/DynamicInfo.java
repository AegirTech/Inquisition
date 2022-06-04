package moe.dazecake.arklightscloudbackend.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.mapper.AccountMapper;
import moe.dazecake.arklightscloudbackend.mapper.DeviceMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    HashMap<String, ArrayList<AccountEntity>> taskList = new HashMap<>();

    HashMap<String, ArrayList<HashMap<Integer, String>>> operateList = new HashMap<>();

    public void initInfo() {
        var devices = deviceMapper.selectList(null);
        devices.forEach(
                device -> deviceStatusMap.put(device.getDeviceToken(), device.getStatus())
        );

        var tasks = accountMapper.selectList(null);
        tasks.forEach(
                task -> {
                    if (taskList.containsKey(task.getBelong())) {
                        taskList.get(task.getBelong()).add(task);
                    } else {
                        taskList.put(task.getBelong(), new ArrayList<>() {
                            {
                                add(task);
                            }
                        });
                    }
                }
        );
    }

}
