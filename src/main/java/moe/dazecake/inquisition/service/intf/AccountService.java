package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.account.AddAccountDTO;
import moe.dazecake.inquisition.model.vo.account.AccountWithSanVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;

import java.util.HashMap;

public interface AccountService {
    /**
     * 增加账号（强制）
     *
     * @param addAccountDTO 账号信息
     * @author DazeCake
     * @date 2023/1/25 20:53
     */
    void addAccount(AddAccountDTO addAccountDTO);

    /**
     * 转换来自速通的账号配置文件
     *
     * @param accountJson 速通账号配置Json文件
     * @return 成功迁移的账号数量
     * @author DazeCake
     * @date 2023/1/25 20:54
     */
    int transferAccount(HashMap<String, String> accountJson);

    /**
     * 逻辑删除账号，将导致账号不可登录注册
     *
     * @param id id
     * @author DazeCake
     * @date 2023/1/25 23:33
     */
    void deleteAccount(Long id);

    /**
     * 更新账号信息（强制）
     *
     * @param accountDTO 账号信息
     * @author DazeCake
     * @date 2023/1/25 23:48
     */
    void updateAccount(AccountDTO accountDTO);

    /**
     * 分页查询所有账号
     *
     * @param current 当前页
     * @param size    每页数量
     * @return: moe.dazecake.inquisition.model.vo.query.PageQueryVO<moe.dazecake.inquisition.model.vo.account.AccountWithSanVO>
     * @author DazeCake
     * @date 2023/1/25 23:34
     */
    PageQueryVO<AccountWithSanVO> queryAllAccount(Long current, Long size);

    /**
     * 通过关键词查询账号
     *
     * @param current 当前页
     * @param size    每页数量
     * @param keyword 关键字 优先级：ID --> account --> name
     * @return: moe.dazecake.inquisition.model.vo.query.PageQueryVO<moe.dazecake.inquisition.model.vo.account.AccountWithSanVO>
     * @author DazeCake
     * @date 2023/1/25 23:35
     */
    PageQueryVO<AccountWithSanVO> queryAccount(Long current, Long size, String keyword);

    /**
     * 重设账号刷新次数
     *
     * @param id  账号id
     * @param num 次数
     * @author DazeCake
     * @date 2023/1/25 23:52
     */
    void resetAccountRefresh(Long id, Integer num);

    /**
     * 强制立即作战
     *
     * @param id      id
     * @param isAdmin 是否以管理员身份执行
     * @return: java.lang.String 返回的结果消息
     * @author DazeCake
     * @date 2023/1/25 23:59
     */
    String forceFightAccount(Long id, boolean isAdmin);

    /**
     * 重置账号的动态信息，包括理智等内容
     *
     * @param id id
     * @return: java.lang.String 返回的结果消息
     * @author DazeCake
     * @date 2023/1/26 0:15
     */
    String resetAccountDynamicInfo(Long id);
}

