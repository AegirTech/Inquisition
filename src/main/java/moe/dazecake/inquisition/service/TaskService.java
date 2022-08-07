package moe.dazecake.inquisition.service;

import moe.dazecake.inquisition.entity.AccountEntity;

public interface TaskService {
    boolean checkActivationTime(AccountEntity account);

    boolean checkFreeze(AccountEntity account);

    void lockTask(String deviceToken, AccountEntity account);

    void log(String deviceToken, AccountEntity account, String level, String title, String content, String imgUrl);

    void messagePush(AccountEntity account, String title, String content);

    void errorHandle(AccountEntity account, String deviceToken, String type);

    void forceHaltTask(AccountEntity account);

}
