package moe.dazecake.arklightscloudbackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("account")
@Schema(description = "账户配置")
public class AccountEntity {

    @TableId
    @Schema(description = "id")
    Long id;//ID

    @Schema(description = "名称")
    String name;//名称

    @Schema(description = "账号")
    String account;//账号

    @Schema(description = "密码")
    String password;//密码

    @Schema(description = "服务器类型")
    Integer server;//服务器类型 0:官服 1:B服

    @Schema(description = "配置")
    String config;//json配置

    @Schema(description = "所属主机")
    String belong;//所属速通主机

    @Schema(description = "过期时间")
    LocalDateTime expireTime;//过期时间

    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;


}
