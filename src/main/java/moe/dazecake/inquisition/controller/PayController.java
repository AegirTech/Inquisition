package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.model.dto.pay.AccountRenewalDTO;
import moe.dazecake.inquisition.service.impl.PayServiceImpl;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Tag(name = "支付接口")
@ResponseBody
@RestController
public class PayController {

    @Resource
    PayServiceImpl payService;

    @Operation(summary = "支付结果回调")
    @GetMapping("/payResultCallBack")
    public String payResultCallBack(@RequestParam Map<String, String> map) {
        return payService.payResultCallBack(map);
    }

    @UserLogin
    @Operation(summary = "获取账号续费url")
    @PostMapping("/getAccountRenewalUrl")
    public Result<String> getAccountRenewalUrl(@RequestHeader("Authorization") String token,
                                               @RequestBody AccountRenewalDTO accountRenewalDTO) {
        return payService.getAccountRenewalUrl(token, accountRenewalDTO.getPayType(), accountRenewalDTO.getMo());
    }
}
