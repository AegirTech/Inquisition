package moe.dazecake.inquisition.entity.HttpEntitySet;

import lombok.Data;

@Data
public class BiliPerLoginRes {
    private String requestId;
    private String timestamp;
    private int code;
    private String hash;
    private String cipher_key;
    private String server_message;
}
