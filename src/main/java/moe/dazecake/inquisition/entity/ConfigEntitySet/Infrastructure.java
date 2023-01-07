package moe.dazecake.inquisition.entity.ConfigEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Infrastructure {

    private boolean harvest;
    private boolean shift;
    private boolean acceleration;
    private boolean communication;
    private boolean deputy;

}