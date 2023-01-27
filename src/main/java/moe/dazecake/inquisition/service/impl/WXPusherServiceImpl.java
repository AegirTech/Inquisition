package moe.dazecake.inquisition.service.impl;

import com.zjiecode.wxpusher.client.WxPusher;
import com.zjiecode.wxpusher.client.bean.Message;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.service.intf.WXPusherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WXPusherServiceImpl implements WXPusherService {

    @Value("${wx-pusher.app-token:}")
    private String appToken;

    @Override
    public void push(int type, String content, String uid, String url) {
        Message message = new Message();
        message.setAppToken(appToken);
        message.setContentType(type);
        message.setContent(content);
        message.setUid(uid);
        if (url != null) {
            message.setUrl(url);
        }
        WxPusher.send(message);
    }
}
