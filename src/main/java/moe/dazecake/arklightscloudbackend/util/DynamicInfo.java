package moe.dazecake.arklightscloudbackend.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicInfo {
    HashMap<String, Integer> deviceStatusMap;

    HashMap<String, ArrayList<AccountEntity>> taskSet;

    HashMap<String, ArrayList<HashMap<Integer, String>>> operateList;

}
