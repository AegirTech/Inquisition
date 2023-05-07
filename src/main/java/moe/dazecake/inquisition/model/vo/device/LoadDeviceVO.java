package moe.dazecake.inquisition.model.vo.device;

import lombok.Data;

import java.util.ArrayList;

@Data
public class LoadDeviceVO {
    private ArrayList<LoadDevice> loadDeviceList = new ArrayList<>();
}
