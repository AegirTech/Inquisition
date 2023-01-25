package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.entity.AccountEntity;

public interface MessageService {
    void push(AccountEntity account, String title, String content);

    void pushAdmin(String title, String content);
}
