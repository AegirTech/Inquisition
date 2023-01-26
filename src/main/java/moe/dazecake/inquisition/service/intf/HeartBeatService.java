package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.heartbeat.HeartBeatDTO;
import moe.dazecake.inquisition.utils.Result;

public interface HeartBeatService {

    /**
     * 上传心跳
     *
     * @param heartBeat 心跳包
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 16:10
     */
    Result<String> postHeartBeat(HeartBeatDTO heartBeat);

    /**
     * 完成停机上报
     *
     * @param heartBeat 心跳包
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 16:11
     */
    Result<String> postHaltComplete(HeartBeatDTO heartBeat);

}
