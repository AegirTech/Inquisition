package moe.dazecake.inquisition.filter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.ProKey;
import moe.dazecake.inquisition.annotation.ProUserLogin;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.model.entity.ProUserEntity;
import moe.dazecake.inquisition.util.JWTUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Resource
    private ProUserMapper proUserMapper;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.length() > 7) {
            token = token.substring(7);
        }

        HandlerMethod method = (HandlerMethod) handler;

        //ProKey验证
        var proKey = method.getMethod().getAnnotation(ProKey.class);
        if (proKey != null) {
            var proUser = proUserMapper.selectOne(
                    Wrappers.<ProUserEntity>lambdaQuery()
                            .eq(ProUserEntity::getAuthorization, token)
                            .eq(ProUserEntity::getPermission, "pro")
            );
            if (proUser == null) {
                response.setStatus(403);
                return false;
            }
        }

        //管理员登陆验证
        var login = method.getMethod().getAnnotation(Login.class);
        if (login != null) {
            if (JWTUtils.verifyToken(token) && Objects.equals(JWTUtils.getType(Objects.requireNonNull(token)),
                    "admin")) {
                return true;
            } else {
                response.setStatus(401);
                return false;
            }
        }

        //高级用户登陆验证
        var proUserLogin = method.getMethod().getAnnotation(ProUserLogin.class);
        if (proUserLogin != null) {
            if (JWTUtils.verifyToken(token) && Objects.equals(JWTUtils.getType(Objects.requireNonNull(token)),
                    "proUser")) {
                return true;
            } else {
                response.setStatus(401);
                return false;
            }
        }

        //用户登陆验证
        var userLogin = method.getMethod().getAnnotation(UserLogin.class);
        if (userLogin != null) {
            if (JWTUtils.verifyToken(token) && Objects.equals(JWTUtils.getType(Objects.requireNonNull(token)),
                    "user")) {
                return true;
            } else {
                response.setStatus(401);
                return false;
            }
        }
        return true;
    }

}
