package moe.dazecake.inquisition.service.impl;

import com.zjiecode.wxpusher.client.bean.Message;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.service.MessageService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

public class MessageServiceImpl implements MessageService {

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${wx-pusher.enable:fasle}")
    boolean enableWxPusher;

    @Value("${spring.mail.to}")
    String adminMail;

    @Resource
    EmailServiceImpl emailService;

    @Resource
    WXPusherServiceImpl wxPusherService;

    @Override
    public void push(AccountEntity account, String title, String content) {
        //微信推送
        if (enableWxPusher && account.getNotice().getWxUID().getEnable()) {
            wxPusherService.push(Message.CONTENT_TYPE_MD,
                    "# " + title + "\n\n" +
                            content,
                    account.getNotice().getWxUID().getText(),
                    null);
        }

        //邮件推送
        if (enableMail && account.getNotice().getMail().getEnable()) {
            emailService.sendSimpleMail(account.getNotice().getMail().getText(), title,
                    content);
        }
    }

    @Override
    public void pushAdmin(String title, String content) {
        if (enableMail) {
            emailService.sendSimpleMail(adminMail, title, content);
        }
    }
}
