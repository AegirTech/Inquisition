package moe.dazecake.inquisition.model.dto.account;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddAccountDTO {

    private String name;

    private String account;

    private String password;

    private Long server;

    private LocalDateTime expireTime;
}
