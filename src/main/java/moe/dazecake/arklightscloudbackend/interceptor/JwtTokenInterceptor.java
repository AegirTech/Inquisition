package moe.dazecake.arklightscloudbackend.interceptor;

import moe.dazecake.arklightscloudbackend.annotation.Login;
import moe.dazecake.arklightscloudbackend.util.JWTUtils;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.length() > 7) {
            token = token.substring(7);
        }
        System.out.println(token);

        HandlerMethod method = (HandlerMethod) handler;

        //获取类上的注解
        var login = method.getMethod().getAnnotation(Login.class);
        if (login != null) {
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
