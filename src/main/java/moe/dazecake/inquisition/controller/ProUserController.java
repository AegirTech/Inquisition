package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.entity.ProUserEntity;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.util.JWTUtils;
import moe.dazecake.inquisition.util.Result;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

@Tag(name = "高级用户接口")
@ResponseBody
@RestController
public class ProUserController {

    @Resource
    ProUserMapper proUserMapper;

    @Login
    @Operation(summary = "创建高级用户账号")
    @PostMapping("/createProUser")
    public Result<String> createProUser(@RequestBody ProUserEntity proUserEntity) {
        Result<String> result = new Result<>();

        proUserEntity.setId(0L);
        proUserEntity.setDelete(0);

        //rand 8位字符串
        proUserEntity.setAuthorization(RandomStringUtils.randomAlphabetic(16));

        proUserMapper.insert(proUserEntity);

        return result;
    }

    @Operation(summary = "登陆高级用户账号")
    @PostMapping("/proUserLogin")
    public Result<HashMap<String, String>> userLogin(String username, String password) {
        Result<HashMap<String, String>> result = new Result<>();

        if (username == null || password == null) {
            return result.setCode(403)
                    .setMsg("账号或密码为空")
                    .setData(null);
        }

        var account = proUserMapper.selectOne(
                Wrappers.<ProUserEntity>lambdaQuery()
                        .eq(ProUserEntity::getUsername, username)
                        .eq(ProUserEntity::getPassword, password)
        );

        if (account != null) {
            return result.setCode(200)
                    .setMsg("登陆成功")
                    .setData(new HashMap<>() {
                        {
                            put("token", JWTUtils.generateTokenForProUser(account));
                        }
                    });
        } else {
            return result.setCode(404)
                    .setMsg("账号或密码错误")
                    .setData(null);
        }
    }

}
