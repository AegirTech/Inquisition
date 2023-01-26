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
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("device")
@Schema(description = "设备配置")
public class DeviceEntity {

    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @IsNotNull
    @Column(name = "id", comment = "ID")
    @Schema(description = "id")
    Long id;

    @Column(name = "device_name", comment = "设备名称")
    @Schema(description = "设备名称")
    String deviceName;

    @Column(name = "device_token", comment = "设备token")
    @Schema(description = "设备token")
    String deviceToken;

    @Column(name = "chinac", comment = "华云设备")
    @Schema(description = "华云设备")
    Integer chinac;

    @Column(name = "region", comment = "华云设备地域")
    @Schema(description = "华云设备地域")
    String region;

    @Column(name = "expire_time", comment = "过期时间")
    @Schema(description = "过期时间")
    LocalDateTime expireTime;

    @Column(name = "delete", comment = "逻辑删除")
    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;

}
