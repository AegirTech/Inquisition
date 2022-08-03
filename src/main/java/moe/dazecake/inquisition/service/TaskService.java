package moe.dazecake.inquisition.service;

import moe.dazecake.inquisition.entity.AccountEntity;

public interface TaskService {
    boolean checkActivationTime(AccountEntity account);

    boolean checkFreeze(AccountEntity account);

    void lockTask(String deviceToken, AccountEntity account);

    void log(String deviceToken, AccountEntity account);

    void messagePush(AccountEntity account);

}
