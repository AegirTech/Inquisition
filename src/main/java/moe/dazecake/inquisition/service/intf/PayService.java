package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.entity.BillEntity;

public interface PayService {

    BillEntity createOrder(Double amount, String payType, String returnPath);

    boolean billCallBackSolver(BillEntity bill);

}
