package moe.dazecake.inquisition.model.dto.cdk;

import lombok.Data;

@Data
public class CreateCDKDTO {
    private String type;

    private Integer param;

    private String tag;

    private boolean isAgent;

    private Long agent;

    private Integer count;
}
