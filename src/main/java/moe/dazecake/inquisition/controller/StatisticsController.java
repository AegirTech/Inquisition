package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.entity.AccountEntity;
import moe.dazecake.inquisition.entity.BillEntity;
import moe.dazecake.inquisition.mapper.AccountMapper;
import moe.dazecake.inquisition.mapper.BillMapper;
import moe.dazecake.inquisition.mapper.ProUserMapper;
import moe.dazecake.inquisition.service.impl.StatisticsServiceImpl;
import moe.dazecake.inquisition.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;

@Tag(name = "统计接口")
@ResponseBody
@RestController
public class StatisticsController {

    @Resource
    AccountMapper accountMapper;

    @Resource
    ProUserMapper proUserMapper;

    @Resource
    BillMapper billMapper;

    @Resource
    StatisticsServiceImpl statisticsService;

    @Login
    @Operation(summary = "获取概览统计数据")
    @GetMapping("/getStatistics")
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
        result.getData().put("monthIncome", statisticsService.statisticsOfBillIncome(monthBills));
        result.getData().put("dayIncome", statisticsService.statisticsOfBillIncome(dayBills));


        return result.setCode(200)
                .setMsg("success");
    }

}
