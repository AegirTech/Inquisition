package moe.dazecake.inquisition.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

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
    // cos
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
    // s3
    @Value("${storage.s3.enable:false}")
    private boolean s3Enable;

    @Value("${storage.s3.endpoint}")
    private String s3Endpoint;

    @Value("${storage.s3.accessKey}")
    private String s3AccessKey;

    @Value("${storage.s3.secretKey}")
    private String s3SecretKey;

    @Value("${storage.s3.bucketName}")
    private String s3BucketName;
    // chfs
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
        } else if (s3Enable) {
            return uploadImageToS3(base64Image);
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
            com.qcloud.cos.model.PutObjectRequest cosRequest = 
                new com.qcloud.cos.model.PutObjectRequest(bucketName, fileName + ".png", file);
            cosClient.putObject(cosRequest);

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
    private Result<String> uploadImageToS3(String base64Image) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Endpoint, null))
                                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                                .withPathStyleAccessEnabled(true)
                                .build();

        try {
            var fileName = String.valueOf(System.currentTimeMillis()) + ".png";
            var file = File.createTempFile(fileName, ".png");
            var fos = new FileOutputStream(file);
            fos.write(Base64.decodeBase64(base64Image));
            fos.flush();
            fos.close();

            // 上传至S3兼容存储
            com.amazonaws.services.s3.model.PutObjectRequest s3PutRequest = 
                new com.amazonaws.services.s3.model.PutObjectRequest(s3BucketName, fileName, file);
            s3Client.putObject(s3PutRequest);

            // 构建下载URL
            String url = s3Client.getUrl(s3BucketName, fileName).toString();
            return Result.success(url, "上传成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            if (cookie.size() < 2) {
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
                    .post(uploadRequestBody)
                    .addHeader("Cookie", "JWT=" + jwt + "; user=" + chfsUsername)
                    .build();
            try (Response uploadResponse = client.newCall(uploadRequest).execute()) {
                int uploadStatusCode = uploadResponse.code();
                if (uploadStatusCode != 201) {
                    return Result.failed("上传失败，返回码不为 201");
                }
                var downloadUrl = chfsUrl + "/chfs/shared" + chfsUploadDir + "/" + fileName + ".png";
                return Result.success(downloadUrl, "上传成功");
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



