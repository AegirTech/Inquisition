package moe.dazecake.inquisition.model.dto.prouser;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateProUserDTO {

    private String username;

    private String password;

    private String permission;

    private Double discount;

    private LocalDateTime expireTime;

}
