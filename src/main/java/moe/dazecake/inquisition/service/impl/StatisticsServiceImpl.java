package moe.dazecake.inquisition.service.impl;

import moe.dazecake.inquisition.entity.BillEntity;
import moe.dazecake.inquisition.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public Double statisticsOfBillIncome(List<BillEntity> bills) {
        Double income = 0.0;

        for (BillEntity bill : bills) {
            income += bill.getActualPayAmount();
        }

        return income;
    }
}
