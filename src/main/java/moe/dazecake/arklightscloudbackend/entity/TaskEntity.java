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
@Schema(description = "回执")
public class TaskEntity {

    @Schema(description = "操作码")
    Integer code;

    @Schema(description = "任务报文")
    String task;
}
