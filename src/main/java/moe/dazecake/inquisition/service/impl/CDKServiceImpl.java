package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.constant.enums.CDKWrapper;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.CDKMapper;
import moe.dazecake.inquisition.mapper.mapstruct.CDKConvert;
import moe.dazecake.inquisition.model.dto.cdk.CDKDTO;
import moe.dazecake.inquisition.model.dto.cdk.CreateCDKDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.CDKEntity;
import moe.dazecake.inquisition.model.vo.cdk.CDKListVO;
import moe.dazecake.inquisition.service.intf.CDKService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CDKServiceImpl implements CDKService {

    @Resource
    AccountMapper accountMapper;

    @Resource
    CDKMapper cdkMapper;

    @Resource
    LogServiceImpl logService;

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    AccountServiceImpl accountService;

    @Override
    public Result<String> activateCDK(Long id, String cdk) {

        var accountEntity = accountMapper.selectById(id);
        var cdkEntity = cdkMapper.selectOne(
                Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getCdk, cdk)
                        .eq(CDKEntity::getUsed, 0)
        );

        if (accountEntity == null) {
            return Result.notFound("账号不存在");
        }

        if (cdkEntity == null) {
            return Result.notFound("激活码不存在或已使用");
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
        accountEntity.setUpdateTime(LocalDateTime.now());

        cdkMapper.updateById(cdkEntity);
        accountMapper.updateById(accountEntity);

        dynamicInfo.setUserSan(accountEntity.getId(), 135, 135);

        return Result.success("激活成功");
    }

    @Override
    public Result<String> createUserByCDK(AccountEntity accountEntity, String cdk) {

        var cdkEntity = cdkMapper.selectOne(
                Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getCdk, cdk)
                        .eq(CDKEntity::getUsed, 0)
        );

        if (cdkEntity == null) {
            return Result.notFound("激活码不存在或已使用");
        }

        if ("daily".equals(cdkEntity.getType())) {
            accountEntity.setExpireTime(LocalDateTime.now().plusDays(cdkEntity.getParam()));
        } else {
            throw new IllegalStateException("Unexpected value: " + cdkEntity.getType());
        }

        cdkEntity.setUsed(1);
        if (cdkEntity.getIsAgent() == 1) {
            accountEntity.setAgent(cdkEntity.getAgent());
        } else {
            accountEntity.setAgent(0L);
        }

        accountEntity.setCreateTime(LocalDateTime.now());
        accountEntity.setUpdateTime(LocalDateTime.now());

        cdkMapper.updateById(cdkEntity);
        logService.logInfo("CDK使用", "使用CDK " + cdk + " 创建了账号 " + accountEntity.getAccount());
        accountMapper.insert(accountEntity);


        var account = accountMapper.selectOne(
                Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAccount, accountEntity.getAccount())
                        .eq(AccountEntity::getPassword, accountEntity.getPassword())
        );
        accountService.forceFightAccount(account.getId(), true);

        return Result.success("创建成功");
    }

    @Override
    public Result<String> createCDK(CreateCDKDTO createCDKDTO) {
        ArrayList<CDKEntity> newCDKList = new ArrayList<>();
        for (int i = 0; i < createCDKDTO.getCount(); i++) {
            var cdkEntity = new CDKEntity();
            cdkEntity.setId(0L);
            cdkEntity.setCdk(RandomStringUtils.randomAlphabetic(32));
            cdkEntity.setType(createCDKDTO.getType());
            cdkEntity.setParam(createCDKDTO.getParam());
            cdkEntity.setTag(createCDKDTO.getTag());
            cdkEntity.setIsAgent(createCDKDTO.getIsAgent() ? 1 : 0);
            cdkEntity.setAgent(createCDKDTO.getAgent());
            cdkEntity.setUsed(0);
            newCDKList.add(cdkEntity);
        }
        newCDKList.forEach(cdkMapper::insert);
        return Result.success("创建成功");
    }

    @Override
    public LambdaQueryWrapper<CDKEntity> createCDKWrapper(CDKWrapper cdkWrapper, String keyword) {
        switch (cdkWrapper) {
            case TYPE:
                return Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getType, keyword);
            case TAG:
                return Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getTag, keyword);
            case AGENT:
                return Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getAgent, keyword)
                        .eq(CDKEntity::getUsed, 0);
        }
        return null;
    }

    @Override
    public Result<CDKListVO> queryCDKList(CDKWrapper cdkWrapper, String keyword) {
        var cdkList = cdkMapper.selectList(createCDKWrapper(cdkWrapper, keyword));
        List<CDKDTO> list = new ArrayList<>();
        cdkList.forEach(cdk -> list.add(CDKConvert.INSTANCE.toCDKDTO(cdk)));
        var cdkListVO = new CDKListVO();
        cdkListVO.setCdkList(list);

        return Result.success(cdkListVO, "查询成功");
    }
}
