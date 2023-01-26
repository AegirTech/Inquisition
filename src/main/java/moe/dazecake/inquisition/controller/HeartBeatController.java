package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.model.dto.heartbeat.HeartBeatDTO;
import moe.dazecake.inquisition.service.impl.HeartBeatServiceImpl;
import moe.dazecake.inquisition.utils.Result;
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
    private HeartBeatServiceImpl heartBeatService;

    @Operation(summary = "心跳协议")
    @PostMapping("/heartBeat")
    public Result<String> postHeartBeat(@RequestBody HeartBeatDTO heartBeat) {
        return heartBeatService.postHeartBeat(heartBeat);
    }

    @Operation(summary = "完成停机上报")
    @PostMapping("/haltComplete")
    public Result<String> postHaltComplete(@RequestBody HeartBeatDTO heartBeat) {
        return heartBeatService.postHaltComplete(heartBeat);
    }
}
