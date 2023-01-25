package moe.dazecake.inquisition.model.dto.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliLoginRes {
    private String requestId;
    private String timestamp;
    private int code;
    private String auth_name;
    private int realname_verified;
    private int remind_status;
    private int h5_paid_download;
    private String h5_paid_download_sign;
    private String face;
    private String s_face;
    private String uname;
    private String access_key;
    private long uid;
    private long expires;
    private String server_message;
}
