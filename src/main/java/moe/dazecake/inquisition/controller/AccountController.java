package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;

@Tag(name = "账号接口")
@ResponseBody
@RestController
public class AccountController {

    @Resource
    AccountMapper accountMapper;

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    TaskServiceImpl taskService;

    @Login
    @Operation(summary = "增加账号")
    @PostMapping("/addAccount")
    public Result<String> addAccount(@RequestBody AccountEntity accountEntity) {
        Result<String> result = new Result<>();

        accountMapper.insert(accountEntity);

        result.setCode(200);
        result.setMsg("success");
        result.setData(null);

        return result;
    }


    @Login
    @Operation(summary = "删除账号")
    @PostMapping("/delAccount")
    public Result<String> delAccount(Long id) {
        Result<String> result = new Result<>();

        var account = accountMapper.selectById(id);
        if (account != null) {
            account.setDelete(1);
            accountMapper.updateById(account);

            result.setCode(200);
            result.setMsg("success");

        } else {
            result.setCode(403);
            result.setMsg("Unable to delete a non-existent account");

        }
        result.setData(null);
        return result;

    }

    @Login
    @Operation(summary = "分页查询账号")
    @GetMapping("/showAccount")
    public Result<HashMap<String, Object>> showAccount(Long current, Long size) {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        var data = accountMapper.selectPage(new Page<>(current, size), null);

        var sanMap = new HashMap<String, Object>();
        for (AccountEntity user : data.getRecords()) {
            if (dynamicInfo.getUserSanList().containsKey(user.getId())) {
                sanMap.put(user.getId().toString(),
                        dynamicInfo.getUserSanList().get(user.getId()) + "/" + dynamicInfo.getUserMaxSanList()
                                .get(user.getId()));
            } else {
                sanMap.put(user.getId().toString(), null);
            }
        }

        result.getData().put("records", data.getRecords());
        result.getData().put("sanRecords", sanMap);
        result.getData().put("current", data.getCurrent());
        result.getData().put("totalPages", data.getPages());
        result.getData().put("total", data.getTotal());

        result.setCode(200)
                .setMsg("success");

        return result;
    }

    @Login
    @Operation(summary = "搜索账号")
    @GetMapping("/searchAccount")
    public Result<HashMap<String, Object>> searchAccount(Long current, Long size, String account) {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        var data = accountMapper.selectPage(new Page<>(current, size), Wrappers.<AccountEntity>lambdaQuery()
                .like(AccountEntity::getAccount, account));

        var sanMap = new HashMap<String, Object>();
        for (AccountEntity user : data.getRecords()) {
            if (dynamicInfo.getUserSanList().containsKey(user.getId())) {
                sanMap.put(user.getId().toString(),
                        dynamicInfo.getUserSanList().get(user.getId()) + "/" + dynamicInfo.getUserMaxSanList()
                                .get(user.getId()));
            } else {
                sanMap.put(user.getId().toString(), null);
            }
        }

        result.getData().put("records", data.getRecords());
        result.getData().put("sanRecords", sanMap);
        result.getData().put("current", data.getCurrent());
        result.getData().put("totalPages", data.getPages());
        result.getData().put("total", data.getTotal());

        result.setCode(200)
                .setMsg("success");

        return result;
    }

    @Login
    @Operation(summary = "更新账号")
    @PostMapping("/updateAccount")
    public Result<String> updateAccount(Long id, @RequestBody AccountEntity accountEntity) {
        Result<String> result = new Result<>();

        var account = accountMapper.selectById(id);
        if (account != null) {
            account = accountEntity;
            account.setId(id);
            accountMapper.updateById(account);

            result.setCode(200)
                    .setMsg("success");

        } else {
            result.setCode(403)
                    .setMsg("Unable to update a non-existent account");

        }
        result.setData(null);
        return result;
    }

    @Login
    @Operation(summary = "重置刷新次数")
    @PostMapping("/resetRefresh")
    public Result<String> resetRefresh(Long id) {
        Result<String> result = new Result<>();

        var account = accountMapper.selectById(id);
        if (account != null) {
            account.setRefresh(1);
            accountMapper.updateById(account);

            result.setCode(200)
                    .setMsg("success");

        } else {
            result.setCode(403)
                    .setMsg("Unable to update a non-existent account");

        }
        result.setData(null);
        return result;
    }

    @Login
    @Operation(summary = "修复账号")
    @PostMapping("/fixAccount")
    public Result<String> fixAccount(Long id) {
        var account = accountMapper.selectById(id);
        if (account == null || account.getDelete() == 1 || account.getExpireTime().isBefore(LocalDateTime.now())) {

            return new Result<String>()
                    .setCode(200)
                    .setMsg("success")
                    .setData(null);

        }

        //停止作战
        taskService.forceHaltTask(account, true);

        //重置动态数据
        dynamicInfo.getUserSanList().put(id, 135);
        dynamicInfo.getUserMaxSanList().put(id, 135);

        return new Result<String>()
                .setCode(200)
                .setMsg("success")
                .setData("执行成功");
    }
}
