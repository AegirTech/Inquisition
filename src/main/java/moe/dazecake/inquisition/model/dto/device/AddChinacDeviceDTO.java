package moe.dazecake.inquisition.model.dto.device;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddChinacDeviceDTO {
    private String deviceName;

    private String deviceToken;

    private Integer chinac;

    private String region;

    private LocalDateTime expireTime;
}
