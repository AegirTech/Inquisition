package moe.dazecake.inquisition.model.entity.ConfigEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rogue {

    private Operator operator = new Operator(-1, 99, 1);
    private int level = 0;
    private int coin = 999;
    private Skip skip = new Skip(
            false,
            true,
            false,
            true,
            true,
            true
    );

}