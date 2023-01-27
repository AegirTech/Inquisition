package moe.dazecake.inquisition.model.dto.user;

import lombok.Data;

@Data
public class UpdateAccountAndPasswordDTO {
    private String account;

    private String password;

    private Long server;
}
