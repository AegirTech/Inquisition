package moe.dazecake.inquisition.model.entity.ActivationDateSet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivateConfig {

    private boolean enable = true;

    private ArrayList<String> detail = new ArrayList<>();

}
