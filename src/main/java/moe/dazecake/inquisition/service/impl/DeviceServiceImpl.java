package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import moe.dazecake.inquisition.constant.enums.TaskType;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.mapper.mapstruct.DeviceConvert;
import moe.dazecake.inquisition.model.dto.device.*;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.model.vo.device.DeviceScreenshotVO;
import moe.dazecake.inquisition.model.vo.device.DeviceVO;
import moe.dazecake.inquisition.model.vo.device.LoadDevice;
import moe.dazecake.inquisition.model.vo.device.LoadDeviceVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.intf.DeviceService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    DeviceMapper deviceMapper;

    @Resource
    ChinacServiceImpl chinacService;

    @Resource
    AccountMapper accountMapper;

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
            var releaseUsers = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                    .eq(AccountEntity::getServer, 1)
                    .like(AccountEntity::getBLimitDevice, deviceEntity.getDeviceToken()));
            for (AccountEntity releaseUser : releaseUsers) {
                releaseUser.getBLimitDevice().remove(deviceEntity.getDeviceToken());
                accountMapper.updateById(releaseUser);
            }
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
            var loadDevice = new LoadDevice();
            loadDevice.setId(device.getId());
            loadDevice.setDeviceName(device.getDeviceName());
            loadDevice.setDeviceToken(device.getDeviceToken());
            loadDevice.setWorkScope(device.getWorkScope());
            loadDevice.setChinac(device.getChinac());
            loadDevice.setRegion(device.getRegion());
            loadDevice.setExpireTime(device.getExpireTime());
            loadDevice.setDelete(device.getDelete());
            loadDevice.setStatus(dynamicInfo.getDeviceStatusMap().get(device.getDeviceToken()));
            result.getLoadDeviceList().add(loadDevice);
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
    public Result<Boolean> isScopeDeviceFree(TaskType type) {
        var deviceList = deviceMapper.selectList(Wrappers.<DeviceEntity>lambdaQuery()
                .eq(DeviceEntity::getDelete, 0)
                .like(DeviceEntity::getWorkScope, type.getType())
        );
        for (DeviceEntity device : deviceList) {
            if (dynamicInfo.getDeviceStatusMap().get(device.getDeviceToken()) == 1) {
                return Result.success(true, "存在空闲设备");
            }
        }
        return Result.success(false, "无空闲设备");
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
