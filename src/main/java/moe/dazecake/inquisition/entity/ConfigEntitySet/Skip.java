package moe.dazecake.inquisition.entity.ConfigEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Skip {

    private boolean coin;
    private boolean beast;
    private boolean daily;
    private boolean sensitive;
    private boolean illusion;
    private boolean survive;

}