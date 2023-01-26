package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.log.AddLogDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;

public interface LogService {

    /**
     * 添加日志
     *
     * @param addLogDTO 日志体
     * @param isSystem  是否为系统日志
     * @author DazeCake
     * @date 2023/1/26 16:28
     */
    void addLog(AddLogDTO addLogDTO, boolean isSystem);

    void logInfo(String title, String detail);

    void logWarn(String title, String detail);

    /**
     * 高资Tag扫描
     *
     * @param addLogDTO 日志体
     * @author DazeCake
     * @date 2023/1/26 16:29
     */
    void specialScan(AddLogDTO addLogDTO);

    /**
     * 删除日志
     *
     * @param id 日志id
     * @author DazeCake
     * @date 2023/1/26 16:32
     */
    void deleteLog(Long id);

    /**
     * 查询所有日志
     *
     * @param current 当前页
     * @param size    每页大小
     * @return: moe.dazecake.inquisition.model.vo.query.PageQueryVO<moe.dazecake.inquisition.model.dto.log.LogDTO>
     * @author DazeCake
     * @date 2023/1/26 16:45
     */
    PageQueryVO<LogDTO> queryAllLog(Long current, Long size);

    /**
     * 根据账号查询日志
     *
     * @param account 账号
     * @param current 当前页
     * @param size    每页大小
     * @return: moe.dazecake.inquisition.model.vo.query.PageQueryVO<moe.dazecake.inquisition.model.dto.log.LogDTO>
     * @author DazeCake
     * @date 2023/1/26 16:47
     */
    PageQueryVO<LogDTO> queryLogByAccount(String account, Long current, Long size);
}
