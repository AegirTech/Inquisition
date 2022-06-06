package moe.dazecake.arklightscloudbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.entity.HeartBeatEntity;
import moe.dazecake.arklightscloudbackend.util.DynamicInfo;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import java.util.Objects;

@Tag(name = "心跳接口")
@ResponseBody
@RestController
public class HeartBeatController {

    @Resource
    private DynamicInfo dynamicInfo;

    @Operation(summary = "心跳协议")
    @PostMapping("/heartBeat")
    public Result<String> postHeartBeat(@RequestBody HeartBeatEntity heartBeat) {
        Result<String> result = new Result<>();

        //状态更新
        dynamicInfo.getCounter().put(heartBeat.getDeviceToken(), 3);
        dynamicInfo.getDeviceStatusMap().put(heartBeat.getDeviceToken(), heartBeat.getStatus());


        if (dynamicInfo.getFreeTaskList().isEmpty()) {
            result.setCode(200);
        } else {
            result.setCode(201);
        }

        result.setMsg("success");
        result.setData(null);
        return result;
    }
}
