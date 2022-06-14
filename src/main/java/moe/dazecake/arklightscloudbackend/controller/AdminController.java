package moe.dazecake.arklightscloudbackend.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.entity.AdminEntity;
import moe.dazecake.arklightscloudbackend.mapper.AdminMapper;
import moe.dazecake.arklightscloudbackend.util.Encoder;
import moe.dazecake.arklightscloudbackend.util.JWTUtils;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

@Tag(name = "管理员接口")
@ResponseBody
@RestController
public class AdminController {

    private static final String salt = "arklightscloud";

    @Resource
    AdminMapper adminMapper;

    @Operation(summary = "管理员登陆")
    @PostMapping("/adminLogin")
    public Result<HashMap<String, String>> adminLogin(String username, String password) {
        Result<HashMap<String, String>> result = new Result<>();
        System.out.println(Encoder.MD5(password + salt));

        if (username == null || password == null) {
            return result.setCode(403)
                    .setMsg("username or password is null")
                    .setData(null);
        }

        var admin = adminMapper.selectOne(
                Wrappers.<AdminEntity>lambdaQuery()
                        .eq(AdminEntity::getUserName, username)
                        .eq(AdminEntity::getPassword, Encoder.MD5(password + salt))
        );

        if (admin != null) {
            return result.setCode(200)
                    .setMsg("login success")
                    .setData(new HashMap<>() {
                        {
                            put("token", JWTUtils.generateToken(admin));
                        }
                    });
        } else {
            return result.setCode(404)
                    .setMsg("Account does not exist")
                    .setData(null);
        }
    }

}
