package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.service.impl.MessageServiceImpl;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Encoder;
import moe.dazecake.inquisition.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

@Tag(name = "公告接口")
@ResponseBody
@RestController
public class NoticeController {

    @Resource
    AccountMapper accountMapper;

    @Resource
    MessageServiceImpl messageService;

    @Resource
    DynamicInfo dynamicInfo;

    @Login
    @Operation(summary = "向单个用户发送一条广播")
    @PostMapping("/sendMessageToUser")
    public Result<String> sendMessageToUser(Long id, String title, String content) {

        var account = accountMapper.selectById(id);
        if (account != null) {
            messageService.push(account, title, content);
            return new Result<String>().setCode(200).setMsg("发送成功");
        } else {
            return new Result<String>().setCode(403).setMsg("用户不存在");
        }

    }

    @Login
    @Operation(summary = "创建一条公告")
    @PostMapping("/createAnnouncement")
    public Result<String> createAnnouncement(String title, String context) {
        Result<String> result = new Result<>();

        dynamicInfo.getAnnouncement().put("title", title);
        dynamicInfo.getAnnouncement().put("context", context);
        dynamicInfo.getAnnouncement().put("md5", Encoder.MD5(context));

        return result.setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @Operation(summary = "获取公告")
    @GetMapping("/getAnnouncement")
    public Result<HashMap<String, String>> getAnnouncement() {
        Result<HashMap<String, String>> result = new Result<>();

        return result.setCode(200)
                .setMsg("success")
                .setData(dynamicInfo.getAnnouncement());
    }

}
