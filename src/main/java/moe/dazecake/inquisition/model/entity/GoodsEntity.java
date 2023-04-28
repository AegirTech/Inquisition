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
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "goods")
@Schema(description = "商品信息")
public class GoodsEntity {

    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @IsNotNull
    @Column(name = "id", comment = "id")
    @Schema(description = "id")
    Long id;

    @Column(name = "name", comment = "商品名称")
    @Schema(description = "商品名称")
    String name;

    @Column(name = "value", comment = "商品代号")
    @Schema(description = "商品代号")
    String value;

    @Column(name = "type", comment = "商品类型")
    @Schema(description = "商品类型")
    String type;

    @Column(name = "description", comment = "商品描述")
    @Schema(description = "商品描述")
    String description;

    @Column(name = "price", comment = "单价")
    @Schema(description = "单价")
    Double price;

    @Column(name = "on_sale", comment = "是否上架")
    @Schema(description = "是否上架")
    Integer onSale;
}
