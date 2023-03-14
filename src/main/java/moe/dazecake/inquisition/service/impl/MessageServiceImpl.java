package moe.dazecake.inquisition.service.impl;

import com.zjiecode.wxpusher.client.bean.Message;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.service.intf.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Value("${spring.mail.enable:false}")
    boolean enableMail;

    @Value("${wx-pusher.enable:false}")
    boolean enableWxPusher;

    @Value("${spring.mail.to:}")
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
            try {
                emailService.sendSimpleMail(account.getNotice().getMail().getText(), title,
                        content);
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("邮件推送失败 " + account.getAccount() + ": " + account.getNotice().getMail().getText());
            }
        }
    }

    @Override
    public void pushAdmin(String title, String content) {
        if (enableMail) {
            emailService.sendSimpleMail(adminMail, title, content);
        }
    }
}
