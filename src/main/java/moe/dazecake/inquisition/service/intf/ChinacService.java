package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.chinac.ChinacPageListEntity;
import moe.dazecake.inquisition.model.dto.chinac.ChinacPhoneEntity;
import moe.dazecake.inquisition.model.dto.chinac.ChinacResult;
import okhttp3.Request;

import java.util.ArrayList;
import java.util.HashMap;

// 华云云手机API对接
public interface ChinacService {

    /**
     * 创建附带签名的请求Url
     *
     * @param queryMap 参数集合
     * @return HttpUrl
     */
    Request createRequest(HashMap<String, Object> queryMap);

    Request createRequest(HashMap<String, Object> queryMap, String url);

    //【生命周期管理】==========================================

    /**
     * <a href="https://docs-api.chinac.com/product/AncLc/api/OpenCloudPhone">创建云手机</a>
     *
     * @param Region            机房标识
     * @param CloudPhoneImageId 云手机镜像ID
     * @param PayType           付费类型 PREPAID：包年包月 ONDEMAND ：按量付费
     * @param ProductModelId    型号ID
     * @param Period            购买年限周期 PayType为包年包月时，必传，取值范围: 1,2,3,6，单位:月; PayType为按量付费时，不传
     * @param GroupId           分组ID，可以通过查询云手机分组接口获取
     * @param NetworkPacketId   专属网络ID，可以通过查询专属网络接口查询，有传表示加入专属网络
     * @param Num               开通数量，默认为1，取值范围1-50
     * @return ArrayList<String> 创建的云手机uid数组
     */
    ArrayList<String> createDevice(
            String Region,
            String CloudPhoneImageId,
            String PayType,
            Integer ProductModelId,
            Integer Period,
            String GroupId,
            String NetworkPacketId,
            Integer Num
    );

    /**
     * <a href="https://docs-api.chinac.com/product/AncLc/api/ListCloudPhone">查询云手机</a>
     *
     * @param PageNo   页码，默认第1页
     * @param PageSize 每页数量，默认10条
     * @return ArrayList<ChinacPhoneEntity> 云手机列表
     */
    ChinacResult<ChinacPageListEntity> queryDeviceList(
            Integer PageNo,
            Integer PageSize
    );

    /**
     * 获取所有云手机列表
     *
     * @return ArrayList<ChinacPhoneEntity> 云手机列表
     */
    ArrayList<ChinacPhoneEntity> queryAllDeviceList();

    /**
     * <a href="https://docs-api.chinac.com/product/AncLc/api/RenewCloudPhone">续费云手机</a>
     *
     * @param Region 机房标识，取值参见地域列表
     * @param Id     云手机ID
     * @param Period 续费时长，取值范围: 1,2,3,6，单位:月
     * @return boolean 是否续费成功
     */
    boolean renewDevice(
            String Region,
            String Id,
            Integer Period
    );

    //【云手机操作相关】==========================================

    String getDeviceRemoteControlUrl(String Region,
                                     String Id,
                                     Integer Duration,
                                     boolean AutoDisconnect,
                                     boolean AllowGroupControl,
                                     ArrayList<String> SalveCloudPhoneIds);

    //【云手机直连操作】==========================================

    HashMap<String, String> getDeviceControlInfo(ArrayList<String> Ids, String Region);

    HashMap<String, String> getDeviceScreenshot(ArrayList<String> Ids, String Region);

}
