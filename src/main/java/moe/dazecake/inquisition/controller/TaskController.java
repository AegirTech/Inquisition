package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.account.AccountIDDTO;
import moe.dazecake.inquisition.model.dto.device.DeviceTokenDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Tag(name = "任务接口")
@ResponseBody
@RestController
public class TaskController {

    @Resource
    private DynamicInfo dynamicInfo;

    @Resource
    TaskServiceImpl taskService;

    @Operation(summary = "获取任务")
    @GetMapping("/getTask")
    public synchronized Result<AccountDTO> getTask(String deviceToken) {
        return taskService.getTask(deviceToken);
    }

    @Operation(summary = "完成任务上报")
    @PostMapping("/completeTask")
    public Result<String> completeTask(String deviceToken, String imageUrl) {
        return taskService.completeTask(deviceToken, imageUrl);
    }

    @Operation(summary = "任务失败上报")
    @PostMapping("/failTask")
    public Result<String> failTask(String deviceToken, String type, String imageUrl) {
        return taskService.failTask(deviceToken, type, imageUrl);
    }

    @Login
    @Operation(summary = "临时插队任务")
    @PostMapping("/tempInsertTask")
    public Result<String> tempInsertTask(@RequestBody AccountIDDTO accountIDDTO) {
        return taskService.tempInsertTask(accountIDDTO.getId());
    }

    @Login
    @Operation(summary = "临时移除任务")
    @PostMapping("/tempRemoveTask")
    public Result<String> tempRemoveTask(@RequestBody AccountIDDTO accountIDDTO) {
        return taskService.tempRemoveTask(accountIDDTO.getId());
    }

    @Login
    @Operation(summary = "查询待分配任务列表")
    @GetMapping("/showFreeTaskList")
    public Result<ArrayList<AccountEntity>> showTaskList() {
        return Result.success(dynamicInfo.getAllWaitUserInfo(), "查询成功");
    }

    @Login
    @Operation(summary = "查询已分配任务列表")
    @GetMapping("/showLockTaskList")
    public Result<ArrayList<LockTask>> showLockTaskList() {
        return Result.success(dynamicInfo.getAllWorkUserInfo(), "查询成功");
    }

    @Login
    @Operation(summary = "查询已冻结任务列表")
    @GetMapping("/showFreezeTaskList")
    public Result<HashMap<Long, LocalDateTime>> showFreezeTaskList() {
        return Result.success(dynamicInfo.getFreezeUserInfoMap(), "查询成功");
    }

    @Login
    @Operation(summary = "立即从数据库重载全部任务")
    @PostMapping("/forceLoadAllTask")
    public Result<String> forceLoadAllTask() {
        return taskService.forceLoadAllTask();
    }

    @Login
    @Operation(summary = "立即强制释放一设备的上锁任务")
    @PostMapping("/forceUnlockOneTask")
    public Result<String> forceUnlockOneTask(@RequestBody DeviceTokenDTO deviceTokenDTO) {
        return taskService.forceUnlockOneTask(deviceTokenDTO.getToken());
    }

    @Login
    @Operation(summary = "立即强制释放整个上锁队列")
    @PostMapping("/forceUnlockTaskList")
    public Result<String> forceUnlockTaskList() {
        return taskService.forceUnlockTaskList();
    }

}