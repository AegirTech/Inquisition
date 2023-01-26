package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.entity.BillEntity;
import moe.dazecake.inquisition.utils.Result;

import java.util.HashMap;
import java.util.List;

public interface StatisticsService {

    /**
     * 获取概览统计数据
     *
     * @return: Result<HashMap < String, Object>>
     * @author DazeCake
     * @date 2023/1/26 23:55
     */
    Result<HashMap<String, Object>> getStatistics();

    /**
     * 统计区间账单收益
     *
     * @param bills 账单列表
     * @return: java.lang.Double
     * @author DazeCake
     * @date 2023/1/26 23:54
     */
    Double statisticsOfBillIncome(List<BillEntity> bills);

}
