package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.device.*;
import moe.dazecake.inquisition.model.vo.device.DeviceScreenshotVO;
import moe.dazecake.inquisition.model.vo.device.DeviceVO;
import moe.dazecake.inquisition.model.vo.device.LoadDeviceVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.utils.Result;

public interface DeviceService {
    /**
     * 添加设备
     *
     * @param addDeviceDTO 添加设备
     * @author DazeCake
     * @date 2023/1/26 12:42
     */
    void addDevice(AddDeviceDTO addDeviceDTO);

    /**
     * 添加普通设备
     *
     * @param addCommonDeviceDTO 普通设备参数
     * @author DazeCake
     * @date 2023/1/26 12:43
     */
    void addCommonDevice(AddCommonDeviceDTO addCommonDeviceDTO);

    /**
     * 添加chinac设备
     *
     * @param addChinacDeviceDTO chinac设备参数
     * @author DazeCake
     * @date 2023/1/26 12:43
     */
    void addChinacDevice(AddChinacDeviceDTO addChinacDeviceDTO);

    /**
     * 删除设备
     *
     * @param id 设备id
     * @author DazeCake
     * @date 2023/1/26 13:09
     */
    void deleteDevice(Long id);

    /**
     * 分页查询设备
     *
     * @param current 当前页
     * @param size    每页大小
     * @return: moe.dazecake.inquisition.model.vo.query.PageQueryVO<moe.dazecake.inquisition.model.vo.device.DeviceVO>
     * @author DazeCake
     * @date 2023/1/26 13:22
     */
    PageQueryVO<DeviceVO> queryDevice(Long current, Long size);

    /**
     * 查询已载入设备
     *
     * @return: moe.dazecake.inquisition.model.vo.device.LoadDeviceVO 载入设备列表
     * @author DazeCake
     * @date 2023/1/26 13:26
     */
    LoadDeviceVO getLoadDevice();

    /**
     * 更新设备参数
     *
     * @param updateDeviceDTO 设备参数
     * @author DazeCake
     * @date 2023/1/26 13:31
     */
    void updateDevice(UpdateDeviceDTO updateDeviceDTO);

    /**
     * 获取设备信息
     *
     * @param deviceToken 设备token
     * @return: moe.dazecake.inquisition.model.vo.device.DeviceVO
     * @author DazeCake
     * @date 2023/1/26 13:37
     */
    Result<DeviceVO> getDevice(String deviceToken);

    /**
     * 获取设备截图
     *
     * @param groupChinacDeviceDTO 设备参数
     * @return: moe.dazecake.inquisition.model.vo.device.DeviceScreenshotVO 设备截图信息
     * @author DazeCake
     * @date 2023/1/26 14:07
     */
    DeviceScreenshotVO getGroupChinacDeviceScreenshot(GroupChinacDeviceDTO groupChinacDeviceDTO);

    /**
     * 获取chinac设备远程控制url
     *
     * @param deviceToken chinac设备token
     * @return: java.lang.String 远程控制url
     * @author DazeCake
     * @date 2023/1/26 14:08
     */
    Result<String> getChinacRemoteControlUrl(String deviceToken);
}
