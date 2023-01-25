package moe.dazecake.inquisition.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.model.dto.TaskDateSet.LockTask;
import moe.dazecake.inquisition.model.entity.AccountEntity;
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

    HashMap<String, String> announcement = new HashMap<>();

    HashMap<String, Integer> deviceStatusMap = new HashMap<>();

    ArrayList<AccountEntity> freeTaskList = new ArrayList<>();

    ArrayList<LockTask> lockTaskList = new ArrayList<>();

    HashMap<Long, LocalDateTime> freezeTaskList = new HashMap<>();

    ArrayList<String> haltList = new ArrayList<>();

    HashMap<String, Integer> counter = new HashMap<>();

    HashMap<Long, Integer> userSanList = new HashMap<>();

    HashMap<Long, Integer> userMaxSanList = new HashMap<>();

    public void load(DynamicInfo dynamicInfo) {
        this.arklightsMD5 = dynamicInfo.arklightsMD5;
        this.arklightsBateMD5 = dynamicInfo.arklightsBateMD5;
        this.announcement = dynamicInfo.announcement;
        this.deviceStatusMap = dynamicInfo.deviceStatusMap;
        this.freeTaskList = dynamicInfo.freeTaskList;
        this.lockTaskList = dynamicInfo.lockTaskList;
        this.freezeTaskList = dynamicInfo.freezeTaskList;
        this.haltList = dynamicInfo.haltList;
        this.counter = dynamicInfo.counter;
        this.userSanList = dynamicInfo.userSanList;
        this.userMaxSanList = dynamicInfo.userMaxSanList;
    }

}
