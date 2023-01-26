package moe.dazecake.inquisition.model.dto.device;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddCommonDeviceDTO {
    private String deviceName;

    private LocalDateTime expireTime;
}
