package moe.dazecake.inquisition.service.intf;

public interface WXPusherService {
    void push(int type, String content, String uid, String url);
}
