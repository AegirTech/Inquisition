package moe.dazecake.inquisition.model.vo.device;

import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.model.entity.DeviceEntity;

@Data
@NoArgsConstructor
public class LoadDevice extends DeviceEntity {
    Integer status;
}
