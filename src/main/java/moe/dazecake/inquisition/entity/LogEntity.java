package moe.dazecake.inquisition.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@TableName("log")
@Schema(description = "日志")
public class LogEntity {
    @TableId
    @Schema(description = "id")
    Long id;

    @Schema(description = "日志等级")
    String level;

    @Schema(description = "任务类型")
    String taskType;

    @Schema(description = "日志标题")
    String title;

    @Schema(description = "日志细节")
    String detail;

    @Schema(description = "图片")
    String imageUrl;

    @Schema(description = "所属设备")
    @TableField(value = "`from`")
    String from;

    @Schema(description = "用户名")
    String name;

    @Schema(description = "账号")
    String account;

    @Schema(description = "密码")
    String password;

    @Schema(description = "时间")
    LocalDateTime time;

    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;
}
