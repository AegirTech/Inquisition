package moe.dazecake.inquisition.service;

import moe.dazecake.inquisition.entity.BillEntity;

import java.util.List;

public interface StatisticsService {

    Double statisticsOfBillIncome(List<BillEntity> bills);

}
