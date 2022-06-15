package moe.dazecake.inquisition.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import moe.dazecake.inquisition.entity.AdminEntity;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;

public class JWTUtils {

    private static final String SECRET = RandomStringUtils.randomAlphabetic(16);

    private static final long EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    public static String generateToken(AdminEntity adminEntity) {
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("id", adminEntity.getId())
                .withClaim("username", adminEntity.getUserName())
                .withClaim("permission", adminEntity.getPermission())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION));
        return builder.sign(Algorithm.HMAC256(SECRET));
    }

    public static boolean verifyToken(String token) {
        try {
            if (token != null) {
                JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}

