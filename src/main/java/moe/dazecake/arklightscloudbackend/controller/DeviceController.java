package moe.dazecake.arklightscloudbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.arklightscloudbackend.annotation.Login;
import moe.dazecake.arklightscloudbackend.entity.DeviceEntity;
import moe.dazecake.arklightscloudbackend.mapper.DeviceMapper;
import moe.dazecake.arklightscloudbackend.util.DynamicInfo;
import moe.dazecake.arklightscloudbackend.util.Result;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Tag(name = "设备接口")
@ResponseBody
@RestController
public class DeviceController {

    @Resource
    DeviceMapper deviceMapper;

    @Resource
    DynamicInfo dynamicInfo;

    @Login
    @Operation(summary = "增加设备")
    @PostMapping("/addDevice")
    public Result<DeviceEntity> addDevice(@RequestBody DeviceEntity deviceEntity) {
        Result<DeviceEntity> result = new Result<>();

        var deviceToken = RandomStringUtils.randomAlphabetic(16);
        deviceEntity.setDeviceToken(deviceToken);
        deviceMapper.insert(deviceEntity);

        result.setCode(200)
                .setMsg("success")
                .setData(deviceEntity);

        return result;
    }

    @Login
    @Operation(summary = "删除设备")
    @PostMapping("/delDevice")
    public Result<String> delDevice(Long id) {
        Result<String> result = new Result<>();
        var deviceEntity = deviceMapper.selectById(id);

        if (deviceEntity != null) {
            deviceEntity.setDelete(1);

            deviceMapper.updateById(deviceEntity);

            result.setCode(200)
                    .setMsg("success")
                    .setData(null);
        } else {
            result.setCode(403)
                    .setMsg("A nonexistent device cannot be deleted")
                    .setData(null);
        }

        return result;
    }

    @Login
    @Operation(summary = "分页查询库存设备")
    @GetMapping("/showInventoryDevice")
    public Result<ArrayList<DeviceEntity>> showInventoryDevice(Long current, Long size) {
        Result<ArrayList<DeviceEntity>> result = new Result<>();
        result.setData(new ArrayList<>());

        var data = deviceMapper.selectPage(new Page<>(current, size), null);
        result.setCode(200)
                .setMsg("success")
                .getData()
                .addAll(data.getRecords());

        return result;
    }

    @Login
    @Operation(summary = "查询已载入设备")
    @GetMapping("/showLoadedDevice")
    public Result<HashMap<String,Integer>> showLoadedDevice() {
        Result<HashMap<String,Integer>> result = new Result<>();
        result.setData(new HashMap<>());

        result.setCode(200)
                .setMsg("success")
                .setData(dynamicInfo.getDeviceStatusMap());

        return result;
    }

    @Login
    @Operation(summary = "更新设备")
    @PostMapping("/updateDevice")
    public Result<String> updateDevice(Long id, @RequestBody DeviceEntity deviceEntity) {
        Result<String> result = new Result<>();

        var device = deviceMapper.selectById(id);
        if (device != null) {
            device = deviceEntity;
            device.setId(id);
            deviceMapper.updateById(device);

            result.setCode(200)
                    .setMsg("success");

        } else {
            result.setCode(403)
                    .setMsg("Unable to update a non-existent device");

        }
        result.setData(null);
        return result;
    }


}
