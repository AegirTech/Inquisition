package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.mapper.mapstruct.DeviceConvert;
import moe.dazecake.inquisition.model.dto.device.*;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.model.vo.device.DeviceScreenshotVO;
import moe.dazecake.inquisition.model.vo.device.DeviceVO;
import moe.dazecake.inquisition.model.vo.device.LoadDeviceVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.intf.DeviceService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    DeviceMapper deviceMapper;

    @Resource
    ChinacServiceImpl chinacService;

    @Override
    public void addDevice(AddDeviceDTO addDeviceDTO) {
        deviceMapper.insert(DeviceConvert.INSTANCE.toDeviceEntity(addDeviceDTO));
        dynamicInfo.getDeviceStatusMap().put(addDeviceDTO.getDeviceToken(), 0);
    }

    @Override
    public void addCommonDevice(AddCommonDeviceDTO addCommonDeviceDTO) {
        addDevice(DeviceConvert.INSTANCE.toAddDeviceDTO(addCommonDeviceDTO));
    }

    @Override
    public void addChinacDevice(AddChinacDeviceDTO addChinacDeviceDTO) {
        addDevice(DeviceConvert.INSTANCE.toAddDeviceDTO(addChinacDeviceDTO));
    }

    @Override
    public void deleteDevice(Long id) {
        var deviceEntity = deviceMapper.selectById(id);

        if (deviceEntity != null) {
            deviceEntity.setDelete(1);
            deviceMapper.updateById(deviceEntity);
            dynamicInfo.getDeviceStatusMap().remove(deviceEntity.getDeviceToken());
        }
    }

    @Override
    public PageQueryVO<DeviceVO> queryDevice(Long current, Long size) {
        var data = deviceMapper.selectPage(new Page<>(current, size), null);
        return getDevicePageQueryVO(data);
    }

    @Override
    public LoadDeviceVO getLoadDevice() {
        var result = new LoadDeviceVO();
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

            result.getLoadDeviceList().add(new HashMap<>() {
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
        return result;
    }

    @Override
    public void updateDevice(UpdateDeviceDTO updateDeviceDTO) {
        var deviceEntity = deviceMapper.selectById(updateDeviceDTO.getId());
        if (deviceEntity != null) {
            deviceEntity = DeviceConvert.INSTANCE.toDeviceEntity(updateDeviceDTO);
            deviceMapper.updateById(deviceEntity);
        }
    }

    @Override
    public Result<DeviceVO> getDevice(String deviceToken) {
        var device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDeviceToken, deviceToken)
        );

        if (device != null) {
            return Result.success(DeviceConvert.INSTANCE.toDeviceVO(device), "查询成功");
        } else {
            return Result.notFound("查询失败");
        }
    }

    @Override
    public DeviceScreenshotVO getGroupChinacDeviceScreenshot(GroupChinacDeviceDTO groupChinacDeviceDTO) {
        return new DeviceScreenshotVO(chinacService.getDeviceScreenshot(groupChinacDeviceDTO.getTokenList(), groupChinacDeviceDTO.getRegion()));
    }

    @Override
    public Result<String> getChinacRemoteControlUrl(String deviceToken) {
        DeviceEntity device = deviceMapper.selectOne(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDeviceToken, deviceToken));
        if (device == null || device.getChinac() != 1 || device.getDelete() == 1) {
            return Result.forbidden("设备不存在或不是Chinac设备");
        } else {
            return Result.success(chinacService.getDeviceRemoteControlUrl(
                    device.getRegion(),
                    device.getDeviceToken(),
                    30,
                    false,
                    false,
                    null
            ), "获取成功");
        }
    }

    private PageQueryVO<DeviceVO> getDevicePageQueryVO(Page<DeviceEntity> data) {
        var result = new PageQueryVO<DeviceVO>();
        result.setCurrent(data.getCurrent());
        result.setPage(data.getPages());
        result.setTotal(data.getTotal());
        for (DeviceEntity record : data.getRecords()) {
            result.getRecords().add(DeviceConvert.INSTANCE.toDeviceVO(record));
        }
        return result;
    }

}
