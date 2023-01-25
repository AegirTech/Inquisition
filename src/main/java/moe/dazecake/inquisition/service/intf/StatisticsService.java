package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.entity.BillEntity;

import java.util.List;

public interface StatisticsService {

    Double statisticsOfBillIncome(List<BillEntity> bills);

}
