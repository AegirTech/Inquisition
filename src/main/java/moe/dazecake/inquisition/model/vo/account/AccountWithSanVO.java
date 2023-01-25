package moe.dazecake.inquisition.model.vo.account;

import lombok.Data;
import moe.dazecake.inquisition.model.entity.AccountEntity;

@Data
public class AccountWithSanVO extends AccountEntity {
    private String san;
}
