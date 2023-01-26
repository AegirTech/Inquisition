package moe.dazecake.inquisition.constant;

public interface ResponseCodeConstants {
    /**
     * 成功
     */
    int SUCCESS = 200;
    /**
     * 重复请求成功
     */
    int REPEAT_SUCCESS = 201;
    /**
     * 参数错误
     */
    int PARAM_ERROR = 400;
    /**
     * 未授权
     */
    int UNAUTHORIZED = 401;
    /**
     * 拒绝访问
     */
    int FORBIDDEN = 403;
    /**
     * 资源未找到
     */
    int NOT_FOUND = 404;
    /**
     * 失败 发生异常
     */
    int FAIL = 500;
}
