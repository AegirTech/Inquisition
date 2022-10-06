package moe.dazecake.inquisition.service.impl;

import com.google.gson.Gson;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.entity.HttpEntitySet.BiliLoginRes;
import moe.dazecake.inquisition.entity.HttpEntitySet.BiliPerLoginRes;
import moe.dazecake.inquisition.entity.HttpEntitySet.OfficialLoginRes;
import moe.dazecake.inquisition.service.HttpService;
import moe.dazecake.inquisition.util.DynamicInfo;
import moe.dazecake.inquisition.util.Encoder;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class HttpServiceImpl implements HttpService {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    Gson gson = new Gson();

    HashMap<String, String> params = new HashMap<>() {
        {
            put("apk_sign", "4502a02a00395dec05a4134ad593224d");
            put("app_id", "952");
            put("app_ver", "1.8.01");
            put("game_id", "952");
            put("merchant_id", "328");
            put("server_id", "1178");
            put("timestamp", "");
            put("version", "3");
        }
    };

    HashMap<String, String> headers = new HashMap<>() {
        {
            put("User-Agent", "Mozilla/5.0 BSGameSDK");
            put("Host", "line1-sdk-center-login-sh.biligame.net");
            put("Content-Type", "application/x-www-form-urlencoded");
        }
    };

    @SneakyThrows
    private String getPass(String password) {
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("cipher_type", "bili_login_rsa");
        Request request = new Request.Builder()
                .url("https://line1-sdk-center-login-sh.biligame.net/api/external/issue/cipher/v3")
                .headers(Headers.of(headers))
                .post(execParam())
                .build();
        try (Response response = client.newCall(request).execute()) {
            var res = gson.fromJson(
                    Objects.requireNonNull(response.body()).string(), BiliPerLoginRes.class);
            var hash = res.getHash();
            params.remove("cipher_type");
            if (hash != null) {
                var rsa = res.getCipher_key();
                return RsaEncrypt(hash + password, rsa);
            } else {
                return "";
            }
        }
    }

    @SneakyThrows
    private RequestBody execParam() {
        FormBody.Builder builder = new FormBody.Builder();
        ArrayList<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sign = new StringBuilder();
        for (String key : keys) {
            builder.add(key, params.get(key));
            sign.append(params.get(key));
        }
        builder.add("sign", HashString(sign.toString()));

        return builder.build();
    }

    private String HashString(String str) {
        return Encoder.MD5(str + "8783abfb533544c59e598cddc933d1bf");
    }

    @SneakyThrows
    private String RsaEncrypt(String plain, String publicKey) {
        Pattern parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
        byte[] decoded = Base64.getDecoder().decode(parse.matcher(publicKey).replaceFirst("$1").replace("\n", ""));
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }

    @Value("${storage.oss.enable}")
    private boolean ossEnable;

    @Value("${storage.oss.secretId}")
    private String secretId;

    @Value("${storage.oss.secretKey}")
    private String secretKey;

    @Value("${storage.oss.bucket}")
    private String bucketName;

    @Value("${storage.oss.region}")
    private String regionName;

    @Resource
    DynamicInfo dynamicInfo;

    @SneakyThrows
    @Override
    public boolean isOfficialAccountWork(String phone, String password) {
        Request request = new Request.Builder()
                .url("https://as.hypergryph.com/user/auth/v1/token_by_phone_password")
                .post(RequestBody.create(
                        "{\"password\":\"" + password + "\",\"phone\":\"" + phone + "\"}", JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            var res = gson.fromJson(
                    Objects.requireNonNull(response.body()).string(), OfficialLoginRes.class);
            return res.getStatus() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    @Override
    public boolean isBiliAccountWork(String account, String password) {
        params.put("pwd", getPass(password));
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("user_id", account);
        Request request = new Request.Builder()
                .url("https://line1-sdk-center-login-sh.biligame.net/api/external/login/v3")
                .headers(Headers.of(headers))
                .post(execParam())
                .build();
        try (Response response = client.newCall(request).execute()) {
            var res = gson.fromJson(
                    Objects.requireNonNull(response.body()).string(), BiliLoginRes.class);
            return res.getCode() == 0;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public String uploadFile(MultipartFile file, String md5, boolean isBate) {

        COSClient cosClient = new COSClient(
                new BasicCOSCredentials(secretId, secretKey),
                new ClientConfig(new Region(regionName))
        );

        String scriptName;
        String md5FileName;
        try {
            //脚本
            File scriptFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".lr");
            file.transferTo(scriptFile);

            //生成一个file并写入md5
            File md5File = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".md5");
            FileWriter fileWriter = new FileWriter(md5File);
            fileWriter.write(md5);
            fileWriter.close();

            if (isBate) {
                scriptName = "script_bate.lr";
                md5FileName = "script_bate.md5";
            } else {
                scriptName = "script.lr";
                md5FileName = "script.md5";
            }
            // 上传至 COS
            PutObjectRequest objectRequest = new PutObjectRequest(bucketName, scriptName, scriptFile);
            cosClient.putObject(objectRequest);

            PutObjectRequest md5Request = new PutObjectRequest(bucketName, md5FileName, md5File);
            cosClient.putObject(md5Request);

            if (isBate) {
                dynamicInfo.setArklightsBateMD5(md5);
            } else {
                dynamicInfo.setArklightsMD5(md5);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cosClient.shutdown();
        }
        return "上传成功";
    }

    @Override
    public String getDownloadUrl(boolean isBate, boolean isMD5) {

        COSClient cosClient = new COSClient(
                new BasicCOSCredentials(secretId, secretKey),
                new ClientConfig(new Region(regionName))
        );

        String key;

        if (isBate) {
            key = "script_bate.lr";
        } else {
            key = "script.lr";
        }

        if (isMD5) {
            key = key.replace(".lr", ".md5");
        }

        Date expirationDate = new Date(System.currentTimeMillis() + 15 * 60 * 1000);

        HttpMethodName method = HttpMethodName.GET;

        URL url = cosClient.generatePresignedUrl(bucketName, key, expirationDate, method);

        cosClient.shutdown();

        return url.toString();
    }

    @SneakyThrows
    @Override
    public void updateLatestMD5() {

        if (!ossEnable) {
            return;
        }

        var url = getDownloadUrl(false, true);
        var urlBate = getDownloadUrl(true, true);

        //从url和urlBate下载md5文件并读取文件内容到dynamicInfo.arlkightsMD5和dynamicInfo.arlkightsBateMD5
        try (var in = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            var s = in.readLine();
            dynamicInfo.setArklightsMD5(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (var in = new BufferedReader(new InputStreamReader(new URL(urlBate).openStream()))) {
            var s = in.readLine();
            dynamicInfo.setArklightsBateMD5(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("更新md5成功");
        log.info("正式版md5: " + dynamicInfo.getArklightsMD5());
        log.info("测试版md5: " + dynamicInfo.getArklightsBateMD5());
    }

}