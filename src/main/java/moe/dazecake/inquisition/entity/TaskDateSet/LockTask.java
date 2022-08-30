package moe.dazecake.inquisition.entity.TaskDateSet;


import lombok.Data;
import moe.dazecake.inquisition.entity.AccountEntity;

import java.time.LocalDateTime;

@Data
public class LockTask {
    String deviceToken;

    AccountEntity account;

    LocalDateTime expirationTime;
}
