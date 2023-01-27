package moe.dazecake.inquisition.model.dto.user;

import lombok.Data;

@Data
public class CreateUserByCDKDTO extends CreateUserDTO {
    private String cdk;
}
