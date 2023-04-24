package moe.dazecake.inquisition.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.model.local.UserSan;
import moe.dazecake.inquisition.model.local.WorkUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryInfo {

    //======================
    //队列 仅存储用户ID
    //======================
    public ArrayList<Long> waitUserList = new ArrayList<>();
    public List<Long> workUserList = Collections.synchronizedList(new ArrayList<>());
    public ArrayList<String> haltList = new ArrayList<>();

    //======================
    //队列关系映射表 用于映射队列关系信息
    //======================
    public HashMap<Long, UserSan> userSanInfoMap = new HashMap<>();
    public HashMap<Long, WorkUser> workUserInfoMap = new HashMap<>();
    public HashMap<Long, LocalDateTime> freezeUserInfoMap = new HashMap<>();


    //======================
    //额外映射表 用于映射其他信息
    //======================

    //设备状态映射表
    public HashMap<String, Integer> deviceStatusMap = new HashMap<>();

    //设备摇篮计数器
    public HashMap<String, Integer> deviceCounterMap = new HashMap<>();

    //公告信息
    public HashMap<String, String> announcement = new HashMap<>();

}
