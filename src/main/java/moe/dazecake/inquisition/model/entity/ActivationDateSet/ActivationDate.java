package moe.dazecake.inquisition.model.entity.ActivationDateSet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivationDate {

    private ActivateConfig monday = new ActivateConfig();

    private ActivateConfig tuesday = new ActivateConfig();

    private ActivateConfig wednesday = new ActivateConfig();

    private ActivateConfig thursday = new ActivateConfig();

    private ActivateConfig friday = new ActivateConfig();

    private ActivateConfig saturday = new ActivateConfig();

    private ActivateConfig sunday = new ActivateConfig();

}
