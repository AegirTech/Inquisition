package moe.dazecake.inquisition.service.impl;

import com.zjiecode.wxpusher.client.bean.Message;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.mapper.AccountMapper;
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

    @Resource
    AccountMapper accountMapper;

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
                //正则匹配是否为邮箱地址格式
                if (!account.getNotice().getMail().getText().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                    log.info("【审判庭】 邮件推送失败 " + account.getAccount() + ": " + account.getNotice().getMail().getText() + " 不是一个有效的邮箱地址");
                    account.getNotice().getMail().setEnable(false);
                    accountMapper.updateById(account);
                    return;
                }
                e.printStackTrace();
                log.warn("【审判庭】 邮件推送失败 " + account.getAccount() + ": " + account.getNotice().getMail().getText());
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
