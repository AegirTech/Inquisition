package moe.dazecake.inquisition.service;

import moe.dazecake.inquisition.entity.LogEntity;

public interface LogService {
    void addLog(LogEntity logEntity, String deviceToken);
}
