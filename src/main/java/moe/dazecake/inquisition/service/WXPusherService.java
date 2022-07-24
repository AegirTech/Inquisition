package moe.dazecake.inquisition.service;

public interface WXPusherService {
    void push(int type, String content, String uid, String url);
}
