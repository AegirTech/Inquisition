package moe.dazecake.arklightscloudbackend.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import moe.dazecake.arklightscloudbackend.constant.ResponseCodeConstants;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * 响应信息主体
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Result<T> implements Serializable {
    @Getter
    @Setter
    private int code = ResponseCodeConstants.SUCCESS;//状态码

    @Getter
    @Setter
    private String msg;//消息

    @Getter
    @Setter
    private T data;//数据

    public static <T> Result<T> success() {
        return restResult(null, ResponseCodeConstants.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return restResult(data, ResponseCodeConstants.SUCCESS, null);
    }

    public static <T> Result<T> success(T data, String msg) {
        return restResult(data, ResponseCodeConstants.SUCCESS, msg);
    }

    public static <T> Result<T> isSuccess(boolean flag) {
        return flag ? success() : failed();
    }

    public static <T> Result<T> failed() {
        return restResult(null, ResponseCodeConstants.FAIL, null);
    }


    public static <T> Result<T> failed(int code, String msg) {
        return restResult(null, code, msg);
    }

    public static <T> Result<T> failed(String msg) {
        return restResult(null, ResponseCodeConstants.FAIL, msg);
    }

    public static <T> Result<T> failed(String msg, T data) {
        return restResult(data, ResponseCodeConstants.FAIL, msg);
    }


    public static <T> Result<T> restResult(T data, int code, String msg) {
        Result<T> apiResult = new Result<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }


    @JsonIgnore
    public Boolean isDataNull() {
        return ObjectUtils.isEmpty(data);
    }
}
