package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjiecode.wxpusher.client.WxPusher;
import com.zjiecode.wxpusher.client.bean.CreateQrcodeReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.LogEntity;
import moe.dazecake.inquisition.entity.NoticeEntitySet.NoticeEntity;
import moe.dazecake.inquisition.entity.NoticeEntitySet.WXUID;
import moe.dazecake.inquisition.entity.NoticeEntitySet.WechatCallbackEntity;
import moe.dazecake.inquisition.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.BillMapper;
import moe.dazecake.inquisition.mapper.LogMapper;
import moe.dazecake.inquisition.service.impl.CDKServiceImpl;
import moe.dazecake.inquisition.service.impl.HttpServiceImpl;
import moe.dazecake.inquisition.service.impl.PayServiceImpl;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.JWTUtils;
import moe.dazecake.inquisition.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Tag(name = "用户接口")
@ResponseBody
@RestController
public class UserController {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    AccountMapper accountMapper;

    @Resource
    LogMapper logMapper;

    @Resource
    HttpServiceImpl httpService;

    @Resource
    CDKServiceImpl cdkService;

    @Resource
    PayServiceImpl payService;

    @Resource
    BillMapper billMapper;

    @Value("${wx-pusher.app-token:}")
    String appToken;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;

    @Operation(summary = "使用CDK创建我的账号")
    @PostMapping("/createUserByCDK")
    public Result<String> createUserByCDK(String cdk, String username, String account, String password, Integer server) {
        Result<String> result = new Result<>();

        if (accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                .eq(AccountEntity::getAccount, account)).size() != 0) {
            result.setCode(10001);
            result.setMsg("账号已存在，请直接登录");
            return result;
        }

        if (server == 0) {
            if (!httpService.isOfficialAccountWork(account, password)) {
                return result.setCode(10002).setMsg("账号或密码错误，注册需要填写的账号就是游戏账号！");
            }
        } else if (server == 1) {
            if (!httpService.isBiliAccountWork(account, password)) {
                return result.setCode(10002).setMsg("账号或密码错误，注册需要填写的账号就是游戏账号！");
            }
        } else {
            return result.setCode(10003).setMsg("未知的服务器类型");
        }

        var newAccount = new AccountEntity();
        newAccount.setName(username)
                .setAccount(account)
                .setPassword(password);
        var code = cdkService.createUserByCDK(newAccount, cdk);

        if (code == 200) {
            result.setCode(200).setMsg("success").setData("注册成功");
        } else if (code == 404) {
            result.setCode(10004).setMsg("CDK不存在或已被激活");
        }
        return result;
    }

    @Operation(summary = "使用在线支付创建我的账号")
    @PostMapping("/createUserByPay")
    public Result<String> createUserByPay(String payType, String username, String account, String password, Integer server) {
        Result<String> result = new Result<>();

        if (accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                .eq(AccountEntity::getAccount, account)).size() != 0) {
            result.setCode(10001);
            result.setMsg("账号已存在，请直接登录");
            return result;
        }

        if (server == 0) {
            if (!httpService.isOfficialAccountWork(account, password)) {
                return result.setCode(10002).setMsg("账号或密码错误，注册需要填写的账号就是游戏账号！");
            }
        } else if (server == 1) {
            if (!httpService.isBiliAccountWork(account, password)) {
                return result.setCode(10002).setMsg("账号或密码错误，注册需要填写的账号就是游戏账号！");
            }
        } else {
            return result.setCode(10003).setMsg("未知的服务器类型");
        }

        var bill = payService.createOrder(1.0, payType, "/auth/user/");
        bill.setType("register")
                .setParam(username + "|" + account + "|" + password + "|" + server);
        billMapper.updateById(bill);

        result.setData(bill.getPayUrl());

        return result.setCode(200)
                .setMsg("success");
    }


    @Operation(summary = "登陆我的账号")
    @PostMapping("/userLogin")
    public Result<HashMap<String, String>> userLogin(String username, String password) {
        Result<HashMap<String, String>> result = new Result<>();

        if (username == null || password == null) {
            return result.setCode(403)
                    .setMsg("账号或密码为空")
                    .setData(null);
        }

        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAccount, username)
                        .eq(AccountEntity::getPassword, password)
        );

        if (account != null) {
            return result.setCode(200)
                    .setMsg("登陆成功")
                    .setData(new HashMap<>() {
                        {
                            put("token", JWTUtils.generateTokenForUser(account));
                        }
                    });
        } else {
            return result.setCode(404)
                    .setMsg("账号或密码错误")
                    .setData(null);
        }
    }

    @UserLogin
    @Operation(summary = "查询自己的账号")
    @GetMapping("/showMyAccount")
    public Result<AccountEntity> showMyAccount(@RequestHeader("Authorization") String token) {
        Result<AccountEntity> result = new Result<>();

        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, JWTUtils.getId(token))
        );
        result.setCode(200)
                .setMsg("success")
                .setData(account);

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
            String taskType = account.getTaskType();
            LocalDateTime expireTime = account.getExpireTime();
            account = accountEntity;
            account.setId(JWTUtils.getId(token));
            account.setTaskType(taskType);
            account.setExpireTime(expireTime);
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

    @UserLogin
    @Operation(summary = "更新账号密码")
    @PostMapping("/updateAccountAndPassword")
    public Result<String> updateAccountAndPassword(@RequestHeader("Authorization") String token,
                                                   String account, String password, Long server) {
        Result<String> result = new Result<>();
        var accountEntity = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, JWTUtils.getId(token))
        );

        if (account.isBlank() || password.isBlank() || server == null) {
            return result.setCode(403).setMsg("未填写数据");
        }

        if (server == 0) {
            if (httpService.isOfficialAccountWork(account, password)) {
                accountEntity.setAccount(account);
                accountEntity.setPassword(password);
                accountEntity.setServer(server);
                accountEntity.setUpdateTime(LocalDateTime.now());
                accountMapper.updateById(accountEntity);
            } else {
                result.setCode(10002)
                        .setMsg("验证失败，账号或密码错误");
                return result;
            }
        } else if (server == 1) {
            if (httpService.isBiliAccountWork(account, password)) {
                accountEntity.setAccount(account);
                accountEntity.setPassword(password);
                accountEntity.setServer(server);
                accountEntity.setFreeze(0);
                accountEntity.getBLimitDevice().clear();
                accountEntity.setUpdateTime(LocalDateTime.now());
                accountMapper.updateById(accountEntity);
            } else {
                result.setCode(10002)
                        .setMsg("验证失败，账号或密码错误");
                return result;
            }
        }

        return result.setCode(200)
                .setMsg("success")
                .setData(null);
    }

    @UserLogin
    @Operation(summary = "冻结我的账号")
    @PostMapping("/freezeMyAccount")
    public Result<String> freezeMyAccount(@RequestHeader("Authorization") String token) {
        Result<String> result = new Result<>();
        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, JWTUtils.getId(token))
        );
        if (account != null) {
            account.setFreeze(1);
            accountMapper.updateById(account);
            dynamicInfo.getUserSanList().remove(account.getId());
            result.setCode(200)
                    .setMsg("success");
        } else {
            result.setCode(403)
                    .setMsg("Unable to freeze a non-existent account");
        }
        result.setData(null);
        return result;
    }

    @UserLogin
    @Operation(summary = "解冻我的账号")
    @PostMapping("/unfreezeMyAccount")
    public Result<String> unfreezeMyAccount(@RequestHeader("Authorization") String token) {
        Result<String> result = new Result<>();
        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, JWTUtils.getId(token))
        );
        if (account != null) {
            account.setFreeze(0);
            accountMapper.updateById(account);
            if (dynamicInfo.getUserMaxSanList().containsKey(account.getId())) {
                dynamicInfo.getUserSanList().put(account.getId(),
                        dynamicInfo.getUserMaxSanList().get(account.getId()) - 20);
            } else {
                dynamicInfo.getUserSanList().put(account.getId(), 80);
                dynamicInfo.getUserMaxSanList().put(account.getId(), 100);
            }
            result.setCode(200)
                    .setMsg("success");
        } else {
            result.setCode(403)
                    .setMsg("Unable to unfreeze a non-existent account");
        }
        result.setData(null);
        return result;
    }

    @UserLogin
    @Operation(summary = "查询我的日志")
    @GetMapping("/showMyLog")
    public Result<HashMap<String, Object>> showMyLog(@RequestHeader("Authorization") String token, Long current,
                                                     Long size) {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        //降序分页查找
        var data = logMapper.selectPage(new Page<>(current, size), Wrappers.<LogEntity>lambdaQuery()
                .eq(LogEntity::getAccount, JWTUtils.getUsername(token))
                .orderByDesc(LogEntity::getId));

        for (LogEntity log : data.getRecords()) {
            log.setPassword("");
            log.setFrom("");
        }

        result.getData().put("records", data.getRecords());
        result.getData().put("current", data.getCurrent());
        result.getData().put("totalPages", data.getPages());
        result.getData().put("total", data.getTotal());

        result.setCode(200)
                .setMsg("success");

        return result;
    }

    @UserLogin
    @Operation(summary = "查询我状态")
    @GetMapping("/showMyStatus")
    public Result<HashMap<String, String>> showMyStatus(@RequestHeader("Authorization") String token) {
        Result<HashMap<String, String>> result = new Result<>();
        result.setData(new HashMap<>());

        var id = JWTUtils.getId(token);

        if ((accountMapper.selectById(id).getFreeze() == 1)) {
            result.setCode(200).getData().put("msg", "账号已被冻结，若需继续托管请先解冻");
            return result;
        }

        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicInteger index = new AtomicInteger(0);

        dynamicInfo.getFreeTaskList().forEach(
                it -> {
                    if (Objects.equals(it.getId(), id)) {
                        flag.set(true);
                        if (index.get() != 0) {
                            result.getData().put("msg", "前方还有" + index + "个账号");
                        } else {
                            result.getData().put("msg", "等待空闲设备作战，请勿顶号");
                        }
                    }
                    index.getAndIncrement();
                }
        );

        dynamicInfo.getLockTaskList().forEach(
                lockTask -> {
                    if (Objects.equals(lockTask.getAccount().getId(), id)) {
                        flag.set(true);
                        result.getData().put("msg", "正在作战中，请勿顶号");
                    }
                }
        );

        if (!flag.get()) {
//            LocalDateTime nextTime = CronExpression.parse(cron).next(LocalDateTime.now());
            var san = dynamicInfo.getUserSanList().get(id);
            var maxSan = dynamicInfo.getUserMaxSanList().get(id);
            LocalDateTime nextTime = LocalDateTime.now()
                    .plusMinutes((maxSan - san) * 6L);
            String nextTimeStr = nextTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            result.getData().put("msg", "理智: " + san + "/" + maxSan + " 下一轮作战最迟将于" + nextTimeStr + "开始");
        }

        return result;
    }

    @UserLogin
    @Operation(summary = "查询当前理智")
    @GetMapping("/showMySan")
    public Result<String> showMySan(@RequestHeader("Authorization") String token) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);
        var ans = "";
        if (!dynamicInfo.getUserSanList().containsKey(id)) {
            ans = "账号冻结中";
        } else if (dynamicInfo.getUserSanList().get(id).equals(dynamicInfo.getUserMaxSanList().get(id))) {
            ans = "正在尝试作战以校准理智值";
        } else if (dynamicInfo.getLockTaskList().stream().anyMatch(e -> e.getAccount().getId().equals(id))) {
            ans = "等待作战结束以校准理智";
        } else {
            ans = dynamicInfo.getUserSanList().get(id) + "/" + dynamicInfo.getUserMaxSanList().get(id);
        }

        return result.setCode(200)
                .setMsg("success")
                .setData(ans);
    }

    @UserLogin
    @Operation(summary = "使用CDK")
    @PostMapping("/useCDK")
    public Result<String> useCDK(@RequestHeader("Authorization") String token, String cdk) {
        Result<String> result = new Result<>();

        var id = JWTUtils.getId(token);
        var code = cdkService.activateCDK(id, cdk);

        if (code == 200) {
            result.setCode(200).setMsg("success").setData("激活成功");
        } else if (code == 404) {
            result.setCode(404).setMsg("CDK不存在或已被激活");
        }
        return result;
    }

    @UserLogin
    @Operation(summary = "获取微信推送二维码")
    @GetMapping("/getWechatQRCode")
    public Result<String> getWechatQRCode(@RequestHeader("Authorization") String token) {
        Result<String> result = new Result<>();
        if (enableWxPusher) {
            var account = accountMapper.selectOne(
                    Wrappers.<AccountEntity>lambdaQuery()
                            .eq(AccountEntity::getId, JWTUtils.getId(token))
            );
            if (account == null) {
                result.setCode(403);
                result.setMsg("账号不存在");
                return result;
            }
            CreateQrcodeReq createQrcodeReq = new CreateQrcodeReq();
            createQrcodeReq.setAppToken(appToken);
            createQrcodeReq.setExtra(String.valueOf(JWTUtils.getId(token)));
            createQrcodeReq.setValidTime(3600);
            var qrcode = WxPusher.createAppTempQrcode(createQrcodeReq);
            if (qrcode.isSuccess()) {
                return result.setCode(200).setMsg("success").setData(qrcode.getData().getUrl());
            } else {
                return result.setCode(403).setMsg("获取二维码失败");
            }
        } else {
            return result.setCode(403).setMsg("未开启微信推送");
        }
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
    @Operation(summary = "获取通知状态")
    @GetMapping("/getNoticeStatus")
    public Result<NoticeEntity> getNoticeStatus(@RequestHeader("Authorization") String token) {
        Result<NoticeEntity> result = new Result<>();
        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, JWTUtils.getId(token))
        );

        if (account.getNotice() == null) {
            account.setNotice(new NoticeEntity());
            account.getNotice().getWxUID().setEnable(false);
            account.getNotice().getWxUID().setText("");
            account.getNotice().getMail().setEnable(false);
            account.getNotice().getMail().setText("");
            account.getNotice().getQq().setEnable(false);
            account.getNotice().getQq().setText("");
            accountMapper.updateById(account);
        }

        result.setCode(200);
        result.setMsg("success");
        result.setData(account.getNotice());
        return result;
    }

    @UserLogin
    @Operation(summary = "强制停止作战")
    @PostMapping("/forceHalt")
    public Result<String> forceHalt(@RequestHeader("Authorization") String token) {
        Result<String> result = new Result<>();
        Long id = JWTUtils.getId(token);

        var freeListIterator = dynamicInfo.getFreeTaskList().iterator();
        while (freeListIterator.hasNext()) {
            var freeTask = freeListIterator.next();
            if (freeTask.getId().equals(id)) {
                freeListIterator.remove();
                break;
            }
        }

        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(id)) {
                dynamicInfo.getHaltList().add(lockTask.getDeviceToken());
                dynamicInfo.getLockTaskList().remove(lockTask);
                break;
            }
        }

        dynamicInfo.getFreezeTaskList().remove(id);

        return result.setCode(200).setMsg("执行成功");
    }

    @UserLogin
    @Operation(summary = "立即开始作战")
    @PostMapping("/startNow")
    public Result<String> startNow(@RequestHeader("Authorization") String token) {
        Result<String> result = new Result<>();
        Long id = JWTUtils.getId(token);
        AccountEntity account = accountMapper.selectById(id);

        if (account.getDelete() == 1 || account.getExpireTime().isBefore(LocalDateTime.now())) {
            return result.setCode(10006).setMsg("账号已到期或失效");
        }

        for (AccountEntity freeTask : dynamicInfo.getFreeTaskList()) {
            if (freeTask.getId().equals(id)) {
                var freeListIterator = dynamicInfo.getFreeTaskList().iterator();
                while (freeListIterator.hasNext()) {
                    var insertTask = freeListIterator.next();
                    if (insertTask.getId().equals(id)) {
                        freeListIterator.remove();
                        dynamicInfo.getFreeTaskList().add(0, insertTask);
                        return result.setCode(20003).setMsg("插队成功");
                    }
                }
            }
        }

        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(id)) {
                return result.setCode(20002).setMsg("已经在作战中");
            }
        }

        if (account.getRefresh() < 1) {
            return result.setCode(10005).setMsg("今日刷新次数已达上限，每天零点刷新，明天再来看看吧");
        }

        if (account.getFreeze() == 1) {
            return result.setCode(10007).setMsg("请先解冻再执行操作");
        }

        account.setRefresh(account.getRefresh() - 1);
        accountMapper.updateById(account);
        dynamicInfo.getFreeTaskList().add(0, account);
        dynamicInfo.getUserSanList().put(id, 0);

        return result.setCode(20001).setMsg("立即开始作战成功，等待分配作战服务器");
    }

    @UserLogin
    @Operation(summary = "获取刷新次数")
    @GetMapping("/getRefresh")
    public Result<Integer> getRefresh(@RequestHeader("Authorization") String token) {
        Result<Integer> result = new Result<>();
        Long id = JWTUtils.getId(token);
        AccountEntity account = accountMapper.selectById(id);
        result.setCode(200).setMsg("success").setData(account.getRefresh());
        return result;
    }

}
