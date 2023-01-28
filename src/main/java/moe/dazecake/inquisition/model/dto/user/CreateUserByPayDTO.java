package moe.dazecake.inquisition.model.dto.user;

import lombok.Data;

@Data
public class CreateUserByPayDTO extends CreateUserDTO {
    private String payType;

    private Long agent;
}
