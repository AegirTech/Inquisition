package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.mapper.DeviceMapper;
import moe.dazecake.inquisition.model.dto.chinac.ChinacPageListEntity;
import moe.dazecake.inquisition.model.dto.chinac.ChinacPhoneEntity;
import moe.dazecake.inquisition.model.dto.chinac.ChinacResult;
import moe.dazecake.inquisition.model.dto.chinac.ChinacScreenshotEntity;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.service.intf.ChinacService;
import moe.dazecake.inquisition.utils.Encoder;
import moe.dazecake.inquisition.utils.UrlUtil;
import okhttp3.*;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ChinacServiceImpl implements ChinacService {

    private static final SimpleDateFormat UTCDateFormatter =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.CHINESE);

    @Value("${inquisition.chinac.AccessKey:}")
    private String AccessKey;

    @Value("${inquisition.chinac.AccessKeySecret:}")
    private String AccessKeySecret;

    @Value("${inquisition.chinac.Version:1.0}")
    private String Version;

    @Resource
    DeviceMapper deviceMapper;

    @Override
    public Request createRequest(HashMap<String, Object> queryMap) {
        return createRequest(queryMap, "https://api.chinac.com/v2");
    }

    @Override
    public Request createRequest(HashMap<String, Object> queryMap, String url) {
        //设置公共参数
        HashMap<String, Object> publicParams = new HashMap<>();
        String date = UTCDateFormatter.format(new Date());
        publicParams.put("AccessKeyId", AccessKey);
        publicParams.put("Version", Version);
        publicParams.put("Date", date);
        publicParams.put("Action", queryMap.get("Action"));
        queryMap.remove("Action");

        //按照参数名称升序排列并编码
        Map<String, Object> treeQueryMap = new TreeMap<>(publicParams);

        StringBuilder params = new StringBuilder();
        for (String key : treeQueryMap.keySet()) {
            if (treeQueryMap.get(key) != null) {
                params.append(key).append("=").append(UrlUtil.urlEncode(treeQueryMap.get(key).toString())).append("&");
            }
        }
        String paramsStrForMd5 = params.deleteCharAt(params.length() - 1).toString();

        //被签名字符串 :  METHOD + "\n" + MD5(属性key-value) + "\n" + ContentType + "\n" + 时间 + "\n"
        String stringToSign = HttpPost.METHOD_NAME + "\n" + Encoder.MD5(paramsStrForMd5) + "\napplication/json;charset=UTF-8\n" + UrlUtil.urlEncode(date) + "\n";

        //构造签名
        String signature = UrlUtil.getSign(AccessKeySecret, stringToSign, "HmacSHA256");
        params.append("&Signature=").append(UrlUtil.urlEncode(signature));

        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse(url + "/?" + params))
                .newBuilder().build();

        Gson gson = new Gson();
        MediaType mediaType = MediaType.Companion.parse("application/json;charset=UTF-8");
        RequestBody requestBody = RequestBody.Companion.create(gson.toJson(queryMap), mediaType);

        return new Request.Builder().url(httpUrl.toString())
                .post(requestBody)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Date", date).build();
    }

    @Override
    public ArrayList<String> createDevice(String Region,
                                          String CloudPhoneImageId,
                                          String PayType,
                                          Integer ProductModelId,
                                          Integer Period,
                                          String GroupId,
                                          String NetworkPacketId,
                                          Integer Num
    ) {

        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("Action", "OpenCloudPhone");
        queryMap.put("Region", Region);
        queryMap.put("CloudPhoneImageId", CloudPhoneImageId);
        queryMap.put("PayType", PayType);
        queryMap.put("ProductModelId", ProductModelId);
        if (Period != null) {
            queryMap.put("Period", Period);
        }
        if (GroupId != null) {
            queryMap.put("GroupId", GroupId);
        }
        if (NetworkPacketId != null) {
            queryMap.put("NetworkPacketId", NetworkPacketId);
        }
        if (Num != null) {
            queryMap.put("Num", Num);
        }

        try (Response response = new OkHttpClient().newCall(createRequest(queryMap)).execute()) {
            if (response.isSuccessful()) {
                Gson gson = new Gson();
                assert response.body() != null;
                var jsonStr = response.body().string();
                ChinacResult<HashMap<String, ArrayList<String>>> result =
                        gson.fromJson(jsonStr,
                                new TypeToken<ChinacResult<HashMap<String, ArrayList<String>>>>() {
                                }.getType());
                if (result.getCode() == 10000) {
                    log.info("【Chinac】 购买设备成功");
                    return result.getData().get("ResourceIds");
                }
            } else {
                log.error(response.toString());
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.warn("【Chinac】 购买设备失败");
        return null;
    }

    @Override
    public ChinacResult<ChinacPageListEntity> queryDeviceList(Integer PageNo, Integer PageSize) {
        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("Action", "ListCloudPhone");
        queryMap.put("PageNo", PageNo);
        queryMap.put("PageSize", PageSize);

        try (Response response = new OkHttpClient().newCall(createRequest(queryMap)).execute()) {
            if (response.isSuccessful()) {
                Gson gson = new Gson();
                assert response.body() != null;
                var jsonStr = response.body().string();
                ChinacResult<ChinacPageListEntity> result =
                        gson.fromJson(jsonStr,
                                new TypeToken<ChinacResult<ChinacPageListEntity>>() {
                                }.getType());
                if (result.getCode() == 10000) {
                    return result;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public ArrayList<ChinacPhoneEntity> queryAllDeviceList() {
        var firstInfo = queryDeviceList(1, 10);
        var deviceList = new ArrayList<>(firstInfo.getData().getList());
        if (firstInfo.getData().getPage().get("TotalPage") != 1) {
            for (int i = 1; i < firstInfo.getData().getPage().get("TotalPage"); i++) {
                deviceList.addAll(queryDeviceList(1 + i, 10).getData().getList());
            }
        }
        return deviceList;
    }

    @Override
    public boolean renewDevice(String Region, String Id, Integer Period) {
        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("Action", "RenewCloudPhone");
        queryMap.put("Region", Region);
        queryMap.put("Id", Id);
        queryMap.put("Period", Period);

        try (Response response = new OkHttpClient().newCall(createRequest(queryMap)).execute()) {
            if (response.isSuccessful()) {
                Gson gson = new Gson();
                assert response.body() != null;
                var jsonStr = response.body().string();
                ChinacResult<HashMap<String, ArrayList<String>>> result =
                        gson.fromJson(jsonStr,
                                new TypeToken<ChinacResult<HashMap<String, ArrayList<String>>>>() {
                                }.getType());
                if (result.getCode() == 10000) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public String getDeviceRemoteControlUrl(String Region, String Id, Integer Duration, boolean AutoDisconnect, boolean AllowGroupControl, ArrayList<String> SalveCloudPhoneIds) {
        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("Action", "GetPhonePageUrl");
        queryMap.put("Region", Region);
        queryMap.put("Id", Id);
        if (Duration != null) {
            queryMap.put("Duration", Duration);
        }
        queryMap.put("AutoDisconnect", AutoDisconnect);
        queryMap.put("AllowGroupControl", AllowGroupControl);
        if (AllowGroupControl) {
            queryMap.put("SalveCloudPhoneIds", SalveCloudPhoneIds);
        }

        try (Response response = new OkHttpClient().newCall(createRequest(queryMap)).execute()) {
            if (response.isSuccessful()) {
                Gson gson = new Gson();
                assert response.body() != null;
                var jsonStr = response.body().string();
                ChinacResult<HashMap<String, String>> result =
                        gson.fromJson(jsonStr,
                                new TypeToken<ChinacResult<HashMap<String, String>>>() {
                                }.getType());
                if (result.getCode() == 10000) {
                    return result.getData().get("Url");
                }
            } else {
                log.error(response.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public HashMap<String, String> getDeviceControlInfo(ArrayList<String> Ids, String Region) {
        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("Action", "GetCmdApiSignature");
        queryMap.put("Ids", Ids);
        queryMap.put("Region", Region);

        try (Response response = new OkHttpClient().newCall(createRequest(queryMap)).execute()) {
            if (response.isSuccessful()) {
                Gson gson = new Gson();
                assert response.body() != null;
                var jsonStr = response.body().string();
                ChinacResult<HashMap<String, String>> result =
                        gson.fromJson(jsonStr,
                                new TypeToken<ChinacResult<HashMap<String, String>>>() {
                                }.getType());
                if (result.getCode() == 10000) {
                    return result.getData();
                }
            } else {
                log.error(response.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public HashMap<String, String> getDeviceScreenshot(ArrayList<String> Ids, String Region) {
        HashMap<String, String> imageMap = new HashMap<>();
        List<DeviceEntity> deviceList = deviceMapper.selectList(Wrappers.<DeviceEntity>lambdaQuery()
                .in(DeviceEntity::getDeviceToken, Ids)
                .eq(DeviceEntity::getDelete, 0)
        );
        deviceList.removeIf(device -> device.getChinac() != 1);
        var ids = new ArrayList<String>();
        deviceList.forEach(it -> ids.add(it.getDeviceToken()));
        HashMap<String, String> controlInfo = getDeviceControlInfo(ids, Region);
        //依次获取图片
        for (String id : ids) {
            var queryMap = new HashMap<>();
            queryMap.put("RToken", controlInfo.get("RToken"));
            queryMap.put("Id", id);

            Gson gson = new Gson();
            Request request = new Request.Builder()
                    .url(Objects.requireNonNull(HttpUrl.parse(controlInfo.get("ApiUrl") +
                            "/cloudPhone/cmd/screenShot?Signature=" +
                            UrlUtil.urlEncode(controlInfo.get("Signature")))).newBuilder().build())
                    .post(RequestBody.Companion.create(gson.toJson(queryMap),
                            MediaType.Companion.parse("application/json;charset=UTF-8")))
                    .build();

            try (Response response = new OkHttpClient().newCall(request).execute()) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    var jsonStr = response.body().string();
                    ChinacScreenshotEntity result =
                            gson.fromJson(jsonStr,
                                    new TypeToken<ChinacScreenshotEntity>() {
                                    }.getType());
                    if (result.getResponseCode() == 100000) {
                        imageMap.put(id, result.getResponseData().get("Img"));
                    }
                } else {
                    log.error(response.toString());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return imageMap;
    }
}
