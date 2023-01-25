package moe.dazecake.inquisition.service.intf;

public interface EmailService {

    void sendSimpleMail(String to, String subject, String content);

    void sendHtmlMail(String to, String subject, String content);

}
