package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.BillMapper;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.model.dto.fmpay.CreateOrderResultEntity;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.BillEntity;
import moe.dazecake.inquisition.service.intf.PayService;
import moe.dazecake.inquisition.utils.Encoder;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.Result;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PayServiceImpl implements PayService {

    @Value("${inquisition.pay.enablePay:false}")
    boolean enablePay;

    @Value("${inquisition.pay.merchantNum:}")
    String merchantNum;

    @Value("${inquisition.backUrl:}")
    String backUrl;

    @Value("${inquisition.frontUrl:}")
    String frontUrl;

    @Value("${inquisition.pay.payToken:}")
    String payToken;

    @Value("${inquisition.pay.startOrderUrl:}")
    String startOrderUrl;

    @Value("${inquisition.price.daily:1.0}")
    private Double dailyPrice;

    @Resource
    AccountMapper accountMapper;

    @Resource
    ProUserMapper proUserMapper;

    @Resource
    BillMapper billMapper;

    @Resource
    MessageServiceImpl messageService;

    @Resource
    AccountServiceImpl accountService;

    @Override
    public BillEntity createOrder(Double amount, String payType, String returnPath) {
        if (!enablePay || merchantNum.equals("") || backUrl.equals("") || payToken.equals("")) {
            return null;
        }

        String orderNo = String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
        String sign = Encoder.MD5(merchantNum + orderNo + amount.toString() + backUrl + "/payResultCallBack" + payToken);

        OkHttpClient okHttpClient = new OkHttpClient();

        HttpUrl.Builder parameter = Objects.requireNonNull(HttpUrl.parse(startOrderUrl)).newBuilder()
                .addQueryParameter("merchantNum", merchantNum)
                .addQueryParameter("orderNo", orderNo)
                .addQueryParameter("amount", amount.toString())
                .addQueryParameter("notifyUrl", backUrl + "/payResultCallBack")
                .addQueryParameter("payType", payType)
                .addQueryParameter("sign", sign);
        if (returnPath != null) {
            parameter.addQueryParameter("returnUrl", frontUrl + returnPath);
        }

        HttpUrl httpUrl = parameter.build();

        Request request = new Request.Builder().url(httpUrl.toString()).post(okhttp3.internal.Util.EMPTY_REQUEST).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Gson gson = new Gson();
                assert response.body() != null;
                CreateOrderResultEntity result = gson.fromJson(response.body().string(), CreateOrderResultEntity.class);
                BillEntity bill = new BillEntity();
                bill.setOrderNo(orderNo)
                        .setPlatformOrderNo(result.getData().get("id"))
                        .setPayType(payType)
                        .setPayUrl(result.getData().get("payUrl"))
                        .setAmount(amount)
                        .setState(0)
                        .setUpdateTime(LocalDateTime.now());
                billMapper.insert(bill);
                return billMapper.selectOne(Wrappers.<BillEntity>lambdaQuery()
                        .eq(BillEntity::getPlatformOrderNo, result.getData().get("id")));
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean billCallBackSolver(BillEntity bill) {

        if (bill.getType().equals("daily")) {

            var user = accountMapper.selectById(bill.getUserId());

            if (user.getExpireTime().isBefore(LocalDateTime.now())) {
                user.setExpireTime(LocalDateTime.now());
            }
            user.setUpdateTime(LocalDateTime.now());
            user.setExpireTime(user.getExpireTime().plusDays(Integer.parseInt(bill.getParam())));

            //代理佣金
            if (user.getAgent() != null) {
                if (user.getAgent() != 0) {
                    calculateCommission(user.getAgent(), dailyPrice * Integer.parseInt(bill.getParam()));
                }
            } else {
                user.setAgent(0L);
            }

            accountMapper.updateById(user);
            return true;
        } else if (bill.getType().equals("register")) {
            var newUser = new AccountEntity();
            newUser.setName(bill.getParam().split("\\|")[0]);
            newUser.setAccount(bill.getParam().split("\\|")[1]);
            newUser.setPassword(bill.getParam().split("\\|")[2]);
            newUser.setServer(Long.valueOf(bill.getParam().split("\\|")[3]));
            newUser.setAgent(Long.valueOf(bill.getParam().split("\\|")[4]));
            newUser.setExpireTime(LocalDateTime.now().plusDays(3));
            accountMapper.insert(newUser);
            var userId = accountMapper.selectOne(
                    Wrappers.<AccountEntity>lambdaQuery()
                            .eq(AccountEntity::getAccount, newUser.getAccount())).getId();
            accountService.forceFightAccount(userId, true);
            //代理佣金
            if (newUser.getAgent() != 0) {
                calculateCommission(newUser.getAgent(), 1.0);
            }
            return true;
        }
        return false;
    }

    @Override
    public String payResultCallBack(Map<String, String> map) {
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
                log.warn("【支付回调】 前置账单获取出错");
                return "500";
            }

            if (map.get("state").equals("1")) {

                bill.setActualPayAmount(Double.valueOf(map.get("actualPayAmount")))
                        .setState(1)
                        .setUpdateTime(LocalDateTime.now());
                billMapper.updateById(bill);

                if (billCallBackSolver(bill)) {
                    log.info("【支付回调】 支付成功");
                } else {
                    log.info("【支付回调】 支付成功, 解决失败");
                    messageService.pushAdmin("支付成功, 但是解决失败", "支付成功, 但是解决失败");
                }
                return "success";

            }

            log.warn("【支付回调】  状态错误");
            return "state error";
        } else {
            log.warn("【支付回调】  签名错误");
            return "sign error";
        }
    }

    @Override
    public Result<String> getAccountRenewalUrl(String token, String payType, Integer mo) {
        var id = JWTUtils.getId(token);
        if (mo < 1) {
            return Result.failed("不允许购买小于1个月的套餐");
        }

        var bill = createOrder(mo * 30 * dailyPrice, payType, "/user/home/");
        bill.setUserId(id)
                .setType("daily")
                .setParam(String.valueOf(30 * mo));
        billMapper.updateById(bill);
        return Result.success(bill.getPayUrl(), "获取成功");
    }

    @NotNull
    private void calculateCommission(Long id, Double rawAmount) {
        var proUser = proUserMapper.selectById(id);
        proUser.setBalance(proUser.getBalance() + rawAmount * (1 - proUser.getDiscount()));
        proUserMapper.updateById(proUser);

        var newBill = new BillEntity();
        newBill.setOrderNo(String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()))
                .setType("commission")
                .setUserId(proUser.getId())
                .setActualPayAmount(0 - rawAmount * (1 - proUser.getDiscount()))
                .setState(1)
                .setUpdateTime(LocalDateTime.now());
        billMapper.insert(newBill);
    }
}
