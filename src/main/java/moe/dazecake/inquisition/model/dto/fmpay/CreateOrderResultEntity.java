package moe.dazecake.inquisition.model.dto.fmpay;

import lombok.Data;

import java.util.HashMap;

@Data
public class CreateOrderResultEntity {
    boolean success;
    String msg;
    int code;
    long timestamp;
    HashMap<String, String> data;
}
