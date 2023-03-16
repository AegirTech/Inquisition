package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.admin.ChangeAdminPasswordDTO;
import moe.dazecake.inquisition.model.dto.admin.LoginAdminDTO;
import moe.dazecake.inquisition.model.vo.admin.AddProUserBalanceDTO;
import moe.dazecake.inquisition.model.vo.admin.AdminLoginVO;
import moe.dazecake.inquisition.utils.Result;

public interface AdminService {

    /**
     * 登录管理员账户
     *
     * @param loginAdminDTO 账号密码
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String> 返回类
     * @author DazeCake
     * @date 2023/1/26 10:23
     */
    Result<AdminLoginVO> loginAdmin(LoginAdminDTO loginAdminDTO);

    Result<String> updateAdminPassword(ChangeAdminPasswordDTO changeAdminPasswordDTO);

    /**
     * 增加代理用户余额
     *
     * @param addProUserBalanceDTO 代理id和增加余额
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String> 返回消息
     * @author DazeCake
     * @date 2023/1/26 10:57
     */
    Result<String> addBalanceForProUser(AddProUserBalanceDTO addProUserBalanceDTO);

}
