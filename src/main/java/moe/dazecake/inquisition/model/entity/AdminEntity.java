package moe.dazecake.inquisition.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsNotNull;
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

    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @IsNotNull
    @Column(name = "id", comment = "ID")
    @Schema(description = "id")
    Long id;

    @Column(name = "username", comment = "账号")
    @Schema(description = "账号")
    String username;

    @Column(name = "password", comment = "密码")
    @Schema(description = "密码")
    String password;

    @Column(name = "permission", comment = "权限")
    @Schema(description = "权限")
    String permission;

    @Column(name = "notice", comment = "通知")
    @Schema(description = "通知")
    String notice;

    @Column(name = "delete", comment = "逻辑删除")
    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;
}
