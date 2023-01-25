package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.BillMapper;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.model.dto.FMEntitySet.CreateOrderResultEntity;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.BillEntity;
import moe.dazecake.inquisition.service.intf.PayService;
import moe.dazecake.inquisition.util.Encoder;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

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
                var proUser = proUserMapper.selectById(user.getAgent());

                proUser.setBalance(proUser.getBalance() +
                        dailyPrice * Integer.parseInt(bill.getParam()) * (1 - proUser.getDiscount()));
                proUserMapper.updateById(proUser);

                var newBill = new BillEntity();
                newBill.setOrderNo(String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()))
                        .setType("commission")
                        .setUserId(proUser.getId())
                        .setActualPayAmount(0 - dailyPrice * Integer.parseInt(bill.getParam()) * (1 - proUser.getDiscount()))
                        .setState(1)
                        .setUpdateTime(LocalDateTime.now());
                billMapper.insert(newBill);
            }

            accountMapper.updateById(user);
            return true;
        } else if (bill.getType().equals("register")) {
            var newUser = new AccountEntity();
            newUser.setName(bill.getParam().split("\\|")[0]);
            newUser.setAccount(bill.getParam().split("\\|")[1]);
            newUser.setPassword(bill.getParam().split("\\|")[2]);
            newUser.setServer(Long.valueOf(bill.getParam().split("\\|")[3]));
            newUser.setExpireTime(LocalDateTime.now().plusDays(3));
            accountMapper.insert(newUser);
            return true;
        }
        return false;
    }
}
