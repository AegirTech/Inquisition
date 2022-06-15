package moe.dazecake.arklightscloudbackend.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.annotation.Login;
import moe.dazecake.arklightscloudbackend.entity.DeviceEntity;
import moe.dazecake.arklightscloudbackend.entity.LogEntity;
import moe.dazecake.arklightscloudbackend.mapper.DeviceMapper;
import moe.dazecake.arklightscloudbackend.mapper.LogMapper;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Tag(name = "日志接口")
@ResponseBody
@RestController
public class LogController {
    @Resource
    LogMapper logMapper;

    @Resource
    DeviceMapper deviceMapper;

    @Operation(summary = "增加日志")
    @PostMapping("/addLog")
    public Result<String> addLog(@RequestBody LogEntity logEntity, String deviceToken) {
        Result<String> result = new Result<>();
        logEntity.setId(0L);

        if (Objects.equals(deviceToken, "system")) {
            logMapper.insert(logEntity);
            return null;
        } else {
            var device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery().eq(DeviceEntity::getDeviceToken, deviceToken));

            if (device != null) {
                logEntity.setTime(LocalDateTime.now());
                logMapper.insert(logEntity);

                result.setCode(200)
                        .setMsg("success")
                        .setData(null);
            } else {
                result.setCode(403)
                        .setMsg("fail")
                        .setData(null);
            }
        }

        return result;
    }

    @Login
    @Operation(summary = "删除日志")
    @PostMapping("/delLog")
    public Result<String> delLog(Long id) {
        Result<String> result = new Result<>();

        var logEntity = logMapper.selectById(id);

        if (logEntity != null) {
            logEntity.setDelete(1);
            logMapper.updateById(logEntity);
            result.setCode(200)
                    .setMsg("success")
                    .setData(null);
        } else {
            result.setCode(404)
                    .setMsg("Unable to find this log")
                    .setData(null);
        }
        return result;
    }

    @Login
    @Operation(summary = "查询日志")
    @GetMapping("/showLog")
    public Result<ArrayList<LogEntity>> showLog(Long current, Long size) {
        Result<ArrayList<LogEntity>> result = new Result<>();
        result.setData(new ArrayList<>());

        //降序分页查找
        var data = logMapper.selectPage(new Page<>(current, size), Wrappers.<LogEntity>lambdaQuery().orderByDesc(LogEntity::getId));
        result.setCode(200)
                .setMsg("success")
                .getData()
                .addAll(data.getRecords());

        return result;
    }
}
