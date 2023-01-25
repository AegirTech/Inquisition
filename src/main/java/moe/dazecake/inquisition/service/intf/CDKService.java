package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.entity.AccountEntity;

public interface CDKService {

    int activateCDK(Long id, String cdk);

    int createUserByCDK(AccountEntity accountEntity, String cdk);
}
