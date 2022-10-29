package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.CDKEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.CDKMapper;
import moe.dazecake.inquisition.service.CDKService;
import moe.dazecake.inquisition.util.DynamicInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CDKServiceImpl implements CDKService {

    @Resource
    AccountMapper accountMapper;

    @Resource
    CDKMapper cdkMapper;

    @Resource
    DynamicInfo dynamicInfo;


    @Override
    public int activateCDK(Long id, String cdk) {

        var accountEntity = accountMapper.selectById(id);
        var cdkEntity = cdkMapper.selectOne(
                Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getCdk, cdk)
                        .eq(CDKEntity::getUsed, 0)
        );

        if (accountEntity == null) {
            log.error("Unable to activate CDK for a non-existent account");
            return 404;
        }

        if (cdkEntity == null) {
            log.error("Unable to activate a non-existent CDK");
            return 404;
        }

        if (accountEntity.getExpireTime().isBefore(LocalDateTime.now())) {
            accountEntity.setExpireTime(LocalDateTime.now());
        }

        if ("daily".equals(cdkEntity.getType())) {
            accountEntity.setExpireTime(accountEntity.getExpireTime().plusDays(cdkEntity.getParam()));
        } else {
            throw new IllegalStateException("Unexpected value: " + cdkEntity.getType());
        }

        cdkEntity.setUsed(1);
        if (cdkEntity.getIsAgent() == 1) {
            accountEntity.setAgent(cdkEntity.getAgent());
        }
        accountEntity.setFreeze(0);

        cdkMapper.updateById(cdkEntity);
        accountMapper.updateById(accountEntity);

        dynamicInfo.getUserSanList().put(accountEntity.getId(), 135);
        dynamicInfo.getUserMaxSanList().put(accountEntity.getId(), 135);

        return 200;
    }

    @Override
    public int createUserByCDK(AccountEntity accountEntity, String cdk) {

        var cdkEntity = cdkMapper.selectOne(
                Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getCdk, cdk)
                        .eq(CDKEntity::getUsed, 0)
        );

        if (cdkEntity == null) {
            return 404;
        }

        if ("daily".equals(cdkEntity.getType())) {
            accountEntity.setExpireTime(LocalDateTime.now().plusDays(cdkEntity.getParam()));
        } else {
            throw new IllegalStateException("Unexpected value: " + cdkEntity.getType());
        }

        cdkEntity.setUsed(1);
        if (cdkEntity.getIsAgent() == 1) {
            accountEntity.setAgent(cdkEntity.getAgent());
        }

        cdkMapper.updateById(cdkEntity);
        accountMapper.insert(accountEntity);

        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAccount, accountEntity.getAccount())
                        .eq(AccountEntity::getPassword, accountEntity.getPassword())
        );
        dynamicInfo.getUserSanList().put(account.getId(), 135);
        dynamicInfo.getUserMaxSanList().put(account.getId(), 135);

        return 200;
    }
}
