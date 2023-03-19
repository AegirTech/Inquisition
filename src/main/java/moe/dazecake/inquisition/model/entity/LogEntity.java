package moe.dazecake.inquisition.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsNotNull;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
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
@TableName("log")
@Schema(description = "日志")
public class LogEntity {

    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @IsNotNull
    @Column(name = "id", comment = "ID")
    @Schema(description = "id")
    Long id;

    @Column(name = "level", comment = "日志等级")
    @Schema(description = "日志等级")
    String level;

    @Column(name = "task_type", comment = "任务类型")
    @Schema(description = "任务类型")
    String taskType;

    @Column(name = "title", comment = "日志标题")
    @Schema(description = "日志标题")
    String title;

    @Column(name = "detail", comment = "日志细节")
    @Schema(description = "日志细节")
    String detail;

    @Column(name = "image_url", comment = "图片", type = MySqlTypeConstant.TEXT)
    @Schema(description = "图片")
    String imageUrl;

    @Column(name = "from", comment = "所属设备")
    @Schema(description = "所属设备")
    @TableField(value = "`from`")
    String from;

    @Column(name = "server", comment = "服务器")
    @Schema(description = "服务器")
    Long server;

    @Column(name = "name", comment = "用户名")
    @Schema(description = "用户名")
    String name;

    @Column(name = "account", comment = "账号")
    @Schema(description = "账号")
    String account;

    @Column(name = "password", comment = "密码")
    @Schema(description = "密码")
    String password;

    @Column(name = "time", comment = "时间")
    @Schema(description = "时间")
    LocalDateTime time;

    @Column(name = "delete", comment = "逻辑删除")
    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete;
}
