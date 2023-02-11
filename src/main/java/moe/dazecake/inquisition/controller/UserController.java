package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.cdk.RawCDKDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.dto.user.*;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.NoticeEntitySet.NoticeEntity;
import moe.dazecake.inquisition.model.entity.NoticeEntitySet.WXUID;
import moe.dazecake.inquisition.model.entity.NoticeEntitySet.WechatCallbackEntity;
import moe.dazecake.inquisition.model.vo.UserLoginVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.impl.UserServiceImpl;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Tag(name = "用户接口")
@ResponseBody
@RestController
public class UserController {

    @Resource
    AccountMapper accountMapper;

    @Value("${wx-pusher.app-token:}")
    String appToken;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;

    @Resource
    UserServiceImpl userService;

    @Operation(summary = "使用CDK创建我的账号")
    @PostMapping("/createUserByCDK")
    public synchronized Result<String> createUserByCDK(@RequestBody CreateUserByCDKDTO createUserByCDKDTO) {
        return userService.createUserByCDK(createUserByCDKDTO.getCdk(),
                createUserByCDKDTO.getUsername(),
                createUserByCDKDTO.getAccount(),
                createUserByCDKDTO.getPassword(),
                createUserByCDKDTO.getServer());
    }

    @Operation(summary = "使用在线支付创建我的账号")
    @PostMapping("/createUserByPay")
    public synchronized Result<String> createUserByPay(@RequestBody CreateUserByPayDTO createUserByPayDTO) {
        return userService.createUserByPay(createUserByPayDTO,
                createUserByPayDTO.getUsername(),
                createUserByPayDTO.getAccount(),
                createUserByPayDTO.getPassword(),
                createUserByPayDTO.getServer());
    }


    @Operation(summary = "登陆我的账号")
    @PostMapping("/userLogin")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return userService.userLogin(userLoginDTO.getAccount(), userLoginDTO.getPassword());
    }

    @UserLogin
    @Operation(summary = "查询自己的账号")
    @GetMapping("/showMyAccount")
    public Result<AccountDTO> showMyAccount(@RequestHeader("Authorization") String token) {
        return userService.showMyAccount(JWTUtils.getId(token));
    }

    @UserLogin
    @Operation(summary = "更新自己的账号")
    @PostMapping("/updateMyAccount")
    public Result<String> updateMyAccount(@RequestHeader("Authorization") String token,
                                          @RequestBody AccountDTO accountDTO) {
        return userService.updateMyAccount(JWTUtils.getId(token), accountDTO);
    }

    @UserLogin
    @Operation(summary = "更新账号密码")
    @PostMapping("/updateAccountAndPassword")
    public Result<String> updateAccountAndPassword(@RequestHeader("Authorization") String token,
                                                   @RequestBody UpdateAccountAndPasswordDTO updateAccountAndPasswordDTO) {
        return userService.updateAccountAndPassword(JWTUtils.getId(token),
                updateAccountAndPasswordDTO.getAccount(),
                updateAccountAndPasswordDTO.getPassword(),
                updateAccountAndPasswordDTO.getServer());
    }

    @UserLogin
    @Operation(summary = "冻结我的账号")
    @PostMapping("/freezeMyAccount")
    public Result<String> freezeMyAccount(@RequestHeader("Authorization") String token) {
        return userService.freezeMyAccount(JWTUtils.getId(token));
    }

    @UserLogin
    @Operation(summary = "解冻我的账号")
    @PostMapping("/unfreezeMyAccount")
    public Result<String> unfreezeMyAccount(@RequestHeader("Authorization") String token) {
        return userService.unfreezeMyAccount(JWTUtils.getId(token));
    }

    @UserLogin
    @Operation(summary = "查询我的日志")
    @GetMapping("/showMyLog")
    public Result<PageQueryVO<LogDTO>> showMyLog(@RequestHeader("Authorization") String token,
                                                 Long current,
                                                 Long size) {
        return userService.showMyLog(JWTUtils.getAccount(token), current, size);
    }

    @UserLogin
    @Operation(summary = "查询我状态")
    @GetMapping("/showMyStatus")
    public Result<UserStatusSTO> showMyStatus(@RequestHeader("Authorization") String token) {
        return userService.showMyStatus(JWTUtils.getId(token));
    }

    @UserLogin
    @Operation(summary = "查询当前理智")
    @GetMapping("/showMySan")
    public Result<String> showMySan(@RequestHeader("Authorization") String token) {
        return userService.showMySan(JWTUtils.getId(token));
    }

    @UserLogin
    @Operation(summary = "使用CDK")
    @PostMapping("/useCDK")
    public synchronized Result<String> useCDK(@RequestHeader("Authorization") String token,
                                              @RequestBody RawCDKDTO rawCDKDTO) {
        return userService.useCDK(JWTUtils.getId(token), rawCDKDTO.getCdk());
    }

    @UserLogin
    @Operation(summary = "获取微信推送二维码")
    @GetMapping("/getWechatQRCode")
    public Result<String> getWechatQRCode(@RequestHeader("Authorization") String token) {
        return userService.getWechatQRCode(JWTUtils.getId(token));
    }

    @Operation(summary = "获取微信推送回调")
    @PostMapping("/getWechatCallback")
    public void getWechatCallback(@RequestBody WechatCallbackEntity wechatCallback) {
        if (enableWxPusher) {
            AccountEntity accountEntity = accountMapper.selectById(wechatCallback.getData().getExtra());
            if (accountEntity != null) {
                var noticeEntity = accountEntity.getNotice();
                if (noticeEntity == null) {
                    noticeEntity = new NoticeEntity();
                }
                if (noticeEntity.getWxUID() == null) {
                    noticeEntity.setWxUID(new WXUID());
                }
                noticeEntity.getWxUID().setText(wechatCallback.getData().getUid());
                noticeEntity.getWxUID().setEnable(true);
                accountEntity.setNotice(noticeEntity);
                accountMapper.updateById(accountEntity);
            }
        }
    }

    @UserLogin
    @Operation(summary = "强制停止作战")
    @PostMapping("/forceHalt")
    public Result<String> forceHalt(@RequestHeader("Authorization") String token) {
        return userService.forceHalt(JWTUtils.getId(token));
    }

    @UserLogin
    @Operation(summary = "立即开始作战")
    @PostMapping("/startNow")
    public Result<String> startNow(@RequestHeader("Authorization") String token) {
        return userService.startNow(JWTUtils.getId(token));
    }

    @UserLogin
    @Operation(summary = "获取刷新次数")
    @GetMapping("/getRefresh")
    public Result<Integer> getRefresh(@RequestHeader("Authorization") String token) {
        return userService.getRefresh(JWTUtils.getId(token));
    }

}
