package moe.dazecake.inquisition.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("admin")
public class AdminEntity {
    @TableId
    @Schema(description = "id")
    Long id;

    @Schema(description = "账号")
    String userName;

    @Schema(description = "密码")
    String password;

    @Schema(description = "权限")
    String permission;

    @Schema(description = "通知")
    String notice;

    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;
}
