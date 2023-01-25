package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.ProUserLogin;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.CDKMapper;
import moe.dazecake.inquisition.mapper.LogMapper;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.model.dto.TaskDateSet.LockTask;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.CDKEntity;
import moe.dazecake.inquisition.model.entity.LogEntity;
import moe.dazecake.inquisition.model.entity.ProUserEntity;
import moe.dazecake.inquisition.service.impl.CDKServiceImpl;
import moe.dazecake.inquisition.service.impl.HttpServiceImpl;
import moe.dazecake.inquisition.service.impl.TaskServiceImpl;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Encoder;
import moe.dazecake.inquisition.util.JWTUtils;
import moe.dazecake.inquisition.util.Result;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Tag(name = "高级用户接口")
@ResponseBody
@RestController
public class ProUserController {

    @Resource
    ProUserMapper proUserMapper;

    @Resource
    AccountMapper accountMapper;

    @Resource
    HttpServiceImpl httpService;

    @Resource
    CDKServiceImpl cdkService;

    @Resource
    CDKMapper cdkMapper;

    @Resource
    LogMapper logMapper;

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    TaskServiceImpl taskService;

    @Value("${inquisition.price.daily:1.0}")
    private Double dailyPrice;

    @Value("${inquisition.price.rogue_1:40.0}")
    private Double rogue1Price;

    @Value("${inquisition.price.rogue_2:40.0}")
    private Double rogue2Price;

    private static final String salt = "arklightspro";

    @Login
    @Operation(summary = "创建高级用户账号")
    @PostMapping("/createProUser")
    public Result<String> createProUser(@RequestBody ProUserEntity proUserEntity) {
        Result<String> result = new Result<>();

        proUserEntity.setId(0L);
        proUserEntity.setDelete(0);
        proUserEntity.setPassword(Encoder.MD5(proUserEntity.getPassword() + salt));

        //rand 8位字符串
        proUserEntity.setAuthorization(RandomStringUtils.randomAlphabetic(16));

        proUserMapper.insert(proUserEntity);

        return result;
    }

    @Operation(summary = "登陆高级用户账号")
    @PostMapping("/proUserLogin")
    public Result<HashMap<String, String>> proUserLogin(String username, String password) {
        Result<HashMap<String, String>> result = new Result<>();

        if (username == null || password == null) {
            return result.setCode(403)
                    .setMsg("账号或密码为空")
                    .setData(null);
        }

        var account = proUserMapper.selectOne(
                Wrappers.<ProUserEntity>lambdaQuery()
                        .eq(ProUserEntity::getUsername, username)
                        .eq(ProUserEntity::getPassword, Encoder.MD5(password + salt))
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

    @ProUserLogin
    @Operation(summary = "获取高级用户信息")
    @GetMapping("/getProUserInfo")
    public Result<ProUserEntity> getProUserInfo(@RequestHeader("Authorization") String token) {
        Result<ProUserEntity> result = new Result<>();

        var proUserEntity = proUserMapper.selectById(JWTUtils.getId(token));

        return result.setCode(200)
                .setMsg("success")
                .setData(proUserEntity);
    }

    @ProUserLogin
    @Operation(summary = "修改高级用户密码")
    @PostMapping("/updateProUserPassword")
    public Result<String> updateProUserPassword(@RequestHeader("Authorization") String token,
                                                @RequestParam String oldPassword,
                                                @RequestParam String newPassword) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);

        var old = proUserMapper.selectById(id);

        if (Encoder.MD5(oldPassword + salt).equals(old.getPassword() + salt)) {
            old.setPassword(Encoder.MD5(newPassword + salt));
            proUserMapper.updateById(old);
            return result.setCode(200)
                    .setMsg("success")
                    .setData("修改成功");
        } else {
            return result.setCode(403)
                    .setMsg("原密码错误")
                    .setData("原密码错误");
        }
    }

    @ProUserLogin
    @Operation(summary = "分页显示代理商的附属用户")
    @GetMapping("/getSubUserList")
    public Result<HashMap<String, Object>> getSubUserList(@RequestHeader("Authorization") String token,
                                                          @RequestParam Integer page,
                                                          @RequestParam Integer size) {

        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        var id = JWTUtils.getId(token);

        var data = accountMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAgent, id)
        );

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

        return result.setCode(200)
                .setMsg("success");
    }

    @ProUserLogin
    @Operation(summary = "搜索附属用户")
    @GetMapping("/searchSubUser")
    public Result<HashMap<String, Object>> searchSubUser(@RequestHeader("Authorization") String token,
                                                         @RequestParam Integer page,
                                                         @RequestParam Integer size,
                                                         @RequestParam String account) {

        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        var id = JWTUtils.getId(token);

        var data = accountMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAgent, id)
                        .like(AccountEntity::getAccount, account)
        );

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

        return result.setCode(200)
                .setMsg("success");
    }

    @ProUserLogin
    @Operation(summary = "代理商配置附属用户设置")
    @PostMapping("/setSubUser")
    public Result<String> setSubUser(@RequestHeader("Authorization") String token,
                                     @RequestParam Long userId,
                                     @RequestBody AccountEntity accountEntity) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);

        var oldAccountEntity = accountMapper.selectById(userId);

        if (oldAccountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData("用户不存在");
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(oldAccountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData("无权限");
        }

        //校验新账号密码
        if (!Objects.equals(oldAccountEntity.getAccount(), accountEntity.getAccount()) || !Objects.equals(oldAccountEntity.getPassword(), accountEntity.getPassword())) {
            if (accountEntity.getServer() == 0) {
                if (!httpService.isOfficialAccountWork(accountEntity.getAccount(), accountEntity.getPassword())) {
                    return result.setCode(403)
                            .setMsg("账号或密码错误")
                            .setData("账号或密码错误");
                }
            } else {
                if (!httpService.isBiliAccountWork(accountEntity.getAccount(), accountEntity.getPassword())) {
                    return result.setCode(403)
                            .setMsg("账号或密码错误")
                            .setData("账号或密码错误");
                }
            }
        }

        //只读数据迁移
        accountEntity.setId(oldAccountEntity.getId());
        accountEntity.setExpireTime(oldAccountEntity.getExpireTime());
        accountEntity.setAgent(id);
        accountEntity.setUpdateTime(LocalDateTime.now());

        //冻结设置
        if (!Objects.equals(oldAccountEntity.getFreeze(), accountEntity.getFreeze())) {
            if (accountEntity.getFreeze() == 1) {
                //冻结
                dynamicInfo.getUserSanList().remove(accountEntity.getId());
            } else {
                //解冻
                accountEntity.setFreeze(0);
                if (dynamicInfo.getUserMaxSanList().containsKey(accountEntity.getId())) {
                    dynamicInfo.getUserSanList().put(accountEntity.getId(),
                            dynamicInfo.getUserMaxSanList().get(accountEntity.getId()) - 20);
                } else {
                    dynamicInfo.getUserSanList().put(accountEntity.getId(), 80);
                    dynamicInfo.getUserMaxSanList().put(accountEntity.getId(), 100);
                }
            }
        }


        return result.setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @ProUserLogin
    @Operation(summary = "显示代理商附属用户日志")
    @GetMapping("/getSubUserLog")
    public Result<HashMap<String, Object>> getSubUserLog(@RequestHeader("Authorization") String token,
                                                         @RequestParam Long userId,
                                                         @RequestParam Integer page,
                                                         @RequestParam Integer size) {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        var data = logMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<LogEntity>lambdaQuery()
                        .eq(LogEntity::getAccount, accountEntity.getAccount())
                        .orderByDesc(LogEntity::getId)
        );

        result.getData().put("records", data.getRecords());
        result.getData().put("current", data.getCurrent());
        result.getData().put("totalPages", data.getPages());
        result.getData().put("total", data.getTotal());

        return result.setCode(200)
                .setMsg("success");
    }

    @ProUserLogin
    @Operation(summary = "显示代理商附属用户状态")
    @GetMapping("/getSubUserStatus")
    public Result<HashMap<String, Object>> getSubUserStatus(@RequestHeader("Authorization") String token,
                                                            @RequestParam Long userId) {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        //状态表
        if (accountEntity.getFreeze() == 1) {
            result.getData().put("freeze", 1);
            result.getData().put("san", 0);
            result.getData().put("maxSan", 0);
        } else {
            result.getData().put("freeze", 0);
            result.getData().put("san", dynamicInfo.getUserSanList().get(accountEntity.getId()));
            result.getData().put("maxSan", dynamicInfo.getUserMaxSanList().get(accountEntity.getId()));
        }

        //冻结情况
        if (dynamicInfo.getFreezeTaskList().containsKey(accountEntity.getId())) {
            result.getData().put("freezeTask", dynamicInfo.getFreezeTaskList().get(accountEntity.getId()));
        } else {
            result.getData().put("freezeTask", 0);
        }

        //作战情况
        result.getData().put("fighting", false);
        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(accountEntity.getId())) {
                result.getData().put("fighting", true);
                break;
            }
        }

        //排队情况
        result.getData().put("queue", -1);
        int index = 0;
        for (AccountEntity account : dynamicInfo.getFreeTaskList()) {
            if (Objects.equals(account.getId(), accountEntity.getId())) {
                result.getData().put("queue", index);
                break;
            }
            index++;
        }

        return result.setCode(200)
                .setMsg("success");
    }

    @ProUserLogin
    @Operation(summary = "刷新附属用户立即作战次数")
    @PostMapping("/refreshSubUserFight")
    public Result<String> refreshSubUserFight(@RequestHeader("Authorization") String token,
                                              @RequestParam Long userId) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        //刷新
        accountEntity.setRefresh(1);
        accountEntity.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(accountEntity);

        return result.setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @ProUserLogin
    @Operation(summary = "强制附属用户立即作战")
    @PostMapping("/forceSubUserFight")
    public Result<String> forceSubUserFight(@RequestHeader("Authorization") String token,
                                            @RequestParam Long userId) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        //作战
        for (AccountEntity freeTask : dynamicInfo.getFreeTaskList()) {
            if (freeTask.getId().equals(userId)) {
                return result.setCode(200).setMsg("已经在排队中");
            }
        }

        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(userId)) {
                return result.setCode(200).setMsg("已经在作战中");
            }
        }

        dynamicInfo.getFreeTaskList().add(0, accountEntity);
        dynamicInfo.getUserSanList().put(userId, 0);

        return result.setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @ProUserLogin
    @Operation(summary = "强制附属用户立即停止作战")
    @PostMapping("/forceSubUserStop")
    public Result<String> forceSubUserStop(@RequestHeader("Authorization") String token,
                                           @RequestParam Long userId) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        //停止作战
        taskService.forceHaltTask(accountEntity, true);

        return result.setCode(200).setMsg("success").setData("执行成功");
    }

    @ProUserLogin
    @Operation(summary = "修复附属用户账号问题")
    @PostMapping("/fixSubUser")
    public Result<String> repairSubUser(@RequestHeader("Authorization") String token,
                                        @RequestParam Long userId) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        //停止作战
        taskService.forceHaltTask(accountEntity, true);

        //重置动态数据
        dynamicInfo.getUserSanList().put(userId, 135);
        dynamicInfo.getUserMaxSanList().put(userId, 135);

        return result.setCode(200).setMsg("success").setData("执行成功");
    }

    @ProUserLogin
    @Operation(summary = "为附属用户激活CDK")
    @PostMapping("/activateSubUserCdk")
    public Result<String> activateSubUserCdk(@RequestHeader("Authorization") String token,
                                             @RequestParam Long userId,
                                             @RequestParam String cdk) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        var code = cdkService.activateCDK(userId, cdk);

        if (code == 200) {
            result.setCode(200).setMsg("success").setData("激活成功");
        } else if (code == 404) {
            result.setCode(404).setMsg("CDK不存在或已被激活");
        }
        return result;
    }

    @ProUserLogin
    @Operation(summary = "显示pro_user库存CDK")
    @GetMapping("/getProUserInventoryCdk")
    public Result<ArrayList<CDKEntity>> getProUserInventoryCdk(@RequestHeader("Authorization") String token) {
        Result<ArrayList<CDKEntity>> result = new Result<>();

        var id = JWTUtils.getId(token);

        var cdkList =
                cdkMapper.selectList(Wrappers.<CDKEntity>lambdaQuery().eq(CDKEntity::getAgent, id)
                        .eq(CDKEntity::getUsed, 0));

        return result.setCode(200)
                .setMsg("success")
                .setData((ArrayList<CDKEntity>) cdkList);
    }

    @ProUserLogin
    @Operation(summary = "pro_user扣除余额创建cdk")
    @PostMapping("/createCdkByProUser")
    public Result<ArrayList<CDKEntity>> createCdkByProUser(@RequestHeader("Authorization") String token,
                                                           @RequestParam String type,
                                                           @RequestParam String tag,
                                                           @RequestParam Integer param,
                                                           @RequestParam Integer num) {
        Result<ArrayList<CDKEntity>> result = new Result<>();

        var id = JWTUtils.getId(token);

        var proUser = proUserMapper.selectById(id);

        if (proUser == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        if (num <= 0) {
            return result.setCode(400)
                    .setMsg("数量必须大于0")
                    .setData(null);
        }

        //判断类型
        switch (type) {
            case "daily":
                if (proUser.getBalance() < num * param * dailyPrice * proUser.getDiscount()) {
                    return result.setCode(403)
                            .setMsg("余额不足")
                            .setData(null);
                } else {
                    proUser.setBalance(proUser.getBalance() - num * param * dailyPrice * proUser.getDiscount());
                    proUserMapper.updateById(proUser);
                    ArrayList<CDKEntity> newCDKList = new ArrayList<>();
                    for (int i = 0; i < num; i++) {
                        newCDKList.add(CDKEntity.builder()
                                .id(0L)
                                .cdk(RandomStringUtils.randomAlphabetic(32))
                                .type(type)
                                .param(param)
                                .tag(tag)
                                .isAgent(1)
                                .agent(id)
                                .used(0)
                                .build());
                    }
                    newCDKList.forEach(cdkMapper::insert);
                    return result.setCode(200)
                            .setMsg("success")
                            .setData(newCDKList);
                }
            case "rogue_1":
                if (proUser.getBalance() < num * rogue1Price * proUser.getDiscount()) {
                    return result.setCode(403)
                            .setMsg("余额不足")
                            .setData(null);
                }
                //适配魁影肉鸽
                break;
            case "rogue_2":
                if (proUser.getBalance() < num * rogue2Price * proUser.getDiscount()) {
                    return result.setCode(403)
                            .setMsg("余额不足")
                            .setData(null);
                }
                //适配水月肉鸽
                break;
            default:
                return result.setCode(403)
                        .setMsg("类型错误")
                        .setData(null);

        }
        return result;
    }

    @ProUserLogin
    @Operation(summary = "pro_user手动续期附属用户daily时长")
    @PostMapping("/renewSubUserDaily")
    public Result<String> renewSubUserDaily(@RequestHeader("Authorization") String token,
                                            @RequestParam Long userId,
                                            @RequestParam Integer param) {
        Result<String> result = new Result<>();

        if (param <= 0) {
            return result.setCode(400)
                    .setMsg("数量必须大于0")
                    .setData(null);
        }

        var id = JWTUtils.getId(token);

        var accountEntity = accountMapper.selectById(userId);

        if (accountEntity == null) {
            return result.setCode(404)
                    .setMsg("用户不存在")
                    .setData(null);
        }

        //判断是否是代理商的附属用户
        if (!Objects.equals(accountEntity.getAgent(), id)) {
            return result.setCode(403)
                    .setMsg("无权限")
                    .setData(null);
        }

        var proUser = proUserMapper.selectById(id);

        if (proUser == null) {
            return result.setCode(404)
                    .setMsg("pro用户不存在")
                    .setData(null);
        }

        if (proUser.getBalance() < param * dailyPrice * proUser.getDiscount()) {
            return result.setCode(403)
                    .setMsg("余额不足")
                    .setData(null);
        }

        proUser.setBalance(proUser.getBalance() - param * dailyPrice * proUser.getDiscount());
        proUserMapper.updateById(proUser);

        if (accountEntity.getExpireTime().isBefore(LocalDateTime.now())) {
            accountEntity.setExpireTime(LocalDateTime.now().plusDays(param));
        } else {
            accountEntity.setExpireTime(accountEntity.getExpireTime().plusDays(param));
        }
        accountEntity.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(accountEntity);

        return result.setCode(200)
                .setMsg("success")
                .setData(null);
    }
}
