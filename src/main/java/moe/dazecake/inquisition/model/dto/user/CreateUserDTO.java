package moe.dazecake.inquisition.model.dto.user;

import lombok.Data;

@Data
public class CreateUserDTO {
    private String username;

    private String account;

    private String password;

    private Integer server;
}
