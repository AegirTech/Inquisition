package moe.dazecake.inquisition.model.vo.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceScreenshotVO {
    HashMap<String, String> screenshotMap;
}
