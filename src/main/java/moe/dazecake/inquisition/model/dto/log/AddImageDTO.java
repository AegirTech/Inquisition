package moe.dazecake.inquisition.model.dto.log;

import lombok.Data;

@Data
public class AddImageDTO {

    private String base64Image;

    private String deviceToken;

}
