package moe.dazecake.inquisition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.GsonTypeHandler;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsNotNull;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.dazecake.inquisition.entity.ActivationDateSet.ActivationDate;
import moe.dazecake.inquisition.entity.ConfigEntitySet.ConfigEntity;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "account", autoResultMap = true)
@Schema(description = "账户配置")
public class AccountEntity {

    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @IsNotNull
    @Column(name = "id", comment = "id")
    @Schema(description = "id")
    Long id;//ID

    @Column(name = "name", comment = "名称")
    @Schema(description = "名称")
    String name;//名称

    @Column(name = "account", comment = "账号")
    @Schema(description = "账号")
    String account;//账号

    @Column(name = "password", comment = "密码")
    @Schema(description = "密码")
    String password;//密码

    @Column(name = "server", comment = "服务器类型")
    @Schema(description = "服务器类型")
    Long server;//服务器类型 0:官服 1:B服

    @Column(name = "task_type", comment = "任务类型")
    @Schema(description = "任务类型")
    String taskType;//任务类型

    @Column(name = "config", comment = "配置", type = MySqlTypeConstant.JSON)
    @Schema(description = "配置")
    @TableField(typeHandler = GsonTypeHandler.class)
    ConfigEntity config;

    @Column(name = "active", comment = "激活时间", type = MySqlTypeConstant.JSON)
    @Schema(description = "激活时间")
    @TableField(typeHandler = GsonTypeHandler.class)
    ActivationDate active;

    @Column(name = "expire_time", comment = "过期时间")
    @Schema(description = "过期时间")
    LocalDateTime expireTime;//过期时间

    @Column(name = "delete", comment = "逻辑删除")
    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;


}
