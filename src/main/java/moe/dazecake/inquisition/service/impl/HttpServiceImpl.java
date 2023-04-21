package moe.dazecake.inquisition.service.impl;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.model.dto.http.BiliLoginRes;
import moe.dazecake.inquisition.model.dto.http.BiliPerLoginRes;
import moe.dazecake.inquisition.model.dto.http.OfficialLoginRes;
import moe.dazecake.inquisition.service.intf.HttpService;
import moe.dazecake.inquisition.utils.Encoder;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
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

}