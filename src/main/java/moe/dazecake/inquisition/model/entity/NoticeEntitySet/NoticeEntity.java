package moe.dazecake.inquisition.model.entity.NoticeEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeEntity {
    private WXUID wxUID = new WXUID();
    private QQ qq = new QQ();
    private Mail mail = new Mail();
}
