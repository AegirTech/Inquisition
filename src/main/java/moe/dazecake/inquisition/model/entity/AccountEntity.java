package moe.dazecake.inquisition.model.entity;

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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import moe.dazecake.inquisition.model.entity.ActivationDateSet.ActivationDate;
import moe.dazecake.inquisition.model.entity.ConfigEntitySet.ConfigEntity;
import moe.dazecake.inquisition.model.entity.NoticeEntitySet.NoticeEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

    @Column(name = "freeze", comment = "冻结")
    @Schema(description = "冻结")
    Integer freeze = 0;//冻结

    @Column(name = "server", comment = "服务器类型")
    @Schema(description = "服务器类型")
    Long server = 0L;//服务器类型 0:官服 1:B服

    @Column(name = "task_type", comment = "任务类型")
    @Schema(description = "任务类型")
    String taskType = "daily";//任务类型

    @Column(name = "config", comment = "配置", type = MySqlTypeConstant.JSON)
    @Schema(description = "配置")
    @TableField(typeHandler = GsonTypeHandler.class)
    ConfigEntity config = new ConfigEntity();

    @Column(name = "active", comment = "激活时间", type = MySqlTypeConstant.JSON)
    @Schema(description = "激活时间")
    @TableField(typeHandler = GsonTypeHandler.class)
    ActivationDate active = new ActivationDate();

    @Column(name = "notice", comment = "通知", type = MySqlTypeConstant.JSON)
    @Schema(description = "通知")
    @TableField(typeHandler = GsonTypeHandler.class)
    NoticeEntity notice = new NoticeEntity();

    @Column(name = "b_limit_device", comment = "B服限制设备", type = MySqlTypeConstant.JSON)
    @Schema(description = "B服限制设备")
    @TableField(typeHandler = GsonTypeHandler.class)
    ArrayList<String> bLimitDevice = new ArrayList<>();//B服限制设备

    @Column(name = "refresh", comment = "剩余刷新次数")
    @Schema(description = "剩余刷新次数")
    Integer refresh = 1;//剩余刷新次数

    @Column(name = "agent", comment = "代理商")
    @Schema(description = "代理商")
    Long agent;//代理商

    @Column(name = "create_time", comment = "创建时间")
    @Schema(description = "创建时间")
    LocalDateTime createTime = LocalDateTime.now();//创建时间

    @Column(name = "update_time", comment = "更新时间")
    @Schema(description = "更新时间")
    LocalDateTime updateTime = LocalDateTime.now();//更新时间

    @Column(name = "expire_time", comment = "过期时间")
    @Schema(description = "过期时间")
    LocalDateTime expireTime = LocalDateTime.now().plusDays(30);//过期时间

    @Column(name = "delete", comment = "逻辑删除")
    @Schema(description = "逻辑删除")
    @TableField(value = "`delete`")
    Integer delete = 0;


}
