package moe.dazecake.inquisition.model.dto.prouser;

import lombok.Data;

@Data
public class CreateUserByProUserDTO {
    private String name;

    private String account;

    private String password;

    private Long server;

    private Integer days;
}
