package moe.dazecake.inquisition.model.entity.ConfigEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntity {

    private Daily daily = new Daily();
    private Rogue rogue = new Rogue();

}