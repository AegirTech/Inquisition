package moe.dazecake.arklightscloudbackend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "心跳")
public class HeartBeatEntity {

    @Schema(description = "状态")
    Integer status;//状态

    @Schema(description = "设备token")
    String deviceToken;//设备token
}
