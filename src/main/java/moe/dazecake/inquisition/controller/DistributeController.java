package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.ProKey;
import moe.dazecake.inquisition.service.impl.HttpServiceImpl;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

@Tag(name = "分发接口")
@ResponseBody
@RestController
public class DistributeController {

    @Resource
    HttpServiceImpl httpService;

    @Resource
    DynamicInfo dynamicInfo;

    @Login
    @Operation(summary = "上传热更新包")
    @PostMapping("/uploadHotUpdatePackage")
    public Result<String> uploadHotUpdatePackage(@RequestParam(value = "file") MultipartFile file, String md5,
                                                 boolean isBate) {
        Result<String> result = new Result<>();

        //校验文件md5
        try {
            if (DigestUtils.md5Hex(file.getBytes()).equals(md5)) {
                result.setCode(200)
                        .setMsg("success")
                        .setData(httpService.uploadFile(file, md5, isBate));
            } else {
                result.setCode(403)
                        .setMsg("md5校验失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @ProKey
    @Operation(summary = "检查是否需要更新")
    @GetMapping("/checkUpdate")
    public Result<Boolean> checkUpdate(String md5, boolean isBate) {
        Result<Boolean> result = new Result<>();

        if (isBate) {
            if (!Objects.equals(md5, dynamicInfo.getArklightsBateMD5())) {
                result.setCode(200)
                        .setMsg("success")
                        .setData(true);
            } else {
                result.setCode(200)
                        .setMsg("success")
                        .setData(false);
            }
        } else {
            if (!Objects.equals(md5, dynamicInfo.getArklightsMD5())) {
                result.setCode(200)
                        .setMsg("success")
                        .setData(true);
            } else {
                result.setCode(200)
                        .setMsg("success")
                        .setData(false);
            }
        }

        return result;
    }

    @ProKey
    @Operation(summary = "获取下载链接")
    @GetMapping("/getDownloadUrl")
    public Result<String> getDownloadUrl(boolean isBate) {
        Result<String> result = new Result<>();

        if (isBate) {
            result.setData(httpService.getDownloadUrl(true, false));
        } else {
            result.setData(httpService.getDownloadUrl(false, false));
        }

        return result.setCode(200)
                .setMsg("success");
    }

    @ProKey
    @Operation(summary = "获取文件MD5")
    @GetMapping("/getFileMD5")
    public Result<String> getFileMD5(boolean isBate) {
        Result<String> result = new Result<>();

        if (isBate) {
            result.setData(dynamicInfo.getArklightsBateMD5());
        } else {
            result.setData(dynamicInfo.getArklightsMD5());
        }

        return result.setCode(200)
                .setMsg("success");
    }
}
