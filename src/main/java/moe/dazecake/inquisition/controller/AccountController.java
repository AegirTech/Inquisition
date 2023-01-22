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
import org.springframework.format.annotation.DateTimeFormat;
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
    public Result<String> addAccount(String username, String account, String password, Long server,
                                     @RequestParam
                                     @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                                     LocalDateTime expireTime) {
        Result<String> result = new Result<>();

        var accountEntity = new AccountEntity();
        accountEntity.setName(username)
                .setAccount(account)
                .setPassword(password)
                .setServer(server)
                .setExpireTime(expireTime);

        accountMapper.insert(accountEntity);

        result.setCode(200);
        result.setMsg("success");
        result.setData(null);

        return result;
    }

    @Login
    @Operation(summary = "从速通迁移账号")
    @PostMapping("/transferAccountFromArkLights")
    public Result<String> transferAccountFromArkLights(@RequestBody HashMap<String, String> accountJson) {
        Result<String> result = new Result<>();
        var num = 0;

        for (int i = 1; i <= 30; i++) {
            if (accountJson.containsKey("username" + i) && accountJson.containsKey("password" + i)) {
                var account = new AccountEntity();

                //导入账号密码
                if (accountJson.get("username" + i).contains("#")) {
                    var parts = accountJson.get("username" + i).split("#");
                    account.setName(parts[1]);
                    account.setAccount(parts[0]);
                } else {
                    account.setName(accountJson.get("username" + i));
                    account.setAccount(accountJson.get("username" + i));
                }
                account.setPassword(accountJson.get("password" + i));
                if (accountJson.containsKey("server" + i)) {
                    account.setServer(Long.valueOf(accountJson.get("server" + i)));
                } else {
                    account.setServer(0L);
                }

                accountMapper.insert(account);
                num++;
            }
        }

        return result.setCode(200)
                .setMsg("success")
                .setData("已添加" + num + "个账号");
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
            account.setUpdateTime(LocalDateTime.now());
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
            account.setUpdateTime(LocalDateTime.now());
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
    @Operation(summary = "账号立即作战")
    @PostMapping("/startAccountByAdmin")
    public Result<String> startAccountByAdmin(Long id) {
        Result<String> result = new Result<>();

        var account = accountMapper.selectById(id);
        if (account != null) {
            accountMapper.updateById(account);
            dynamicInfo.getFreeTaskList().add(0, account);
            dynamicInfo.getUserSanList().put(id, 0);

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
