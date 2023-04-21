package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "理智接口")
@ResponseBody
@RestController
public class SanController {
    @Resource
    private DynamicInfo dynamicInfo;

    @Operation(summary = "理智上报")
    @PostMapping("/sanReport")
    public Result<String> SanReport(Integer san, Integer maxSan, String deviceToken) {
        Result<String> result = new Result<>();
        var id = dynamicInfo.getUserIdByDeviceToken(deviceToken);
        dynamicInfo.setUserSan(id, san, maxSan);
        return result.setCode(200).setMsg("success");
    }
}
