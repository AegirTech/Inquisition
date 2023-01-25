package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.model.vo.HeartBeatVO;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "心跳接口")
@ResponseBody
@RestController
public class HeartBeatController {

    @Resource
    private DynamicInfo dynamicInfo;

    @Operation(summary = "心跳协议")
    @PostMapping("/heartBeat")
    public Result<String> postHeartBeat(@RequestBody HeartBeatVO heartBeat) {
        Result<String> result = new Result<>();

        //状态更新
        dynamicInfo.getCounter().put(heartBeat.getDeviceToken(), 3);
        dynamicInfo.getDeviceStatusMap().put(heartBeat.getDeviceToken(), heartBeat.getStatus());


        if (dynamicInfo.getFreeTaskList().isEmpty()) {
            result.setCode(200);
        } else {
            result.setCode(201);
        }

        //停机检查
        if (dynamicInfo.getHaltList().contains(heartBeat.getDeviceToken())) {
            result.setCode(500);
        }

        return result.setMsg("success");
    }

    @Operation(summary = "完成停机上报")
    @PostMapping("/haltComplete")
    public Result<String> postHaltComplete(@RequestBody HeartBeatVO heartBeat) {
        Result<String> result = new Result<>();

        //移除所有停机列表
        while (dynamicInfo.getHaltList().contains(heartBeat.getDeviceToken())) {
            dynamicInfo.getHaltList().remove(heartBeat.getDeviceToken());
        }

        return result.setCode(200).setMsg("success");
    }
}
