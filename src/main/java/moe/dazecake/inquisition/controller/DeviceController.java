package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.entity.DeviceEntity;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.service.impl.ChinacServiceImpl;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Result;
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

    @Resource
    ChinacServiceImpl chinacService;

    @Login
    @Operation(summary = "增加设备")
    @PostMapping("/addDevice")
    public Result<DeviceEntity> addDevice(@RequestBody DeviceEntity deviceEntity) {
        Result<DeviceEntity> result = new Result<>();

        var deviceToken = RandomStringUtils.randomAlphabetic(16);
        deviceEntity.setDeviceToken(deviceToken)
                .setChinac(0)
                .setDelete(0);
        deviceMapper.insert(deviceEntity);
        dynamicInfo.getDeviceStatusMap().put(deviceToken, 0);

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
            dynamicInfo.getDeviceStatusMap().remove(deviceEntity.getDeviceToken());

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
    public Result<ArrayList<HashMap<String, String>>> showLoadedDevice() {
        Result<ArrayList<HashMap<String, String>>> result = new Result<>();
        result.setData(new ArrayList<>());

        var devices = deviceMapper.selectList(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDelete, 0)
        );

        for (DeviceEntity device : devices) {
            if (!dynamicInfo.getDeviceStatusMap().containsKey(device.getDeviceToken())) {
                dynamicInfo.getDeviceStatusMap().put(device.getDeviceToken(), 0);
            }

            if (device.getChinac() == null) {
                device.setChinac(0);
            }

            result.getData().add(new HashMap<>() {
                {
                    put("id", String.valueOf(device.getId()));
                    put("deviceName", device.getDeviceName());
                    put("deviceToken", device.getDeviceToken());
                    put("isChinac", device.getChinac().toString());
                    put("region", device.getRegion());
                    put("expireTime", String.valueOf(device.getExpireTime()));
                    put("status", String.valueOf(dynamicInfo.getDeviceStatusMap().get(device.getDeviceToken())));
                }
            });

        }

        result.setCode(200)
                .setMsg("success");

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

    @Login
    @Operation(summary = "通过设备token获取设备信息")
    @GetMapping("/getDeviceByToken")
    public Result<DeviceEntity> getDeviceByToken(String deviceToken) {
        Result<DeviceEntity> result = new Result<>();

        var device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDeviceToken, deviceToken)
        );

        if (device != null) {
            result.setCode(200)
                    .setMsg("success")
                    .setData(device);
        } else {
            result.setCode(403)
                    .setMsg("Unable to get a non-existent device")
                    .setData(null);
        }

        return result;
    }

    @Login
    @Operation(summary = "通过设备token获取华云设备实时截图参数")
    @PostMapping("/getDeviceScreenshotInfo")
    public Result<HashMap<String, String>> getDeviceScreenshotInfo(@RequestBody ArrayList<String> tokenList, String region) {
        Result<HashMap<String, String>> result = new Result<>();
        result.setData(chinacService.getDeviceScreenshot(tokenList, region));
        return result
                .setCode(200)
                .setMsg("success");
    }

    @Login
    @Operation(summary = "通过设备token获取华云设备远程控制url")
    @PostMapping("/getDeviceRemoteControlUrl")
    public Result<String> getDeviceRemoteControlUrl(String token) {
        Result<String> result = new Result<>();
        DeviceEntity device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDeviceToken, token));
        if (device == null || device.getChinac() != 1 || device.getDelete() == 1) {
            result.setData("");
        } else {
            result.setData(chinacService.getDeviceRemoteControlUrl(
                    device.getRegion(),
                    device.getDeviceToken(),
                    30,
                    false,
                    false,
                    null
            ));
        }
        return result.setCode(200)
                .setMsg("success");
    }

    @Login
    @Operation(summary = "获取华云设备组远程控制url")
    @PostMapping("/getGroupDeviceRemoteControlUrl")
    public Result<String> getGroupDeviceRemoteControlUrl(String token, @RequestBody ArrayList<String> tokenList) {
        Result<String> result = new Result<>();
        DeviceEntity device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDeviceToken, token));
        if (device == null || device.getChinac() != 1 || device.getDelete() == 1) {
            result.setData("");
        } else {
            result.setData(chinacService.getDeviceRemoteControlUrl(
                    device.getRegion(),
                    device.getDeviceToken(),
                    30,
                    false,
                    true,
                    tokenList
            ));
        }
        return result.setCode(200)
                .setMsg("success");
    }

}
