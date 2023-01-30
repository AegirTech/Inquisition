package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.mapstruct.AccountConvert;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.account.AddAccountDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.TaskDateSet.LockTask;
import moe.dazecake.inquisition.model.vo.account.AccountWithSanVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.intf.AccountService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    AccountMapper accountMapper;

    @Resource
    TaskServiceImpl taskService;

    @Override
    public void addAccount(AddAccountDTO addAccountDTO) {
        var accountEntity = new AccountEntity();
        accountEntity.setName(addAccountDTO.getName())
                .setAccount(addAccountDTO.getAccount())
                .setPassword(addAccountDTO.getPassword())
                .setServer(addAccountDTO.getServer())
                .setExpireTime(addAccountDTO.getExpireTime());

        accountMapper.insert(accountEntity);
    }

    @Override
    public int transferAccount(HashMap<String, String> accountJson) {
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

        return num;
    }

    @Override
    public void deleteAccount(Long id) {
        var account = accountMapper.selectById(id);

        if (account != null) {
            account.setDelete(1);
            accountMapper.updateById(account);
        }
    }

    @Override
    public void updateAccount(AccountDTO accountDTO) {
        var account = accountMapper.selectById(accountDTO.getId());

        if (account != null) {
            accountMapper.updateById(AccountConvert.INSTANCE.toAccountEntity(accountDTO));
        }
    }

    @Override
    public PageQueryVO<AccountWithSanVO> queryAllAccount(Long current, Long size) {
        var data = accountMapper.selectPage(new Page<>(current, size), null);
        return getAccountWithSanVOPageQueryVO(data);
    }

    @Override
    public PageQueryVO<AccountWithSanVO> queryAccount(Long current, Long size, String keyword) {
        var data = accountMapper.selectPage(new Page<>(current, size), Wrappers.<AccountEntity>lambdaQuery()
                .eq(AccountEntity::getId, keyword));

        if (data.getRecords().size() == 0) {
            data = accountMapper.selectPage(new Page<>(current, size), Wrappers.<AccountEntity>lambdaQuery()
                    .like(AccountEntity::getAccount, keyword));
        }
        if (data.getRecords().size() == 0) {
            data = accountMapper.selectPage(new Page<>(current, size), Wrappers.<AccountEntity>lambdaQuery()
                    .like(AccountEntity::getName, keyword));
        }

        return getAccountWithSanVOPageQueryVO(data);
    }

    @Override
    public void resetAccountRefresh(Long id, Integer num) {
        var account = accountMapper.selectById(id);
        if (account != null) {
            account.setRefresh(num);
            accountMapper.updateById(account);
        }
    }

    @Override
    public String forceFightAccount(Long id, boolean isAdmin) {
        var account = accountMapper.selectById(id);
        if (account == null) {
            return "账号不存在";
        }
        //检查先决条件
        if (!isAdmin) {
            if (account.getDelete() == 1 || account.getExpireTime().isBefore(LocalDateTime.now())) {
                return "账号已到期或失效";
            }
            if (account.getFreeze() == 1) {
                return "请先解冻再执行操作";
            }
        } else {
            account.setFreeze(0);
        }
        //插队检查
        for (AccountEntity freeTask : dynamicInfo.getFreeTaskList()) {
            if (freeTask.getId().equals(id)) {
                var freeListIterator = dynamicInfo.getFreeTaskList().iterator();
                while (freeListIterator.hasNext()) {
                    var insertTask = freeListIterator.next();
                    if (insertTask.getId().equals(id)) {
                        freeListIterator.remove();
                        dynamicInfo.getFreeTaskList().add(0, insertTask);
                        return "插队成功";
                    }
                }
            }
        }
        //上锁检查
        for (LockTask lockTask : dynamicInfo.getLockTaskList()) {
            if (lockTask.getAccount().getId().equals(id)) {
                return "已经在作战中";
            }
        }
        //资格检查
        if (!isAdmin) {
            if (account.getRefresh() < 1) {
                return "今日刷新次数已达上限，每天零点刷新，明天再来看看吧";
            }
        }
        //执行
        dynamicInfo.getFreeTaskList().add(0, account);
        dynamicInfo.getUserSanList().put(id, 0);
        account = accountMapper.selectById(id);
        account.setRefresh(account.getRefresh() - 1);
        accountMapper.updateById(account);
        return "立即开始作战成功，等待分配作战服务器";
    }

    @Override
    public String resetAccountDynamicInfo(Long id) {
        var account = accountMapper.selectById(id);
        if (account == null || account.getDelete() == 1 || account.getExpireTime().isBefore(LocalDateTime.now())) {

            return "不在激活状态，无需修复";

        }

        //停止作战
        taskService.forceHaltTask(id);

        //重置动态数据
        dynamicInfo.getUserSanList().put(id, 135);
        dynamicInfo.getUserMaxSanList().put(id, 135);

        return "重置成功";
    }

    @NotNull
    public PageQueryVO<AccountWithSanVO> getAccountWithSanVOPageQueryVO(Page<AccountEntity> data) {
        var result = new PageQueryVO<AccountWithSanVO>();
        result.setCurrent(data.getCurrent());
        result.setTotal(data.getPages());

        for (AccountEntity user : data.getRecords()) {
            if (dynamicInfo.getUserSanList().containsKey(user.getId())) {
                result.getRecords().add(AccountConvert.INSTANCE.toAccountWithSanVO(user, dynamicInfo.getUserSanList().get(user.getId()) + "/" + dynamicInfo.getUserMaxSanList()
                        .get(user.getId())));
            } else {
                result.getRecords().add(AccountConvert.INSTANCE.toAccountWithSanVO(user, ""));
            }
        }
        return result;
    }


}
