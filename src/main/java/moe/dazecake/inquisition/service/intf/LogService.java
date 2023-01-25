package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.entity.LogEntity;

public interface LogService {
    void addLog(LogEntity logEntity, String deviceToken);
}
