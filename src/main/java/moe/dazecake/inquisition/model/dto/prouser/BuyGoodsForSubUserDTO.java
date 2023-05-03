package moe.dazecake.inquisition.model.dto.prouser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyGoodsForSubUserDTO {
    Long subUserId;

    Long goodsId;
}
