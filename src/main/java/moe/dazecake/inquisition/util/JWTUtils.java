package moe.dazecake.inquisition.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.AdminEntity;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;

public class JWTUtils {

    private static final String SECRET = RandomStringUtils.randomAlphabetic(16);

    private static final long EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    public static String generateTokenForAdmin(AdminEntity adminEntity) {
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("id", adminEntity.getId())
                .withClaim("username", adminEntity.getUsername())
                .withClaim("permission", adminEntity.getPermission())
                .withClaim("type", "admin")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION));
        return builder.sign(Algorithm.HMAC256(SECRET));
    }

    public static String generateTokenForUser(AccountEntity accountEntity) {
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("id", accountEntity.getId())
                .withClaim("username", accountEntity.getAccount())
                .withClaim("type", "user")
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

    public static Long getId(String token) {
        assert token != null;
        return JWT.decode(token.substring(7)).getClaim("id").asLong();
    }

    public static String getUsername(String token) {
        assert token != null;
        return JWT.decode(token.substring(7)).getClaim("username").asString();
    }

    public static String getType(String token) {
        assert token != null;
        return JWT.decode(token).getClaim("type").asString();
    }
}

