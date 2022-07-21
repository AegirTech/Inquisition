package moe.dazecake.inquisition.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("cdk")
@Schema(description = "CDK配置")
public class CDKEntity {

    @TableId
    @Schema(description = "id")
    Long id;

    @Schema(description = "卡密")
    String cdk;

    @Schema(description = "类型")
    String type;

    @Schema(description = "参数")
    Integer param;

    @Schema(description = "tag")
    String tag;

    @Schema(description = "是否使用")
    Integer used;
}
