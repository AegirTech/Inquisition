package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.cdk.CreateCDKDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.dto.prouser.CreateProUserDTO;
import moe.dazecake.inquisition.model.dto.prouser.ProUserDTO;
import moe.dazecake.inquisition.model.dto.prouser.ProUserLoginDTO;
import moe.dazecake.inquisition.model.dto.prouser.UpdateProUserPasswordDTO;
import moe.dazecake.inquisition.model.vo.account.AccountWithSanVO;
import moe.dazecake.inquisition.model.vo.cdk.CDKListVO;
import moe.dazecake.inquisition.model.vo.prouser.ProUserLoginVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.utils.Result;

import java.util.ArrayList;

public interface ProUserService {

    /**
     * 创建代理账号
     *
     * @param createProUserDTO 代理账号结构
     * @author DazeCake
     * @date 2023/1/26 19:03
     */
    void CreateProUser(CreateProUserDTO createProUserDTO);

    /**
     * 分页查询高级用户账号
     *
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.query.PageQueryVO < moe.dazecake.inquisition.model.dto.prouser.ProUserDTO>>
     * @author DazeCake
     * @date 2023/1/31 20:29
     */
    Result<PageQueryVO<ProUserDTO>> getAllProUser(Long current, Long size);

    /**
     * 更新高级用户信息
     *
     * @param proUserDTO 高级用户信息
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/2/15 22:02
     */
    Result<String> updateProUser(ProUserDTO proUserDTO);

    /**
     * 代理账号登录
     *
     * @param proUserLoginDTO 代理账号密码
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.prouser.ProUserLoginVO>
     * @author DazeCake
     * @date 2023/1/26 20:03
     */
    Result<ProUserLoginVO> loginProUser(ProUserLoginDTO proUserLoginDTO);

    /**
     * 获取代理账号信息
     *
     * @param id 代理账号ID
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.dto.prouser.ProUserDTO>
     * @author DazeCake
     * @date 2023/1/26 23:36
     */
    Result<ProUserDTO> getProUserInfo(Long id);

    /**
     * 更新代理账号密码
     *
     * @param id                       代理账号ID
     * @param updateProUserPasswordDTO 新旧密码
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 20:21
     */
    Result<String> updateProUserPassword(Long id, UpdateProUserPasswordDTO updateProUserPasswordDTO);

    /**
     * 查询所有子账号
     *
     * @param id      代理账号ID
     * @param type    子账号类型
     * @param current 当前页
     * @param size    每页大小
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.query.PageQueryVO < moe.dazecake.inquisition.model.vo.account.AccountWithSanVO>>
     * @author DazeCake
     * @date 2023/3/25 18:30
     */
    Result<PageQueryVO<AccountWithSanVO>> queryAllSubUser(Long id, String type, Integer current, Integer size);

    /**
     * 通过账号查询子账号
     *
     * @param id      代理账号ID
     * @param current 当前页
     * @param size    每页大小
     * @param keyword 关键字
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.query.PageQueryVO < moe.dazecake.inquisition.model.dto.account.AccountDTO>>
     * @author DazeCake
     * @date 2023/1/26 20:29
     */
    Result<PageQueryVO<AccountWithSanVO>> querySubUserByAccount(Long id, Integer current, Integer size, String keyword);

    /**
     * 更新子账号配置
     *
     * @param id         代理账号ID
     * @param accountDTO 子账号配置信息
     * @author DazeCake
     * @date 2023/1/26 20:30
     */
    void updateSubAccount(Long id, AccountDTO accountDTO);

    /**
     * 查询子账号日志
     *
     * @param id      代理账号ID
     * @param userID  子账号ID
     * @param current 当前页
     * @param size    每页大小
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.query.PageQueryVO < moe.dazecake.inquisition.model.dto.account.AccountDTO>>
     * @author DazeCake
     * @date 2023/1/26 20:42
     */
    Result<PageQueryVO<LogDTO>> querySubUserLogByAccount(Long id, Long userID, Integer current, Integer size);

    /**
     * 强制子账号立即作战
     *
     * @param id     代理账号ID
     * @param userID 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 21:01
     */
    Result<String> forceFightSubUser(Long id, Long userID);

    /**
     * 强制子账号停止作战
     *
     * @param id     代理账号ID
     * @param userID 用户ID
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 21:13
     */
    Result<String> forceStopSubUser(Long id, Long userID);

    /**
     * 为子账号激活cdk
     *
     * @param userID 用户ID
     * @param cdk    cdk
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 22:01
     */
    Result<String> activateSubUserCdk(Long userID, String cdk);

    /**
     * 获取代理账号的cdk列表
     *
     * @param id 代理账号ID
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.cdk.CDKListVO>
     * @author DazeCake
     * @date 2023/1/26 22:03
     */
    Result<CDKListVO> queryProUserCDKList(Long id);

    /**
     * 代理账号创建cdk
     *
     * @param createCDKDTO 创建cdk信息
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 22:23
     */
    Result<String> createCdkByProUser(Long id, CreateCDKDTO createCDKDTO);

    /**
     * 手动续费用户时长
     *
     * @param id     代理账号ID
     * @param userID 用户ID
     * @param mo     月数
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/26 22:36
     */
    Result<String> renewSubUserDaily(Long id, Long userID, Integer mo);

    /**
     * 手动创建用户
     *
     * @param name     昵称
     * @param account  账号
     * @param password 密码
     * @param server   服务器
     * @param days     时长
     * @return
     */
    Result<String> createSubUserByProUser(Long id, String name, String account, String password, Long server, Integer days);

    /**
     * 获取最近到期的用户
     *
     * @param id 代理账号ID
     * @return: moe.dazecake.inquisition.utils.Result<java.util.ArrayList < moe.dazecake.inquisition.model.dto.account.AccountDTO>>
     * @author DazeCake
     * @date 2023/3/13 22:09
     */
    Result<ArrayList<AccountDTO>> getRecentlyExpiredUsers(Long id);
}
