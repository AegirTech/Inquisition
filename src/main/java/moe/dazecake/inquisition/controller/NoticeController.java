package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import moe.dazecake.inquisition.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "公告接口")
@ResponseBody
@RestController
public class NoticeController {

    @Resource
    AccountMapper accountMapper;

    @Resource
    TaskServiceImpl taskService;

    @Operation(summary = "向所有用户发送一条广播")
    @PostMapping("/sendMessageToAllUsers")
    public Result<String> sendMessageToAllUsers(String title, String content) {

        accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery().eq(AccountEntity::getDelete, 0))
                .forEach(accountEntity -> taskService.messagePush(accountEntity, title, content));

        return new Result<String>().setCode(200).setMsg("发送成功");

    }

    @Operation(summary = "向单个用户发送一条广播")
    @PostMapping("/sendMessageToUser")
    public Result<String> sendMessageToUser(Long id, String title, String content) {

        var account = accountMapper.selectById(id);
        if (account != null) {
            taskService.messagePush(account, title, content);
            return new Result<String>().setCode(200).setMsg("发送成功");
        } else {
            return new Result<String>().setCode(403).setMsg("用户不存在");
        }

    }

    
}
