package moe.dazecake.inquisition.model.dto.chinac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChinacPhoneEntity {
    private String Id;
    private String Name;
    private String Region;
    private String PayType;
    private String TaskStatus;
    private int IsEnable;
    private String CloudPhoneNetworkId;
    private String NetworkType;
    private String Eip;
    private String IntranetIp;
    private String Status;
    private String ProductStatus;
    private long ProductModelId;
    private String ProductModelName;
    private String DisplaySize;
    private String ProductType;
    private long LastStartTime;
    private long DueTime;
    private long CloseTime;
    private long CreateTime;
    private long UpdateTime;
}
