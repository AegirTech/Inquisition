package moe.dazecake.inquisition.model.dto.admin;

import lombok.Data;

@Data
public class ChangeAdminPasswordDTO {

    private String username;

    private String oldPassword;

    private String newPassword;
}
