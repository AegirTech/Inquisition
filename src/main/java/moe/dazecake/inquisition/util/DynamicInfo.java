package moe.dazecake.inquisition.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.TaskDateSet.LockTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicInfo {

    String arklightsMD5;

    String arklightsBateMD5;

    HashMap<String, Integer> deviceStatusMap = new HashMap<>();

    ArrayList<AccountEntity> freeTaskList = new ArrayList<>();

    ArrayList<LockTask> lockTaskList = new ArrayList<>();

    HashMap<Long, LocalDateTime> freezeTaskList = new HashMap<>();

    ArrayList<String> haltList = new ArrayList<>();

    HashMap<String, Integer> counter = new HashMap<>();

    HashMap<Long,Integer> userSanList = new HashMap<>();

    HashMap<Long,Integer> userMaxSanList = new HashMap<>();

    public void load(DynamicInfo dynamicInfo) {
        this.setCounter(dynamicInfo.getCounter());
        this.setDeviceStatusMap(dynamicInfo.getDeviceStatusMap());
        this.setFreeTaskList(dynamicInfo.getFreeTaskList());
        this.setFreezeTaskList(dynamicInfo.getFreezeTaskList());
        this.setHaltList(dynamicInfo.getHaltList());
        this.setLockTaskList(dynamicInfo.getLockTaskList());
        this.setUserMaxSanList(dynamicInfo.getUserMaxSanList());
        this.setUserSanList(dynamicInfo.getUserSanList());
    }

}
