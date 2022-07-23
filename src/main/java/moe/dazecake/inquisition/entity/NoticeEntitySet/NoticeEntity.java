package moe.dazecake.inquisition.entity.NoticeEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeEntity {
    private WXUID wxUID;
    private QQ qq;
    private Mail mail;
}
