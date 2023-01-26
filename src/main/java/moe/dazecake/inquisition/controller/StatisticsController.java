package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.service.impl.StatisticsServiceImpl;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

@Tag(name = "统计接口")
@ResponseBody
@RestController
public class StatisticsController {

    @Resource
    StatisticsServiceImpl statisticsService;

    @Login
    @Operation(summary = "获取概览统计数据")
    @GetMapping("/getStatistics")
    public Result<HashMap<String, Object>> getStatistics() {
        return statisticsService.getStatistics();
    }

}
