package moe.dazecake.inquisition.model.dto.cdk;

import lombok.Data;

@Data
public class CreateCDKDTO {
    private String type;

    private String param;

    private String tag;

    private Boolean isAgent;

    private Long agent;

    private Integer count;
}
