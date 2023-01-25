package moe.dazecake.inquisition.model.dto.HttpEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficialLoginRes {
    private int status;
    private String msg;
    private OfficialData data;
}