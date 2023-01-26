package moe.dazecake.inquisition.model.vo.device;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class LoadDeviceVO {
    private ArrayList<HashMap<String, String>> loadDeviceList = new ArrayList<>();
}
