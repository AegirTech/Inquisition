package moe.dazecake.inquisition.model.dto.chinac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashMap;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChinacScreenshotEntity {
    private long ResponseCode;
    private HashMap<String, String> ResponseData;
    private String ResponseMsg;
    private String ResponseDate;
    private String ResponseTaskId;
}
