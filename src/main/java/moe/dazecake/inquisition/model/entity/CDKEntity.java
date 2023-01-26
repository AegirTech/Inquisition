package moe.dazecake.inquisition.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cdk")
@Schema(description = "CDK配置")
public class CDKEntity {

    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @IsNotNull
    @Column(name = "id",comment = "ID")
    @Schema(description = "id")
    Long id;

    @Column(name = "cdk",comment = "卡密")
    @Schema(description = "卡密")
    String cdk;

    @Column(name = "type",comment = "类型")
    @Schema(description = "类型")
    String type;

    @Column(name = "param",comment = "参数")
    @Schema(description = "参数")
    Integer param;

    @Column(name = "tag",comment = "tag")
    @Schema(description = "tag")
    String tag;

    @Column(name = "is_agent", comment = "是否启用代理归属")
    @Schema(description = "是否启用代理归属")
    Integer isAgent;

    @Column(name = "agent",comment = "代理商ID")
    @Schema(description = "代理商ID")
    Long agent;

    @Column(name = "used",comment = "是否使用")
    @Schema(description = "是否使用")
    Integer used;
}
