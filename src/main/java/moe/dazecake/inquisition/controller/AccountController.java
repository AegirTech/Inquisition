package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.util.JWTUtils;
import moe.dazecake.inquisition.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@Tag(name = "账号接口")
@ResponseBody
@RestController
public class AccountController {

    @Resource
    AccountMapper accountMapper;

    @Operation(summary = "创建我的账号")
    @PostMapping("/createAccount")
    public Result<String> createAccount(String cdk, @RequestBody AccountEntity accountEntity) {
        Result<String> result = new Result<>();

        // TODO: 2022/7/15 判断cdk是否有效

        accountEntity.setId(0L);
        accountMapper.insert(accountEntity);
        return result.setCode(200).setMsg("success").setData(null);
    }

    @Operation(summary = "登陆我的账号")
    @PostMapping("/userLogin")
    public Result<HashMap<String, String>> userLogin(String username, String password) {
        Result<HashMap<String, String>> result = new Result<>();

        if (username == null || password == null) {
            return result.setCode(403)
                    .setMsg("username or password is null")
                    .setData(null);
        }

        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAccount, username)
                        .eq(AccountEntity::getPassword, password)
        );

        if (account != null) {
            return result.setCode(200)
                    .setMsg("login success")
                    .setData(new HashMap<>() {
                        {
                            put("token", JWTUtils.generateTokenForUser(account));
                        }
                    });
        } else {
            return result.setCode(404)
                    .setMsg("Account does not exist")
                    .setData(null);
        }
    }


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
    public Result<ArrayList<AccountEntity>> showAccount(Long current, Long size) {
        Result<ArrayList<AccountEntity>> result = new Result<>();
        result.setData(new ArrayList<>());

        var data = accountMapper.selectPage(new Page<>(current, size), null);
        result.setCode(200)
                .setMsg("success")
                .getData()
                .addAll(data.getRecords());

        return result;
    }

    @UserLogin
    @Operation(summary = "查询自己的账号")
    @GetMapping("/showMyAccount")
    public Result<AccountEntity> showMyAccount(@RequestHeader("Authorization") String token) {
        Result<AccountEntity> result = new Result<>();
        result.setData(new AccountEntity());

        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, JWTUtils.getId(token))
        );
        result.setCode(200)
                .setMsg("success")
                .setData(account);

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

    @UserLogin
    @Operation(summary = "更新自己的账号")
    @PostMapping("/updateMyAccount")
    public Result<String> updateMyAccount(@RequestHeader("Authorization") String token,
                                          @RequestBody AccountEntity accountEntity) {
        Result<String> result = new Result<>();

        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, JWTUtils.getId(token))
        );
        if (account != null) {
            LocalDateTime expireTime = account.getExpireTime();
            account = accountEntity;
            account.setId(JWTUtils.getId(token));
            account.setExpireTime(expireTime);

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
}
