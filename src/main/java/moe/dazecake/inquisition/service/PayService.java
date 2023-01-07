package moe.dazecake.inquisition.service;

import moe.dazecake.inquisition.entity.BillEntity;

public interface PayService {

    BillEntity createOrder(Double amount, String payType);

    BillEntity createOrder(Double amount, String payType, String returnPath);

    boolean billCallBackSolver(BillEntity bill);

}
