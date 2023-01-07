package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.entity.BillEntity;
import moe.dazecake.inquisition.mapper.BillMapper;
import moe.dazecake.inquisition.service.impl.PayServiceImpl;
import moe.dazecake.inquisition.util.Encoder;
import moe.dazecake.inquisition.util.JWTUtils;
import moe.dazecake.inquisition.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Tag(name = "支付接口")
@ResponseBody
@RestController
public class PayController {

    @Value("${inquisition.pay.payToken:}")
    String payToken;

    @Resource
    BillMapper billMapper;

    @Resource
    PayServiceImpl payService;

    @Value("${inquisition.price.daily:1.0}")
    private Double dailyPrice;

    @Operation(summary = "支付结果回调")
    @GetMapping("/payResultCallBack")
    public String payResultCallBack(@RequestParam Map<String, String> map) {

        if (map.get("sign").equals(Encoder.MD5(
                map.get("state") +
                        map.get("merchantNum") +
                        map.get("orderNo") +
                        map.get("amount") +
                        payToken
        ))) {
            var bill = billMapper.selectOne(Wrappers.<BillEntity>lambdaQuery()
                    .eq(BillEntity::getPlatformOrderNo, map.get("platformOrderNo")));

            if (bill == null) {
                log.warn("[支付回调]: 前置账单获取出错");
                return "500";
            }

            if (map.get("state").equals("1")) {

                bill.setActualPayAmount(Double.valueOf(map.get("actualPayAmount")))
                        .setState(1)
                        .setUpdateTime(LocalDateTime.now());
                billMapper.updateById(bill);

                if (payService.billCallBackSolver(bill)) {
                    log.info("[支付回调]: 支付成功");
                    return "success";
                } else {
                    log.info("[支付回调]: 支付成功, 解决失败");
                    // TODO: 12/30/22 消息推送至管理员
                    return "success";
                }


            }

            log.warn("[支付回调]: 状态错误");
            return "state error";
        } else {
            log.warn("[支付回调]: 签名错误");
            return "sign error";
        }
    }

    @UserLogin
    @Operation(summary = "获取账号续费url")
    @PostMapping("/getAccountRenewalUrl")
    public Result<String> getAccountRenewalUrl(@RequestHeader("Authorization") String token, String payType, Integer mo) {
        Result<String> result = new Result<>();
        var id = JWTUtils.getId(token);
        if (mo < 1) {
            return result.setData("你小子折腾啥呢");
        }

        var bill = payService.createOrder(mo * 30 * dailyPrice, payType);
        bill.setUserId(id)
                .setType("daily")
                .setParam(String.valueOf(30 * mo));
        billMapper.updateById(bill);

        return result.setData(bill.getPayUrl());
    }
}
