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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pro_user")
public class ProUserEntity {

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

    @Column(name = "balance", comment = "余额", decimalLength = 2)
    @Schema(description = "余额")
    Double balance;

    @Column(name = "discount", comment = "折扣系数", decimalLength = 2)
    @Schema(description = "折扣系数")
    Double discount;

    @Column(name = "authorization", comment = "授权码")
    @Schema(description = "授权码")
    String authorization;

    @Column(name = "expire_time", comment = "到期时间")
    @Schema(description = "到期时间")
    LocalDateTime expireTime;

    @Column(name = "delete", comment = "逻辑删除")
    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;
}
