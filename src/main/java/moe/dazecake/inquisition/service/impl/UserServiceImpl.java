package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zjiecode.wxpusher.client.WxPusher;
import com.zjiecode.wxpusher.client.bean.CreateQrcodeReq;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.BillMapper;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.mapper.mapstruct.AccountConvert;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.dto.user.CreateUserByPayDTO;
import moe.dazecake.inquisition.model.dto.user.UserStatusSTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.vo.UserLoginVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.intf.UserService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    AccountMapper accountMapper;

    @Resource
    LogServiceImpl logService;

    @Resource
    HttpServiceImpl httpService;

    @Resource
    CDKServiceImpl cdkService;

    @Resource
    PayServiceImpl payService;

    @Resource
    TaskServiceImpl taskService;

    @Resource
    AccountServiceImpl accountService;

    @Resource
    BillMapper billMapper;

    @Resource
    ProUserMapper proUserMapper;

    @Value("${wx-pusher.app-token:}")
    String appToken;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;

    @Override
    public Result<String> createUserByCDK(String cdk, String username, String account, String password, Integer server) {
        if (accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                .eq(AccountEntity::getAccount, account)).size() != 0) {
            return Result.forbidden("账号已存在，请直接登录");
        }

        var newAccount = new AccountEntity();
        newAccount.setName(username)
                .setAccount(account)
                .setPassword(password);
        return cdkService.createUserByCDK(newAccount, cdk);
    }

    @Override
    public Result<String> createUserByPay(CreateUserByPayDTO createUserByPayDTO, String username, String account, String password, Integer server) {
        if (username.contains("|") || account.contains("|") || password.contains("|")) {
            return Result.paramError("用户名，账号，密码中不能包含 | 字符");
        }
        if (createUserByPayDTO.getAgent() != 0) {
            var proUser = proUserMapper.selectById(createUserByPayDTO.getAgent());
            if (proUser == null) {
                return Result.notFound("代理商不存在");
            }
        }

        if (accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                .eq(AccountEntity::getAccount, account)).size() != 0) {
            return Result.forbidden("账号已存在，请直接登录");
        }

        var bill = payService.createOrder(1.0, createUserByPayDTO.getPayType(), "/auth/user/");
        bill.setType("register")
                .setParam(username + "|" + account + "|" + password + "|" + server);
        if (createUserByPayDTO.getAgent() != 0) {
            bill.setParam(bill.getParam() + "|" + createUserByPayDTO.getAgent());
        } else {
            bill.setParam(bill.getParam() + "|0");
        }
        billMapper.updateById(bill);

        return Result.success(bill.getPayUrl(), "请在支付成功后直接登录");
    }

    @Override
    public Result<UserLoginVO> userLogin(String account, String password) {
        if (account == null || password == null) {
            return Result.unauthorized("请输入账号密码");
        }

        var user = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAccount, account)
                        .eq(AccountEntity::getPassword, password)
        );

        if (user != null) {
            if (user.getDelete() == 1) {
                return Result.forbidden("账号已被删除，请联系管理员解除");
            }
            return Result.success(new UserLoginVO(JWTUtils.generateTokenForUser(user)), "登录成功");
        } else {
            return Result.unauthorized("账号或密码错误");
        }
    }

    @Override
    public Result<AccountDTO> showMyAccount(Long id) {
        return Result.success(AccountConvert.INSTANCE.toAccountDTO(accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, id)
        )), "获取成功");
    }

    @Override
    public Result<String> updateMyAccount(Long id, AccountDTO accountDTO) {
        var newAccount = AccountConvert.INSTANCE.toAccountEntity(accountDTO);
        var oldAccount = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, id)
        );
        if (oldAccount != null) {

            oldAccount.setName(newAccount.getName())
                    .setConfig(newAccount.getConfig())
                    .setActive(newAccount.getActive())
                    .setNotice(newAccount.getNotice());
            accountMapper.updateById(oldAccount);

            return Result.success("更新成功");

        } else {
            return Result.notFound("不存在的账号");
        }
    }

    @Override
    public Result<String> updateAccountAndPassword(Long id, String account, String password, Long server) {

        if (account.isBlank() || password.isBlank() || server == null) {
            return Result.paramError("不允许为空");
        }

        var accountList = accountMapper.selectList(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAccount, account)
        );
        if (accountList.size() > 1) {
            return Result.forbidden("此账号已经被使用");
        }

        var accountEntity = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, id)
        );

        if (server == 0) {
            if (httpService.isOfficialAccountWork(account, password)) {
                accountEntity.setAccount(account);
                accountEntity.setPassword(password);
                accountEntity.setServer(server);
                accountEntity.setUpdateTime(LocalDateTime.now());
                accountMapper.updateById(accountEntity);
            } else {
                return Result.unauthorized("验证失败，账号或密码错误");
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
                return Result.unauthorized("验证失败，账号或密码错误");
            }
        }

        return Result.success("更新成功");
    }

    @Override
    public Result<String> freezeMyAccount(Long id) {
        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, id)
        );
        if (account != null) {
            account.setFreeze(1);
            accountMapper.updateById(account);
            dynamicInfo.getUserSanInfoMap().remove(account.getId());
            taskService.forceHaltTask(account.getId());
            return Result.success("冻结成功");
        } else {
            return Result.notFound("不存在的账号");
        }
    }

    @Override
    public Result<String> unfreezeMyAccount(Long id) {
        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, id)
        );
        if (account != null) {
            account.setFreeze(0);
            accountMapper.updateById(account);
            if (dynamicInfo.getUserSanInfoMap().containsKey(account.getId())) {
                dynamicInfo.setUserSanZero(account.getId());
            } else {
                dynamicInfo.setUserSan(account.getId(), 0, 135);
            }
            return Result.success("解冻成功");
        } else {
            return Result.notFound("不存在的账号");
        }
    }

    @Override
    public Result<PageQueryVO<LogDTO>> showMyLog(String account, Long current, Long size) {
        return Result.success(logService.queryLogByAccount(account, current, size), "获取成功");
    }

    @Override
    public Result<UserStatusSTO> showMyStatus(Long id) {
        var user = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getId, id)
        );
        if (user == null) {
            return Result.notFound("不存在的账号");
        }
        if (user.getFreeze() == 1) {
            return Result.success(new UserStatusSTO("账号已被冻结，若需继续托管请先解冻"), "获取成功");
        }

        for (Long k : dynamicInfo.getFreezeUserInfoMap().keySet()) {
            if (Objects.equals(k, id)) {
                return Result.success(new UserStatusSTO("发生冲突，账号强制冷却，稍后将自动重试作战"), "获取成功");
            }
        }

        var index = 0;
        for (Long waiter : dynamicInfo.getWaitUserList()) {
            if (waiter.equals(id)) {
                if (index != 0) {
                    return Result.success(new UserStatusSTO("前方还有" + index + "个账号，请耐心等待"), "获取成功");
                } else {
                    return Result.success(new UserStatusSTO("等待空闲设备响应，即将作战，请勿顶号"), "获取成功");
                }
            }
            index++;
        }

        for (Long worker : dynamicInfo.getWorkUserList()) {
            if (worker.equals(id)) {
                return Result.success(new UserStatusSTO("正在作战中，请勿顶号"), "获取成功");
            }
        }


        if (!dynamicInfo.getUserSanInfoMap().containsKey(id)) {
            return Result.success(new UserStatusSTO("无法获取理智状态，请尝试使用立即作战重新校准理智"), "获取成功");
        } else {
            var san = dynamicInfo.getUserSanInfoMap().get(id).getSan();
            var maxSan = dynamicInfo.getUserSanInfoMap().get(id).getMaxSan();
            LocalDateTime nextTime = LocalDateTime.now()
                    .plusMinutes((maxSan - san) * 6L);
            String nextTimeStr = nextTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            return Result.success(new UserStatusSTO("下一轮作战最迟将于" + nextTimeStr + "开始"), "获取成功");
        }

    }

    @Override
    public Result<String> showMySan(Long id) {
        var ans = "";
        if (dynamicInfo.getFreezeUserInfoMap().containsKey(id)) {
            ans = "账号冻结中";
        } else if (dynamicInfo.getWorkUserList().contains(id)) {
            ans = "等待作战结束以校准理智";
        } else if (!dynamicInfo.getUserSanInfoMap().containsKey(id)) {
            ans = "出现严重错误，请立即使用立即作战以校准";
        } else {
            ans = dynamicInfo.getUserSanInfoMap().get(id).getSan() + "/" + dynamicInfo.getUserSanInfoMap().get(id).getMaxSan();
        }

        return Result.success(ans, "获取成功");
    }

    @Override
    public Result<String> useCDK(Long id, String cdk) {
        return cdkService.activateCDK(id, cdk);
    }

    @Override
    public Result<String> getWechatQRCode(Long id) {
        Result<String> result = new Result<>();
        if (enableWxPusher) {
            var account = accountMapper.selectOne(
                    Wrappers.<AccountEntity>lambdaQuery()
                            .eq(AccountEntity::getId, id)
            );
            if (account == null) {
                result.setCode(403);
                result.setMsg("账号不存在");
                return result;
            }
            CreateQrcodeReq createQrcodeReq = new CreateQrcodeReq();
            createQrcodeReq.setAppToken(appToken);
            createQrcodeReq.setExtra(String.valueOf(id));
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

    @Override
    public Result<String> forceHalt(Long id) {
        taskService.forceHaltTask(id);
        return Result.success("执行成功");
    }

    @Override
    public Result<String> startNow(Long id) {
        return Result.success(accountService.forceFightAccount(id, false));
    }

    @Override
    public Result<Integer> getRefresh(Long id) {
        var account = accountMapper.selectById(id);
        return Result.success(account.getRefresh(), "获取成功");
    }

}
