package moe.dazecake.inquisition.model.dto.query;

import lombok.Data;

@Data
public class PageQueryDTO {
    private Long current;
    private Long size;
}
