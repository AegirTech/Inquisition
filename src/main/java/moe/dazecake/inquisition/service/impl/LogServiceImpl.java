package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.mapper.LogMapper;
import moe.dazecake.inquisition.mapper.mapstruct.LogConvert;
import moe.dazecake.inquisition.model.dto.log.AddImageDTO;
import moe.dazecake.inquisition.model.dto.log.AddLogDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.model.entity.LogEntity;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.intf.LogService;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Service
public class LogServiceImpl implements LogService {
    @Resource
    LogMapper logMapper;

    @Resource
    AccountMapper accountMapper;

    @Resource
    MessageServiceImpl messageService;

    @Resource
    DeviceMapper deviceMapper;

    @Resource
    ImageServiceImpl imageService;

    @Override
    public void addLog(AddLogDTO addLogDTO, boolean isSystem) {
        var logEntity = LogConvert.INSTANCE.toLogEntity(addLogDTO);
        logEntity.setId(0L);
        logEntity.setTime(LocalDateTime.now());
        logEntity.setDelete(0);
        if (isSystem) {
            logEntity.setTaskType("SYSTEM");
            logEntity.setFrom("SYSTEM");
        } else {
            specialScan(addLogDTO);
        }
        logMapper.insert(logEntity);
    }

    @Override
    public Result<String> uploadImage(AddImageDTO addImageDTO) {
        var device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDeviceToken, addImageDTO.getDeviceToken()));
        if (device == null) {
            return Result.notFound("设备不存在");
        }
        return imageService.uploadImageToCos(addImageDTO.getBase64Image());
    }

    @Override
    public void logInfo(String title, String detail) {
        var addLogDTO = new AddLogDTO();
        addLogDTO.setLevel("INFO");
        addLogDTO.setTitle(title);
        addLogDTO.setDetail(detail);
        addLog(addLogDTO, true);
    }

    @Override
    public void logWarn(String title, String detail) {
        var addLogDTO = new AddLogDTO();
        addLogDTO.setLevel("WARN");
        addLogDTO.setTitle(title);
        addLogDTO.setDetail(detail);
        addLog(addLogDTO, true);
    }

    @Override
    public void specialScan(AddLogDTO addLogDTO) {
        if (addLogDTO.getDetail().contains("高级资深干员")) {
            try {
                var luckyDog = accountMapper.selectOne(Wrappers.<AccountEntity>lambdaQuery()
                        .eq(AccountEntity::getAccount, addLogDTO.getAccount()));
                messageService.push(luckyDog, "高级资深干员提示", "恭喜你获得了高级资深干员！快上游戏看看吧！");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteLog(Long id) {
        logMapper.updateById(logMapper.selectById(id).setDelete(1));
    }

    @Override
    public PageQueryVO<LogDTO> queryAllLog(Long current, Long size) {
        //降序分页查找
        var data = logMapper.selectPage(new Page<>(current, size), Wrappers.<LogEntity>lambdaQuery()
                .orderByDesc(LogEntity::getId));
        return getLogPageQueryVO(data);
    }

    @Override
    public PageQueryVO<LogDTO> queryLogByAccount(String account, Long current, Long size) {
        var data = logMapper.selectPage(new Page<>(current, size), Wrappers.<LogEntity>lambdaQuery()
                .eq(LogEntity::getAccount, account)
                .orderByDesc(LogEntity::getId));
        return getLogPageQueryVO(data);
    }

    @NotNull
    public PageQueryVO<LogDTO> getLogPageQueryVO(Page<LogEntity> data) {
        var result = new PageQueryVO<LogDTO>();
        result.setCurrent(data.getCurrent());
        result.setPage(data.getPages());
        result.setTotal(data.getTotal());
        for (LogEntity record : data.getRecords()) {
            result.getRecords().add(LogConvert.INSTANCE.toLogDTO(record));
        }
        return result;
    }
}
