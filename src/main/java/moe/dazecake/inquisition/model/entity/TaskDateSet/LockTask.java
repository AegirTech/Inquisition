package moe.dazecake.inquisition.model.entity.TaskDateSet;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.model.entity.AccountEntity;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockTask {
    String deviceToken = "";

    AccountEntity account = null;

    LocalDateTime expirationTime;
}
