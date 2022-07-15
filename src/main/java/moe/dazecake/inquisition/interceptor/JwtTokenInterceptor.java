package moe.dazecake.inquisition.interceptor;

import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.util.JWTUtils;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.length() > 7) {
            token = token.substring(7);
        }

        HandlerMethod method = (HandlerMethod) handler;

        //管理员登陆验证
        var login = method.getMethod().getAnnotation(Login.class);
        if (login != null) {
            if (JWTUtils.verifyToken(token)) {
                return true;
            } else {
                response.setStatus(401);
                return false;
            }
        }

        //用户登陆验证
        var userLogin = method.getMethod().getAnnotation(UserLogin.class);
        if (userLogin != null) {
            if (JWTUtils.verifyToken(token)) {
                return true;
            } else {
                response.setStatus(401);
                return false;
            }
        }
        return true;
    }

}
