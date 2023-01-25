package moe.dazecake.inquisition.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class Encoder {
    public static String MD5(String str) {
        //返回str的MD5值
        return Hex.encodeHexString(DigestUtils.md5(str), true);
    }
}
