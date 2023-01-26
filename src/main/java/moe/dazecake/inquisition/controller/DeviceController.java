package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.model.dto.device.*;
import moe.dazecake.inquisition.model.vo.device.DeviceScreenshotVO;
import moe.dazecake.inquisition.model.vo.device.DeviceVO;
import moe.dazecake.inquisition.model.vo.device.LoadDeviceVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.impl.DeviceServiceImpl;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Tag(name = "设备接口")
@ResponseBody
@RestController
public class DeviceController {

    @Resource
    DeviceServiceImpl deviceService;

    @Login
    @Operation(summary = "增加设备")
    @PostMapping("/addDevice")
    public Result<String> addDevice(@RequestBody AddCommonDeviceDTO addCommonDeviceDTO) {
        deviceService.addCommonDevice(addCommonDeviceDTO);
        return Result.success("添加成功");
    }

    @Login
    @Operation(summary = "删除设备")
    @PostMapping("/delDevice")
    public Result<String> delDevice(@RequestBody DeviceIDDTO deviceIDDTO) {
        deviceService.deleteDevice(deviceIDDTO.getId());
        return Result.success("删除成功");
    }

    @Login
    @Operation(summary = "分页查询库存设备")
    @GetMapping("/showInventoryDevice")
    public Result<PageQueryVO<DeviceVO>> showInventoryDevice(Long current, Long size) {
        return Result.success(deviceService.queryDevice(current, size), "查询成功");
    }

    @Login
    @Operation(summary = "查询已载入设备")
    @GetMapping("/showLoadedDevice")
    public Result<LoadDeviceVO> showLoadedDevice() {
        return Result.success(deviceService.getLoadDevice(), "查询成功");
    }

    @Login
    @Operation(summary = "更新设备")
    @PostMapping("/updateDevice")
    public Result<String> updateDevice(@RequestBody UpdateDeviceDTO updateDeviceDTO) {
        deviceService.updateDevice(updateDeviceDTO);
        return Result.success("更新成功");
    }

    @Login
    @Operation(summary = "通过设备token获取设备信息")
    @GetMapping("/getDeviceByToken")
    public Result<DeviceVO> getDeviceByToken(String deviceToken) {
        return deviceService.getDevice(deviceToken);
    }

    @Login
    @Operation(summary = "通过设备token获取华云设备实时截图参数")
    @PostMapping("/getDeviceScreenshotInfo")
    public Result<DeviceScreenshotVO> getDeviceScreenshotInfo(@RequestBody GroupChinacDeviceDTO groupChinacDeviceDTO) {
        return Result.success(deviceService.getGroupChinacDeviceScreenshot(groupChinacDeviceDTO), "获取成功");
    }

    @Login
    @Operation(summary = "通过设备token获取华云设备远程控制url")
    @PostMapping("/getDeviceRemoteControlUrl")
    public Result<String> getDeviceRemoteControlUrl(@RequestBody DeviceTokenDTO deviceTokenDTO) {
        return deviceService.getChinacRemoteControlUrl(deviceTokenDTO.getToken());
    }

}
