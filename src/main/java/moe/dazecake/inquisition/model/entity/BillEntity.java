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
@TableName("bill")
@Schema(description = "账单流水")
public class BillEntity {

    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @IsNotNull
    @Column(name = "id", comment = "ID")
    @Schema(description = "id")
    Long id;

    @Column(name = "order_no", comment = "订单号")
    @Schema(description = "订单号")
    String orderNo;

    @Column(name = "platform_order_no", comment = "平台订单号")
    @Schema(description = "平台订单号")
    String platformOrderNo;

    @Column(name = "pay_type", comment = "支付方式")
    @Schema(description = "支付方式")
    String payType;

    @Column(name = "pay_url", comment = "支付地址")
    @Schema(description = "支付地址")
    String payUrl;

    @Column(name = "type", comment = "类型")
    @Schema(description = "类型")
    String type;

    @Column(name = "param", comment = "参数")
    @Schema(description = "参数")
    String param;

    @Column(name = "user_id", comment = "作用用户")
    @Schema(description = "作用用户")
    Long userId;

    @Column(name = "amount", comment = "金额")
    @Schema(description = "金额")
    Double amount;

    @Column(name = "actual_pay_amount", comment = "实际支付金额")
    @Schema(description = "实际支付金额")
    Double actualPayAmount;

    @Column(name = "state", comment = "付款状态")
    @Schema(description = "付款状态")
    Integer state;

    @Column(name = "update_time", comment = "更新时间")
    @Schema(description = "更新时间")
    LocalDateTime updateTime;


}
