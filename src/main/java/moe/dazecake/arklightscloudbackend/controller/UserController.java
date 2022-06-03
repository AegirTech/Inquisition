package moe.dazecake.arklightscloudbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.entity.HeartBeatEntity;
import moe.dazecake.arklightscloudbackend.entity.TaskEntity;
import moe.dazecake.arklightscloudbackend.service.Impl.UserServiceImpl;
import moe.dazecake.arklightscloudbackend.util.DynamicInfo;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Tag(name = "用户接口")
@ResponseBody
@RestController
public class UserController {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private DynamicInfo dynamicInfo;

    @Operation(summary = "心跳协议")
    @PostMapping("/heartBeat")
    public Result<TaskEntity> postHeartBeat(@RequestBody HeartBeatEntity heartBeat) {
        Result<TaskEntity> result = new Result<>();

        if (!dynamicInfo.getDeviceStatusMap().containsKey(heartBeat.getDeviceToken())) {
            //动态上线
            dynamicInfo.getDeviceStatusMap().put(heartBeat.getDeviceToken(), 1);
        } else {

            //状态更新
            if (!Objects.equals(heartBeat.getStatus(), dynamicInfo.getDeviceStatusMap().get(heartBeat.getDeviceToken()))) {
                dynamicInfo.getDeviceStatusMap().put(heartBeat.getDeviceToken(), heartBeat.getStatus());
            }

            //检查操作队列
            if (dynamicInfo.getOperateList().containsKey(heartBeat.getDeviceToken())) {
                result.setCode(201);
                result.setMsg("There are remote operations waiting to be completed");
                //下发操作任务
                dynamicInfo.getOperateList().get(heartBeat.getDeviceToken()).forEach(
                        operateList -> operateList.forEach(
                                (code, task) -> {
                                    HashMap<Integer, String> data = new HashMap<>();
                                    data.put(code, task);
                                    TaskEntity taskEntity = new TaskEntity();
                                    taskEntity.setList(new ArrayList<>(){
                                        {
                                            add(data);
                                        }
                                    });
                                    result.setData(taskEntity);
                                }
                        )
                );
                //清空队列
                dynamicInfo.getOperateList().clear();
                return result;
            }

        }

        //无操作心跳
        result.setCode(200);
        result.setMsg("success");
        result.setData(null);
        return result;
    }

    @Operation(summary = "通过deviceToken获取所属账户配置")
    @GetMapping("/getDeviceAccountConfig")
    public Result<AccountEntity> getDeviceAccountConfig(String deviceToken) {
        Result<AccountEntity> result = new Result<>();
        var taskList = dynamicInfo.getTaskList().get(deviceToken);

        result.setCode(200);
        result.setMsg("success");
        result.setData(taskList.get(0));

        return result;
    }

    @Operation(summary = "完成任务上报")
    @PostMapping("/completeAccountTask")
    public Result<String> completeAccountTask(String deviceToken, Long id){
        Result<String> result = new Result<>();

        //移除任务队列
        for (int i = 0; i < dynamicInfo.getTaskList().get(deviceToken).size(); i++) {
            if (Objects.equals(dynamicInfo.getTaskList().get(deviceToken).get(i).getId(), id)) {
                dynamicInfo.getTaskList().get(deviceToken).remove(dynamicInfo.getTaskList().get(deviceToken).get(i));
                break;
            }
        }

        result.setCode(200);
        result.setMsg("success");
        result.setData("null");

        return result;
    }
}
