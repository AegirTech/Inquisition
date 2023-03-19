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

    @Override
    public Result<String> uploadImageToCos(String base64Image) {
        if (!ossEnable) {
            return Result.failed("oss未启用");
        }

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
}
