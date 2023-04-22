package moe.dazecake.inquisition.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.model.local.UserSan;
import moe.dazecake.inquisition.model.local.WorkUser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicInfo extends MemoryInfo {

    @Resource
    AccountMapper accountMapper;

    //======================
    //方法封装
    //======================

    public void load(MemoryInfo memoryInfo) {
        this.setWaitUserList(memoryInfo.getWaitUserList());
        this.setWorkUserList(memoryInfo.getWorkUserList());
        this.setHaltList(memoryInfo.getHaltList());
        this.setUserSanInfoMap(memoryInfo.getUserSanInfoMap());
        this.setWorkUserInfoMap(memoryInfo.getWorkUserInfoMap());
        this.setFreezeUserInfoMap(memoryInfo.getFreezeUserInfoMap());
        this.setDeviceStatusMap(memoryInfo.getDeviceStatusMap());
        this.setDeviceCounterMap(memoryInfo.getDeviceCounterMap());
        this.setAnnouncement(memoryInfo.getAnnouncement());
    }

    public MemoryInfo dump() {
        return new MemoryInfo(
                this.getWaitUserList(),
                this.getWorkUserList(),
                this.getHaltList(),
                this.getUserSanInfoMap(),
                this.getWorkUserInfoMap(),
                this.getFreezeUserInfoMap(),
                this.getDeviceStatusMap(),
                this.getDeviceCounterMap(),
                this.getAnnouncement());
    }

    //获取所有等待队列详细信息
    public ArrayList<AccountEntity> getAllWaitUserInfo() {
        if (this.waitUserList.size() != 0) {
            return new ArrayList<>(accountMapper.selectBatchIds(waitUserList));
        } else {
            return new ArrayList<>();
        }
    }

    //获取所有工作队列详细信息
    public ArrayList<LockTask> getAllWorkUserInfo() {
        if (workUserList.size() != 0) {
            var workers = new ArrayList<>(accountMapper.selectBatchIds(workUserList));
            var lockTasks = new ArrayList<LockTask>();
            for (var worker : workers) {
                lockTasks.add(new LockTask(workUserInfoMap.get(worker.getId()).getDeviceToken(),
                        worker,
                        workUserInfoMap.get(worker.getId()).getExpirationTime()));
            }
            return lockTasks;
        } else {
            return new ArrayList<>();
        }

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
        for (Long id : workUserList) {
            if (workUserInfoMap.get(id).getDeviceToken().equals(deviceToken)) {
                return id;
            }
        }
        return null;
    }

    //设置用户理智
    public void setUserSan(Long userId, Integer san, Integer maxSan) {
        userSanInfoMap.put(userId, new UserSan(san, maxSan));
    }

    //置空用户理智
    public void setUserSanZero(Long userId) {
        if (!userSanInfoMap.containsKey(userId)) {
            userSanInfoMap.put(userId, new UserSan(0, 135));
        } else {
            userSanInfoMap.get(userId).setSan(0);
        }
    }

    //增加用户理智
    public void addUserSan(Long userId, Integer san) {
        if (userSanInfoMap.containsKey(userId)) {
            userSanInfoMap.get(userId).setSan(userSanInfoMap.get(userId).getSan() + san);
        } else {
            log.warn("【审判庭】 存在未知用户的理智增加请求，用户ID：" + userId);
            setUserSanZero(userId);
        }
    }

}
