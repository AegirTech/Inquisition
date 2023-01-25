package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.mapper.LogMapper;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.model.entity.LogEntity;
import moe.dazecake.inquisition.service.intf.LogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class LogServiceImpl implements LogService {
    @Resource
    LogMapper logMapper;

    @Resource
    DeviceMapper deviceMapper;

    @Override
    public void addLog(LogEntity logEntity, String deviceToken) {
        logEntity.setId(0L);

        if (Objects.equals(deviceToken, "system")) {
            logMapper.insert(logEntity);
        } else {
            var device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                    .eq(DeviceEntity::getDeviceToken, deviceToken));
            if (device != null && logEntity.getTitle() != null) {
                logEntity.setFrom(deviceToken)
                        .setTime(LocalDateTime.now());

                logMapper.insert(logEntity);
            }
        }
    }
}
