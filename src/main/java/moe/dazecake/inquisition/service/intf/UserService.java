package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.dto.user.CreateUserByPayDTO;
import moe.dazecake.inquisition.model.dto.user.UserStatusSTO;
import moe.dazecake.inquisition.model.vo.UserLoginVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.utils.Result;

public interface UserService {

    /**
     * 通过CDK创建用户
     *
     * @param cdk      CDK
     * @param username 用户名
     * @param account  账号
     * @param password 密码
     * @param server   服务器
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 20:42
     */
    Result<String> createUserByCDK(String cdk, String username, String account, String password, Integer server);

    /**
     * 通过支付创建用户
     *
     * @param createUserByPayDTO 支付参数
     * @param username           用户名
     * @param account            账号
     * @param password           密码
     * @param server             服务器
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 20:52
     */
    Result<String> createUserByPay(CreateUserByPayDTO createUserByPayDTO, String username, String account, String password, Integer server);

    /**
     * 用户登录
     *
     * @param account  账号
     * @param password 密码
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.UserLoginVO>
     * @author DazeCake
     * @date 2023/1/27 21:03
     */
    Result<UserLoginVO> userLogin(String account, String password);

    /**
     * 显示我的账户信息
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.dto.account.AccountDTO>
     * @author DazeCake
     * @date 2023/1/27 21:10
     */
    Result<AccountDTO> showMyAccount(Long id);

    /**
     * 更新我的账户信息
     *
     * @param accountDTO 账户信息
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 21:15
     */
    Result<String> updateMyAccount(Long id, AccountDTO accountDTO);

    /**
     * 更新账号密码
     *
     * @param account  账号
     * @param password 密码
     * @param server   服务器
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 21:24
     */
    Result<String> updateAccountAndPassword(Long id, String account, String password, Long server);

    /**
     * 冻结我的账户
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 21:35
     */
    Result<String> freezeMyAccount(Long id);

    /**
     * 解冻我的账户
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 21:36
     */
    Result<String> unfreezeMyAccount(Long id);

    /**
     * 显示我的日志
     *
     * @param account 用户ID
     * @param current 当前页
     * @param size    每页大小
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.query.PageQueryVO < moe.dazecake.inquisition.model.dto.log.LogDTO>>
     * @author DazeCake
     * @date 2023/1/27 21:39
     */
    Result<PageQueryVO<LogDTO>> showMyLog(String account, Long current, Long size);

    /**
     * 显示我的状态
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.dto.user.UserStatusSTO>
     * @author DazeCake
     * @date 2023/1/27 22:16
     */
    Result<UserStatusSTO> showMyStatus(Long id);

    /**
     * 显示我的理智
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 22:42
     */
    Result<String> showMySan(Long id);

    /**
     * 使用CDK
     *
     * @param id  用户ID
     * @param cdk CDK
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 22:47
     */
    Result<String> useCDK(Long id, String cdk);

    /**
     * 获取微信推送二维码
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 22:50
     */
    Result<String> getWechatQRCode(Long id);

    /**
     * 强制停止作战
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 22:53
     */
    Result<String> forceHalt(Long id);

    /**
     * 立即开始作战
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 23:05
     */
    Result<String> startNow(Long id);

    /**
     * 获取刷新次数
     *
     * @param id 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.Integer>
     * @author DazeCake
     * @date 2023/1/27 23:08
     */
    Result<Integer> getRefresh(Long id);

}
