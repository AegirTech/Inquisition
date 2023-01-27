package moe.dazecake.inquisition.model.dto.user;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String account;

    private String password;
}
