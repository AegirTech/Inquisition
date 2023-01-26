package moe.dazecake.inquisition.model.dto.prouser;

import lombok.Data;

@Data
public class UpdateProUserPasswordDTO {
    private String oldPassword;

    private String newPassword;
}
