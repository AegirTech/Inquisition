package moe.dazecake.inquisition.entity.NoticeEntitySet;

import lombok.Data;

@Data
public class WechatCallbackEntity {
    private String action;
    private CallbackData data;
}
