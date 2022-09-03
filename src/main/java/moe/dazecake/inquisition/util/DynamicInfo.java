package moe.dazecake.inquisition.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.AdminEntity;
import moe.dazecake.inquisition.entity.DeviceEntity;
import moe.dazecake.inquisition.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.AdminMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicInfo {

    @Resource
    AccountMapper accountMapper;

    @Resource
    DeviceMapper deviceMapper;

    @Resource
    AdminMapper adminMapper;

    HashMap<String, Integer> deviceStatusMap = new HashMap<>();

    ArrayList<AccountEntity> freeTaskList = new ArrayList<>();

    ArrayList<LockTask> lockTaskList = new ArrayList<>();

    HashMap<Long, LocalDateTime> freezeTaskList = new HashMap<>();

    ArrayList<String> haltList = new ArrayList<>();

    HashMap<String, Integer> counter = new HashMap<>();

    HashMap<Long,Integer> userSanList = new HashMap<>();

    HashMap<Long,Integer> userMaxSanList = new HashMap<>();

    public void initInfo() {

        //检查admin表是否有数据
        List<AdminEntity> adminEntities = adminMapper.selectList(null);
        if (adminEntities.size() == 0) {
            AdminEntity adminEntity = new AdminEntity();
            adminEntity.setUsername("root");
            adminEntity.setPassword("7966fd2201810e386e8407feaf09b4ea");
            adminEntity.setPermission("root");
            adminMapper.insert(adminEntity);
        }

        var devices = deviceMapper.selectList(
                Wrappers.<DeviceEntity>lambdaQuery()
                        .eq(DeviceEntity::getDelete, 0)
                        .ge(DeviceEntity::getExpireTime, LocalDateTime.now())
        );
        devices.forEach(
                device -> {
                    deviceStatusMap.put(device.getDeviceToken(), 0);
                    counter.put(device.getDeviceToken(), 1);
                }
        );
        for (AccountEntity account : accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                .eq(AccountEntity::getDelete, 0)
                .eq(AccountEntity::getFreeze, 0)
                .eq(AccountEntity::getTaskType, "daily")
                .ge(AccountEntity::getExpireTime, LocalDateTime.now())
        )) {
            userSanList.put(account.getId(), 0);
            userMaxSanList.put(account.getId(), 135);
        }

    }

}
