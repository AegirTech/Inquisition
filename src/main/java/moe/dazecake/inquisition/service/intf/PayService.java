package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.entity.BillEntity;
import moe.dazecake.inquisition.utils.Result;

import java.util.Map;

public interface PayService {

    BillEntity createOrder(Double amount, String payType, String returnPath);

    String createBill(Long userId, String params, String type, Double amount, String payType, String returnPath);

    boolean billCallBackSolver(BillEntity bill);

    String payResultCallBack(Map<String, String> map);

    Result<String> getAccountRenewalUrl(String token, String payType, Integer mo);

}
