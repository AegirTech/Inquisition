package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.model.dto.notice.CreateAnnounceDTO;
import moe.dazecake.inquisition.service.impl.NoticeServiceImpl;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

@Tag(name = "公告接口")
@ResponseBody
@RestController
public class NoticeController {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    NoticeServiceImpl noticeService;

    @Login
    @Operation(summary = "创建一条公告")
    @PostMapping("/createAnnouncement")
    public Result<String> createAnnouncement(@RequestBody CreateAnnounceDTO createAnnounceDTO) {
        noticeService.createAnnouncement(createAnnounceDTO);
        return Result.success("创建成功");
    }

    @Operation(summary = "获取公告")
    @GetMapping("/getAnnouncement")
    public Result<HashMap<String, String>> getAnnouncement() {
        return Result.success(dynamicInfo.getAnnouncement(), "获取成功");
    }

}
