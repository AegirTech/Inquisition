package moe.dazecake.inquisition.model.entity.ConfigEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    private boolean enable;
    private boolean car;
    private boolean star4;
    private boolean star5;
    private boolean star6;
    private boolean other;

}