package moe.dazecake.inquisition.entity;

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
@TableName("device")
@Schema(description = "设备配置")
public class DeviceEntity {

    @TableId
    @Schema(description = "id")
    Long id;

    @Schema(description = "设备名称")
    String deviceName;

    @Schema(description = "设备token")
    String deviceToken;

    @Schema(description = "到期时间")
    LocalDateTime expireTime;

    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;

}
