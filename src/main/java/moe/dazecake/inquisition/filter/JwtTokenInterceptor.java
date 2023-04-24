package moe.dazecake.inquisition.filter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.ProKey;
import moe.dazecake.inquisition.annotation.ProUserLogin;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.model.entity.ProUserEntity;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.RequestWrapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Resource
    private ProUserMapper proUserMapper;

    @Value("${inquisition.dev_mode:false}")
    private boolean devMode;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler) {

        RequestWrapper requestWrapper = new RequestWrapper(request);
        if (requestWrapper.getContentType() != null && requestWrapper.getContentType().contains("application/json")) {
            // 包装原始请求
            String requestBody = requestWrapper.getBody();
            try {
                //尝试解析JSON
                new Gson().fromJson(requestBody, Map.class);
            } catch (Exception ex) {
                // 解析失败，获取原始JSON数据
                log.warn("无法解析JSON： \n"+ requestBody);
            }
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        //开发模式跳过jwt检查
        if (devMode) {
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
