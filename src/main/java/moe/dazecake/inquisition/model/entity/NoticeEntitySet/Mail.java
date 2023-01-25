package moe.dazecake.inquisition.model.entity.NoticeEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mail {
    private String text = "";
    private Boolean enable = false;
}