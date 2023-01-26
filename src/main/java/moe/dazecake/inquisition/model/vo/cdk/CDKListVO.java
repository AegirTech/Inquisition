package moe.dazecake.inquisition.model.vo.cdk;

import lombok.Data;
import moe.dazecake.inquisition.model.dto.cdk.CDKDTO;

import java.util.ArrayList;
import java.util.List;

@Data
public class CDKListVO {
    private List<CDKDTO> list = new ArrayList<>();
}
