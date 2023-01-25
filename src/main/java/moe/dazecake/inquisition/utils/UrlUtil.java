package moe.dazecake.inquisition.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class UrlUtil {
    public static String urlEncode(String raw) {
        String encodedStr = URLEncoder.encode(raw, StandardCharsets.UTF_8);
        encodedStr = encodedStr.replace("+", "%20");
        encodedStr = encodedStr.replace("*", "%2A");
        return encodedStr.replace("%7E", "~");
    }

    public static String getSign(String secretKey, String stringToSign, String signatureMethod) {
        if (secretKey == null || stringToSign == null) {
            return "";
        }
        signatureMethod = signatureMethod == null ? "HmacSHA256" : signatureMethod;
        Mac mac;
        try {
            mac = Mac.getInstance(signatureMethod);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), signatureMethod));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
    }
}
