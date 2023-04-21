package moe.dazecake.inquisition.service.impl;

import moe.dazecake.inquisition.model.dto.heartbeat.HeartBeatDTO;
import moe.dazecake.inquisition.service.intf.HeartBeatService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HeartBeatServiceImpl implements HeartBeatService {

    @Resource
    DynamicInfo dynamicInfo;

    @Override
    public Result<String> postHeartBeat(HeartBeatDTO heartBeat) {
        Result<String> result = new Result<>();
        //状态更新
        dynamicInfo.getDeviceCounterMap().put(heartBeat.getDeviceToken(), 3);
        dynamicInfo.getDeviceStatusMap().put(heartBeat.getDeviceToken(), heartBeat.getStatus());


        if (dynamicInfo.getWaitUserList().isEmpty()) {
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

    @Override
    public Result<String> postHaltComplete(HeartBeatDTO heartBeat) {
        Result<String> result = new Result<>();

        //移除所有停机列表
        while (dynamicInfo.getHaltList().contains(heartBeat.getDeviceToken())) {
            dynamicInfo.getHaltList().remove(heartBeat.getDeviceToken());
        }

        return result.setCode(200).setMsg("success");
    }
}
