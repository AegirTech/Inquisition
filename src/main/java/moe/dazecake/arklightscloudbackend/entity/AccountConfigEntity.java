package moe.dazecake.arklightscloudbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("account_config")
public class AccountConfigEntity {

    @TableId
    Long id;//ID

    String account;//账号

    String password;//密码

    int sever;//服务器类型 0:官服 1:B服

    String name;//名称

    String config;//json配置

    String belong;//所属速通主机

    LocalDateTime expireTime;//过期时间


}
