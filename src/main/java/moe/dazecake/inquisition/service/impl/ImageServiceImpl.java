package moe.dazecake.inquisition.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;

import moe.dazecake.inquisition.service.intf.ImageService;
import moe.dazecake.inquisition.utils.Result;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.*;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${storage.oss.enable:false}")
    private boolean ossEnable;

    @Value("${storage.oss.secretId:}")
    private String secretId;

    @Value("${storage.oss.secretKey:}")
    private String secretKey;

    @Value("${storage.oss.bucket:}")
    private String bucketName;

    @Value("${storage.oss.region:}")
    private String regionName;

    @Value("${storage.chfs.enable:false}")
    private boolean chfsEnable;

    @Value("${storage.chfs.url:}")
    private String chfsUrl;

    @Value("${storage.chfs.username:}")
    private String chfsUsername;

    @Value("${storage.chfs.password:}")
    private String chfsPassword;

    @Value("${storage.chfs.uploadDir:}")
    private String chfsUploadDir;


    @Override
    public Result<String> uploadImage(String base64Image) {
        if (ossEnable) {
            return uploadImageToCos(base64Image);
        } else if (chfsEnable) {
            return uploadImageToCHFS(base64Image);
        } else {
            return Result.failed("未配置任何存储服务");
        }
    }
    
    private Result<String> uploadImageToCos(String base64Image) {
        COSClient cosClient = new COSClient(
            new BasicCOSCredentials(secretId, secretKey),
            new ClientConfig(new Region(regionName))
        );
        try {
            var fileName = String.valueOf(System.currentTimeMillis());
            var file = File.createTempFile(fileName, ".png");
            var fos = new FileOutputStream(file);
            //base64解码并写入文件
            fos.write(Base64.decodeBase64(base64Image));
            fos.flush();
            fos.close();

            //上传至 COS
            PutObjectRequest objectRequest = new PutObjectRequest(bucketName, fileName + ".png", file);
            cosClient.putObject(objectRequest);

            //获取下载地址
            Date expirationDate = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

            HttpMethodName method = HttpMethodName.GET;

            URL url = cosClient.generatePresignedUrl(bucketName, fileName + ".png", expirationDate, method);

            return Result.success(url.toString(), "上传成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            cosClient.shutdown();
        }
    }
    private Result<String> uploadImageToCHFS(String base64Image) {
        OkHttpClient client = new OkHttpClient();
        // 登录 CHFS
        var loginUrl = chfsUrl + "/chfs/session";
        var loginRequestBody = new FormBody.Builder()
                .add("user", chfsUsername)
                .add("pwd", chfsPassword)
                .build();
        var loginRequest = new Request.Builder()
                .url(loginUrl)
                .post(loginRequestBody)
                .build();
        try (Response loginResponse = client.newCall(loginRequest).execute()) {
            int statusCode = loginResponse.code();
            if (statusCode != 201) {
                return Result.failed("登录失败，返回码为 " + statusCode);
            }
            var cookie = loginResponse.headers("Set-Cookie");
            if (cookie == null || cookie.size() < 2) {
                return Result.failed("登录失败，无法获取 Cookie");
            }
            //正则匹配COOKIE中JWT字段后的cookie
            var jwtPattern = Pattern.compile("JWT=([^;]+)");
            String jwt = null;
            for (String c : cookie) {
                var jwtMatcher = jwtPattern.matcher(c);
                if (jwtMatcher.find()) {
                    jwt = jwtMatcher.group(1);
                }
            }

            // 创建图片临时文件
            var fileName = String.valueOf(System.currentTimeMillis());
            var file = File.createTempFile(fileName, ".png");
            var fos = new FileOutputStream(file);
            //base64解码并写入文件
            fos.write(Base64.decodeBase64(base64Image));
            fos.flush();
            fos.close();
            System.out.println("4");
            // 上传至 CHFS
            var uploadUrl = chfsUrl + "/chfs/upload";
            var uploadRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName + ".png", RequestBody.create(file, MediaType.parse("image/png")))
                    .addFormDataPart("folder", chfsUploadDir)
                    .build();
            var uploadRequest = new Request.Builder()
                .url(uploadUrl)
                .post((RequestBody) uploadRequestBody)
                .addHeader("Cookie", "JWT=" + jwt + "; user=" + chfsUsername)
                .build();
            try (Response uploadResponse = client.newCall(uploadRequest).execute()) {
                int uploadStatusCode = uploadResponse.code();
                if (uploadStatusCode != 201) {
                    return Result.failed("上传失败，返回码不为 201");
                }
                var downloadUrl = chfsUrl + "/shared" + chfsUploadDir + "/" + fileName + ".png";
                return Result.success(downloadUrl.toString(), "上传成功");
            } 
        }catch (IOException e) {
        throw new RuntimeException(e);
        }
    }
}



