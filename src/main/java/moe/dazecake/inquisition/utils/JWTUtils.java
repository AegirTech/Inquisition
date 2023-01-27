package moe.dazecake.inquisition.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.AdminEntity;
import moe.dazecake.inquisition.model.entity.ProUserEntity;

import java.util.Date;

public class JWTUtils {

    //    private static String SECRET = RandomStringUtils.randomAlphabetic(16);
    public static String SECRET;


    private static final long EXPIRATION = 1000L * 60 * 60 * 24 * 30;

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
                .withClaim("account", accountEntity.getAccount())
                .withClaim("type", "user")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION));
        return builder.sign(Algorithm.HMAC256(SECRET));
    }

    public static String generateTokenForProUser(ProUserEntity proUserEntity) {
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("id", proUserEntity.getId())
                .withClaim("username", proUserEntity.getUsername())
                .withClaim("type", "proUser")
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

    public static String getAccount(String token) {
        assert token != null;
        return JWT.decode(token.substring(7)).getClaim("account").asString();
    }

    public static String getType(String token) {
        assert token != null;
        return JWT.decode(token).getClaim("type").asString();
    }
}

