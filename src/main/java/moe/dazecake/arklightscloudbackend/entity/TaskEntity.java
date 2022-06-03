package moe.dazecake.arklightscloudbackend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "回执")
public class TaskEntity {

    @Schema(description = "任务列表")
    ArrayList<HashMap<Integer, String>> list;

}
