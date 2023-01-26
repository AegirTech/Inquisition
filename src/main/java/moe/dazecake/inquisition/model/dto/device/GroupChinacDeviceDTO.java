package moe.dazecake.inquisition.model.dto.device;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GroupChinacDeviceDTO {
    private ArrayList<String> tokenList;
    private String region;

}
