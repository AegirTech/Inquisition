package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import moe.dazecake.inquisition.constant.ResponseCodeConstants;
import moe.dazecake.inquisition.constant.enums.CDKWrapper;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.LogMapper;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.mapper.mapstruct.AccountConvert;
import moe.dazecake.inquisition.mapper.mapstruct.ProUserConvert;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.account.AddAccountDTO;
import moe.dazecake.inquisition.model.dto.cdk.CreateCDKDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.dto.prouser.CreateProUserDTO;
import moe.dazecake.inquisition.model.dto.prouser.ProUserDTO;
import moe.dazecake.inquisition.model.dto.prouser.ProUserLoginDTO;
import moe.dazecake.inquisition.model.dto.prouser.UpdateProUserPasswordDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.LogEntity;
import moe.dazecake.inquisition.model.entity.ProUserEntity;
import moe.dazecake.inquisition.model.vo.account.AccountWithSanVO;
import moe.dazecake.inquisition.model.vo.cdk.CDKListVO;
import moe.dazecake.inquisition.model.vo.prouser.ProUserLoginVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.intf.ProUserService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Encoder;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class ProUserServiceImpl implements ProUserService {

    private static final String salt = "arklightspro";

    @Resource
    private DynamicInfo dynamicInfo;

    @Resource
    private ProUserMapper proUserMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private LogMapper logMapper;

    @Resource
    private LogServiceImpl logService;

    @Resource
    private AccountServiceImpl accountService;

    @Resource
    private TaskServiceImpl taskService;

    @Resource
    private CDKServiceImpl cdkService;

    @Value("${inquisition.price.daily:1.0}")
    private Double dailyPrice;

    @Value("${inquisition.price.rogue_1:40.0}")
    private Double rogue1Price;

    @Value("${inquisition.price.rogue_2:40.0}")
    private Double rogue2Price;

    @Override
    public void CreateProUser(CreateProUserDTO createProUserDTO) {
        var proUserEntity = ProUserConvert.INSTANCE.toProUserEntity(createProUserDTO);
        proUserEntity.setPassword(Encoder.MD5(proUserEntity.getPassword() + salt));
        proUserMapper.insert(proUserEntity);
    }

    @Override
    public Result<PageQueryVO<ProUserDTO>> getAllProUser(Long current, Long size) {
        var data = proUserMapper.selectPage(new Page<>(current, size), null);
        return Result.success(getProUserVOPageQueryVO(data), "查询成功");
    }

    @Override
    public Result<String> updateProUser(ProUserDTO proUserDTO) {
        var proUserEntity = ProUserConvert.INSTANCE.toProUserEntity(proUserDTO);
        proUserEntity.setPassword(Encoder.MD5(proUserEntity.getPassword() + salt));
        proUserMapper.updateById(proUserEntity);
        return Result.success("修改成功");
    }

    @Override
    public Result<ProUserLoginVO> loginProUser(ProUserLoginDTO proUserLoginDTO) {
        if (proUserLoginDTO.getUsername() == null || proUserLoginDTO.getPassword() == null) {
            return Result.paramError("用户名或密码不能为空");
        }
        var account = proUserMapper.selectOne(
                Wrappers.<ProUserEntity>lambdaQuery()
                        .eq(ProUserEntity::getUsername, proUserLoginDTO.getUsername())
                        .eq(ProUserEntity::getPassword, Encoder.MD5(proUserLoginDTO.getPassword() + salt))
        );
        if (account != null) {
            return Result.success(new ProUserLoginVO(JWTUtils.generateTokenForProUser(account)), "登录成功");
        } else {
            return Result.unauthorized("用户名或密码错误");
        }
    }

    @Override
    public Result<ProUserDTO> getProUserInfo(Long id) {
        var proUserEntity = proUserMapper.selectById(id);
        if (proUserEntity != null) {
            return Result.success(ProUserConvert.INSTANCE.toProUserDTO(proUserEntity), "获取成功");
        } else {
            return Result.notFound("用户不存在");
        }
    }

    @Override
    public Result<String> updateProUserPassword(Long id, UpdateProUserPasswordDTO updateProUserPasswordDTO) {
        var old = proUserMapper.selectById(id);

        if (Encoder.MD5(updateProUserPasswordDTO.getOldPassword() + salt).equals(old.getPassword())) {
            old.setPassword(Encoder.MD5(updateProUserPasswordDTO.getNewPassword() + salt));
            proUserMapper.updateById(old);
            return Result.success("修改成功");
        } else {
            return Result.forbidden("旧密码错误");
        }
    }

    @Override
    public Result<PageQueryVO<AccountWithSanVO>> queryAllSubUser(Long id, String type, Integer current, Integer size) {
        var wrapper = Wrappers.<AccountEntity>lambdaQuery().eq(AccountEntity::getAgent, id);
        switch (type) {
            case "all":
                break;
            case "active":
                wrapper.gt(AccountEntity::getExpireTime, LocalDateTime.now())
                        .eq(AccountEntity::getDelete, 0);
                break;
            case "expired":
                wrapper.lt(AccountEntity::getExpireTime, LocalDateTime.now())
                        .eq(AccountEntity::getDelete, 0);
                break;
            case "frozen":
                wrapper.eq(AccountEntity::getFreeze, 1)
                        .eq(AccountEntity::getDelete, 0);
                break;
            case "deleted":
                wrapper.eq(AccountEntity::getDelete, 1);
                break;
            default:
                return Result.paramError("参数错误");
        }
        var data = accountMapper.selectPage(new Page<>(current, size), wrapper);
        return Result.success(accountService.getAccountWithSanVOPageQueryVO(data), "查询成功");
    }

    @Override
    public Result<PageQueryVO<AccountWithSanVO>> querySubUserByAccount(Long id, Integer current, Integer size, String keyword) {
        var data = accountMapper.selectPage(
                new Page<>(current, size),
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAgent, id)
                        .eq(AccountEntity::getAccount, keyword)
        );
        return Result.success(accountService.getAccountWithSanVOPageQueryVO(data), "查询成功");
    }

    @Override
    public void updateSubAccount(Long id, AccountDTO accountDTO) {
        var oldSubAccountEntity = accountMapper.selectById(accountDTO.getId());

        if (oldSubAccountEntity != null) {
            if (oldSubAccountEntity.getAgent().equals(id)) {
                var newSubAccountEntity = AccountConvert.INSTANCE.toAccountEntity(accountDTO);
                newSubAccountEntity.setExpireTime(oldSubAccountEntity.getExpireTime());
                newSubAccountEntity.setRefresh(oldSubAccountEntity.getRefresh());
                newSubAccountEntity.setTaskType(oldSubAccountEntity.getTaskType());
                accountMapper.updateById(newSubAccountEntity);
            }
        }
    }

    @Override
    public Result<PageQueryVO<LogDTO>> querySubUserLogByAccount(Long id, Long userID, Integer current, Integer size) {
        var subUser = accountMapper.selectById(userID);
        if (subUser == null) {
            return Result.notFound("未找到该用户");
        }
        if (!subUser.getAgent().equals(id)) {
            return Result.forbidden("无权查看");
        }
        var data = logMapper.selectPage(
                new Page<>(current, size),
                Wrappers.<LogEntity>lambdaQuery()
                        .eq(LogEntity::getAccount, subUser.getAccount())
                        .orderByDesc(LogEntity::getId)
        );
        return Result.success(logService.getLogPageQueryVO(data), "查询成功");
    }

    @Override
    public Result<String> forceFightSubUser(Long id, Long userID) {
        var preCheckResult = preCheckSubUser(id, userID);
        if (preCheckResult.getCode() != ResponseCodeConstants.SUCCESS) {
            return preCheckResult;
        }
        dynamicInfo.getFreezeUserInfoMap().get(userID);
        for (Long worker : dynamicInfo.getWorkUserList()) {
            if (worker.equals(userID)) {
                return Result.success("该用户已经在作战中");
            }
        }
        for (Long waiter : dynamicInfo.getWaitUserList()) {
            if (waiter.equals(userID)) {
                dynamicInfo.getWaitUserList().remove(waiter);
                dynamicInfo.getWaitUserList().add(0, waiter);
                return Result.success("插队成功");
            }
        }
        dynamicInfo.getWaitUserList().add(0, userID);
        dynamicInfo.setUserSanZero(userID);
        return Result.success("立即作战成功");
    }

    @Override
    public Result<String> forceStopSubUser(Long id, Long userID) {
        var preCheckResult = preCheckSubUser(id, userID);
        if (preCheckResult.getCode() != ResponseCodeConstants.SUCCESS) {
            return preCheckResult;
        }
        taskService.forceHaltTask(userID);
        return Result.success("强停成功");
    }

    @Override
    public Result<String> activateSubUserCdk(Long userID, String cdk) {
        return cdkService.activateCDK(userID, cdk);
    }

    @Override
    public Result<CDKListVO> queryProUserCDKList(Long id) {
        return cdkService.queryCDKList(CDKWrapper.AGENT, id.toString());
    }

    @Override
    public Result<String> createCdkByProUser(Long id, CreateCDKDTO createCDKDTO) {
        var proUser = proUserMapper.selectById(id);

        if (proUser == null) {
            return Result.notFound("未找到该用户");
        }

        //检查余额
        if (proUser.getBalance() < createCDKDTO.getCount() * dailyPrice * createCDKDTO.getParam() * proUser.getDiscount()) {
            return Result.forbidden("余额不足");
        }

        //扣除余额
        proUser.setBalance(proUser.getBalance() - createCDKDTO.getCount() * dailyPrice * createCDKDTO.getParam() * proUser.getDiscount());
        proUserMapper.updateById(proUser);

        createCDKDTO.setAgent(id);
        createCDKDTO.setIsAgent(true);

        cdkService.createCDK(createCDKDTO);
        return Result.success("创建成功");
    }

    @Override
    public Result<String> renewSubUserDaily(Long id, Long userID, Integer mo) {
        var proUser = proUserMapper.selectById(id);
        var subUser = accountMapper.selectById(userID);

        if (subUser == null) {
            return Result.notFound("未找到该用户");
        }

        if (proUser == null) {
            return Result.notFound("未找到该用户");
        }

        //检查余额
        if (proUser.getBalance() < mo * 30 * dailyPrice * proUser.getDiscount()) {
            return Result.forbidden("余额不足");
        }

        //扣除余额
        proUser.setBalance(proUser.getBalance() - mo * 30 * dailyPrice * proUser.getDiscount());
        proUserMapper.updateById(proUser);

        if (subUser.getExpireTime().isAfter(LocalDateTime.now())) {
            subUser.setExpireTime(subUser.getExpireTime().plusDays(mo * 30));
        } else {
            subUser.setExpireTime(LocalDateTime.now().plusDays(mo * 30));
        }
        accountMapper.updateById(subUser);
        return Result.success("续费成功");
    }

    @Override
    public Result<String> createSubUserByProUser(Long id, String name, String account, String password, Long server, Integer days) {
        var proUser = proUserMapper.selectById(id);
        //检查余额
        if (proUser.getBalance() < days * dailyPrice * proUser.getDiscount()) {
            return Result.forbidden("余额不足");
        }

        //检查重复用户
        if (accountMapper.selectOne(Wrappers.<AccountEntity>lambdaQuery().eq(AccountEntity::getAccount, account)) != null) {
            return Result.forbidden("该账号已存在，不允许重复创建");
        }

        //扣除余额
        proUser.setBalance(proUser.getBalance() - days * dailyPrice * proUser.getDiscount());
        proUserMapper.updateById(proUser);

        //创建用户
        AddAccountDTO addAccountDTO = new AddAccountDTO();
        addAccountDTO.setName(name);
        addAccountDTO.setAccount(account);
        addAccountDTO.setPassword(password);
        addAccountDTO.setServer(server);
        addAccountDTO.setExpireTime(LocalDateTime.now().plusDays(days));
        addAccountDTO.setAgent(id);
        accountService.addAccount(addAccountDTO);
        var userId = accountMapper.selectOne(Wrappers.<AccountEntity>lambdaQuery().eq(AccountEntity::getAccount, account)).getId();
        accountService.forceFightAccount(userId, true);
        return Result.success("创建成功");
    }

    @Override
    public Result<ArrayList<AccountDTO>> getRecentlyExpiredUsers(Long id) {
        var proUser = proUserMapper.selectById(id);
        if (proUser == null) {
            return Result.notFound("未找到该用户");
        }
        var result = new ArrayList<AccountDTO>();
        var list = accountMapper.selectList(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAgent, id)
                        .lt(AccountEntity::getExpireTime, LocalDateTime.now().plusDays(7))
                        .gt(AccountEntity::getExpireTime, LocalDateTime.now())
                        .orderByAsc(AccountEntity::getExpireTime)
        );
        for (AccountEntity accountEntity : list) {
            result.add(AccountConvert.INSTANCE.toAccountDTO(accountEntity));
        }
        return Result.success(result, "查询成功");
    }

    @NotNull
    private Result<String> preCheckSubUser(Long id, Long userID) {
        var subUser = accountMapper.selectById(userID);
        if (subUser == null) {
            return Result.notFound("未找到该用户");
        }
        if (!subUser.getAgent().equals(id)) {
            return Result.forbidden("无权操作");
        }
        if (subUser.getExpireTime().isBefore(LocalDateTime.now())) {
            return Result.forbidden("该用户已过期");
        }
        return Result.success("检查通过");
    }

    @org.jetbrains.annotations.NotNull
    public PageQueryVO<ProUserDTO> getProUserVOPageQueryVO(Page<ProUserEntity> data) {
        var result = new PageQueryVO<ProUserDTO>();
        result.setCurrent(data.getCurrent());
        result.setPage(data.getPages());
        result.setTotal(data.getTotal());

        for (ProUserEntity user : data.getRecords()) {
            result.getRecords().add(ProUserConvert.INSTANCE.toProUserDTO(user));
        }
        return result;
    }
}
