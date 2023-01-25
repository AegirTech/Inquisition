package moe.dazecake.inquisition.model.dto.chinac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChinacPageListEntity {

    HashMap<String, Integer> Page;

    ArrayList<ChinacPhoneEntity> List;

}
