package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.BillMapper;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.BillEntity;
import moe.dazecake.inquisition.service.intf.StatisticsService;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    AccountMapper accountMapper;

    @Resource
    BillMapper billMapper;

    @Override
    public Result<HashMap<String, Object>> getStatistics() {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setData(new HashMap<>());

        var payedUserList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                .ge(AccountEntity::getExpireTime, LocalDateTime.now())
                .eq(AccountEntity::getDelete, 0));

        var newUserList = accountMapper.selectList(Wrappers.<AccountEntity>lambdaQuery()
                .ge(AccountEntity::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                .lt(AccountEntity::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MAX))
                .ge(AccountEntity::getExpireTime, LocalDateTime.now())
                .eq(AccountEntity::getDelete, 0));

        var monthBills = billMapper.selectList(Wrappers.<BillEntity>lambdaQuery()
                .ge(BillEntity::getUpdateTime, LocalDateTime.now()
                        .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0))
                .lt(BillEntity::getUpdateTime, LocalDateTime.now()
                        .withDayOfMonth(1).plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59))
                .eq(BillEntity::getState, 1));

        var dayBills = billMapper.selectList(Wrappers.<BillEntity>lambdaQuery()
                .ge(BillEntity::getUpdateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                .lt(BillEntity::getUpdateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MAX))
                .eq(BillEntity::getState, 1));

        result.getData().put("payedUserNum", payedUserList.size());
        result.getData().put("newUserNum", newUserList.size());
        result.getData().put("monthIncome", statisticsOfBillIncome(monthBills));
        result.getData().put("dayIncome", statisticsOfBillIncome(dayBills));


        return result.setCode(200)
                .setMsg("success");
    }

    @Override
    public Double statisticsOfBillIncome(List<BillEntity> bills) {
        Double income = 0.0;

        for (BillEntity bill : bills) {
            income += bill.getActualPayAmount();
        }

        return income;
    }
}
