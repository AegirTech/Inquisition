package moe.dazecake.inquisition.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.model.local.UserSan;
import moe.dazecake.inquisition.model.local.WorkUser;
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

    //======================
    //队列 仅存储用户ID
    //======================
    private ArrayList<Long> waitUserList = new ArrayList<>();
    private ArrayList<Long> workUserList = new ArrayList<>();
    private ArrayList<String> haltList = new ArrayList<>();

    //======================
    //队列关系映射表 用于映射队列关系信息
    //======================
    private HashMap<Long, UserSan> userSanInfoMap = new HashMap<>();
    private HashMap<Long, WorkUser> workUserInfoMap = new HashMap<>();
    private HashMap<Long, LocalDateTime> freezeUserInfoMap = new HashMap<>();


    //======================
    //额外映射表 用于映射其他信息
    //======================

    //设备状态映射表
    private HashMap<String, Integer> deviceStatusMap = new HashMap<>();

    //设备摇篮计数器
    private HashMap<String, Integer> deviceCounterMap = new HashMap<>();

    //公告信息
    private HashMap<String, String> announcement = new HashMap<>();


    //======================
    //方法封装
    //======================

    public void load(DynamicInfo dynamicInfo) {
        this.setWaitUserList(dynamicInfo.getWaitUserList());
        this.setWorkUserList(dynamicInfo.getWorkUserList());
        this.setHaltList(dynamicInfo.getHaltList());
        this.setUserSanInfoMap(dynamicInfo.getUserSanInfoMap());
        this.setWorkUserInfoMap(dynamicInfo.getWorkUserInfoMap());
        this.setFreezeUserInfoMap(dynamicInfo.getFreezeUserInfoMap());
        this.setDeviceStatusMap(dynamicInfo.getDeviceStatusMap());
        this.setDeviceCounterMap(dynamicInfo.getDeviceCounterMap());
        this.setAnnouncement(dynamicInfo.getAnnouncement());
    }

    //获取所有等待队列详细信息
    public ArrayList<AccountEntity> getAllWaitUserInfo() {
        return new ArrayList<>(accountMapper.selectBatchIds(waitUserList));
    }

    //获取所有工作队列详细信息
    public ArrayList<LockTask> getAllWorkUserInfo() {
        var workers = new ArrayList<>(accountMapper.selectBatchIds(workUserList));
        var lockTasks = new ArrayList<LockTask>();
        for (var worker : workers) {
            lockTasks.add(new LockTask(workUserInfoMap.get(worker.getId()).getDeviceToken(),
                    worker,
                    workUserInfoMap.get(worker.getId()).getExpirationTime()));
        }
        return lockTasks;
    }

    //增加work队列
    public void addWorkUser(Long userId, String deviceToken, LocalDateTime expireTime) {
        workUserList.add(userId);
        workUserInfoMap.put(userId, new WorkUser(deviceToken, expireTime));
    }

    //删除work队列
    public void removeWorkUser(Long userId) {
        workUserList.remove(userId);
        workUserInfoMap.remove(userId);
    }

    //获取work过期时间
    public LocalDateTime getWorkUserExpireTime(Long userId) {
        return workUserInfoMap.get(userId).getExpirationTime();
    }

    //通过deviceToken获取userId
    public Long getUserIdByDeviceToken(String deviceToken) {
        return workUserInfoMap.entrySet().stream().filter(e -> e.getValue().getDeviceToken().equals(deviceToken)).findFirst().orElseThrow().getKey();
    }

    //设置用户理智
    public void setUserSan(Long userId, Integer san, Integer maxSan) {
        userSanInfoMap.put(userId, new UserSan(san, maxSan));
    }

    //置空用户理智
    public void setUserSanZero(Long userId) {
        if (userSanInfoMap.get(userId) == null) {
            userSanInfoMap.put(userId, new UserSan(0, 135));
        } else {
            userSanInfoMap.get(userId).setSan(0);
        }
    }

    //增加用户理智
    public void addUserSan(Long userId, Integer san) {
        userSanInfoMap.get(userId).setSan(userSanInfoMap.get(userId).getSan() + san);
    }

}
