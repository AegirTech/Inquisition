package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.ProUserLogin;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.account.AccountIDDTO;
import moe.dazecake.inquisition.model.dto.cdk.ActiveAccountCDKDTO;
import moe.dazecake.inquisition.model.dto.cdk.CreateCDKDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.dto.pay.ProUserRenewalSubUserDTO;
import moe.dazecake.inquisition.model.dto.prouser.*;
import moe.dazecake.inquisition.model.vo.account.AccountWithSanVO;
import moe.dazecake.inquisition.model.vo.cdk.CDKListVO;
import moe.dazecake.inquisition.model.vo.prouser.ProUserLoginVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.impl.ProUserServiceImpl;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;

@Tag(name = "高级用户接口")
@ResponseBody
@RestController
public class ProUserController {

    @Resource
    ProUserServiceImpl proUserService;

    @Login
    @Operation(summary = "创建高级用户账号")
    @PostMapping("/createProUser")
    public Result<String> createProUser(@RequestBody CreateProUserDTO createProUserDTO) {
        proUserService.CreateProUser(createProUserDTO);
        return Result.success("创建成功");
    }

    @Login
    @Operation(summary = "分页查询高级用户账号")
    @GetMapping("/getAllProUser")
    public Result<PageQueryVO<ProUserDTO>> getAllProUser(Long current, Long size) {
        return proUserService.getAllProUser(current, size);
    }

    @Login
    @Operation(summary = "更新高级用户账号")
    @PostMapping("/updateProUser")
    public Result<String> updateProUser(@RequestBody ProUserDTO proUserDTO) {
        return proUserService.updateProUser(proUserDTO);
    }

    @Operation(summary = "登陆高级用户账号")
    @PostMapping("/proUserLogin")
    public Result<ProUserLoginVO> proUserLogin(@RequestBody ProUserLoginDTO proUserLoginDTO) {
        return proUserService.loginProUser(proUserLoginDTO);
    }

    @ProUserLogin
    @Operation(summary = "获取高级用户信息")
    @GetMapping("/getProUserInfo")
    public Result<ProUserDTO> getProUserInfo(@RequestHeader("Authorization") String token) {
        return proUserService.getProUserInfo(JWTUtils.getId(token));
    }

    @ProUserLogin
    @Operation(summary = "修改高级用户密码")
    @PostMapping("/updateProUserPassword")
    public Result<String> updateProUserPassword(@RequestHeader("Authorization") String token,
                                                @RequestBody UpdateProUserPasswordDTO updateProUserPasswordDTO) {
        return proUserService.updateProUserPassword(JWTUtils.getId(token), updateProUserPasswordDTO);
    }

    @ProUserLogin
    @Operation(summary = "获取近期到期的附属用户")
    @GetMapping("/getRecentlyExpiredUsers")
    public Result<ArrayList<AccountDTO>> getRecentlyExpiredUsers(@RequestHeader("Authorization") String token) {
        return proUserService.getRecentlyExpiredUsers(JWTUtils.getId(token));
    }

    @ProUserLogin
    @Operation(summary = "分页显示代理商的附属用户")
    @GetMapping("/getSubUserList")
    public Result<PageQueryVO<AccountWithSanVO>> getSubUserList(@RequestHeader("Authorization") String token,
                                                                @RequestParam String type,
                                                                @RequestParam Integer current,
                                                                @RequestParam Integer size) {
        return proUserService.queryAllSubUser(JWTUtils.getId(token), type, current, size);
    }

    @ProUserLogin
    @Operation(summary = "通过账号搜索附属用户")
    @GetMapping("/searchSubUserByAccount")
    public Result<PageQueryVO<AccountWithSanVO>> searchSubUser(@RequestHeader("Authorization") String token,
                                                               @RequestParam Integer page,
                                                               @RequestParam Integer size,
                                                               @RequestParam String keyword) {
        return proUserService.querySubUserByAccount(JWTUtils.getId(token), page, size, keyword);
    }

    @ProUserLogin
    @Operation(summary = "代理商配置附属用户设置")
    @PostMapping("/setSubUser")
    public Result<String> setSubUser(@RequestHeader("Authorization") String token,
                                     @RequestBody AccountDTO accountDTO) {
        proUserService.updateSubAccount(JWTUtils.getId(token), accountDTO);
        return Result.success("修改成功");
    }

    @ProUserLogin
    @Operation(summary = "显示代理商附属用户日志")
    @GetMapping("/getSubUserLog")
    public Result<PageQueryVO<LogDTO>> getSubUserLog(@RequestHeader("Authorization") String token,
                                                     @RequestParam Long userId,
                                                     @RequestParam Integer page,
                                                     @RequestParam Integer size) {
        return proUserService.querySubUserLogByAccount(JWTUtils.getId(token), userId, page, size);
    }

    @ProUserLogin
    @Operation(summary = "强制附属用户立即作战")
    @PostMapping("/forceSubUserFight")
    public Result<String> forceSubUserFight(@RequestHeader("Authorization") String token,
                                            @RequestBody AccountIDDTO accountIDDTO) {
        return proUserService.forceFightSubUser(JWTUtils.getId(token), accountIDDTO.getId());
    }

    @ProUserLogin
    @Operation(summary = "强制附属用户立即停止作战")
    @PostMapping("/forceSubUserStop")
    public Result<String> forceSubUserStop(@RequestHeader("Authorization") String token,
                                           @RequestBody AccountIDDTO accountIDDTO) {
        return proUserService.forceStopSubUser(JWTUtils.getId(token), accountIDDTO.getId());
    }

    @ProUserLogin
    @Operation(summary = "为附属用户激活CDK")
    @PostMapping("/activateSubUserCdk")
    public Result<String> activateSubUserCdk(@RequestBody ActiveAccountCDKDTO activeAccountCDKDTO) {
        return proUserService.activateSubUserCdk(activeAccountCDKDTO.getId(), activeAccountCDKDTO.getCdk());
    }

    @ProUserLogin
    @Operation(summary = "显示pro_user库存CDK")
    @GetMapping("/getProUserInventoryCdk")
    public Result<CDKListVO> getProUserInventoryCdk(@RequestHeader("Authorization") String token) {
        return proUserService.queryProUserCDKList(JWTUtils.getId(token));
    }

    @ProUserLogin
    @Operation(summary = "pro_user扣除余额创建cdk")
    @PostMapping("/createCdkByProUser")
    public Result<String> createCdkByProUser(@RequestHeader("Authorization") String token,
                                             @RequestBody CreateCDKDTO createCDKDTO) {
        return proUserService.createCdkByProUser(JWTUtils.getId(token), createCDKDTO);
    }

    @ProUserLogin
    @Operation(summary = "pro_user扣除余额创建用户")
    @PostMapping("/createSubUserByProUser")
    public Result<String> createSubUserByProUser(@RequestHeader("Authorization") String token,
                                                 @RequestBody CreateUserByProUserDTO createUserByProUserDTO) {
        return proUserService.createSubUserByProUser(
                JWTUtils.getId(token),
                createUserByProUserDTO.getName(),
                createUserByProUserDTO.getAccount(),
                createUserByProUserDTO.getPassword(),
                createUserByProUserDTO.getServer(),
                createUserByProUserDTO.getDays()
        );
    }

    @ProUserLogin
    @Operation(summary = "pro_user手动续期附属用户daily时长")
    @PostMapping("/renewSubUserDaily")
    public Result<String> renewSubUserDaily(@RequestHeader("Authorization") String token,
                                            @RequestBody ProUserRenewalSubUserDTO proUserRenewalSubUserDTO) {
        return proUserService.renewSubUserDaily(JWTUtils.getId(token), proUserRenewalSubUserDTO.getId(), proUserRenewalSubUserDTO.getMo());
    }
}
