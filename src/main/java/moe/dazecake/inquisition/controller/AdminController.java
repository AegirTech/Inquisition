package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.model.dto.admin.ChangeAdminPasswordDTO;
import moe.dazecake.inquisition.model.dto.admin.LoginAdminDTO;
import moe.dazecake.inquisition.model.dto.admin.SetServerStatueDTO;
import moe.dazecake.inquisition.model.vo.admin.AddProUserBalanceDTO;
import moe.dazecake.inquisition.model.vo.admin.AdminLoginVO;
import moe.dazecake.inquisition.service.impl.AdminServiceImpl;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Tag(name = "管理员接口")
@ResponseBody
@RestController
public class AdminController {

    @Resource
    AdminServiceImpl adminService;

    @Resource
    DynamicInfo dynamicInfo;

    @Operation(summary = "管理员登陆")
    @PostMapping("/adminLogin")
    public Result<AdminLoginVO> adminLogin(@RequestBody LoginAdminDTO loginAdminDTO) {
        return adminService.loginAdmin(loginAdminDTO);
    }

    @Login
    @Operation(summary = "修改管理员密码")
    @PostMapping("/changeAdminPassword")
    public Result<String> changeAdminPassword(@RequestBody ChangeAdminPasswordDTO changeAdminPasswordDTO) {
        return adminService.updateAdminPassword(changeAdminPasswordDTO);
    }

    @Login
    @Operation(summary = "为pro_user增加余额")
    @PostMapping("/addBalanceForProUser")
    public Result<String> addBalanceForProUser(@RequestBody AddProUserBalanceDTO addProUserBalanceDTO) {
        return adminService.addBalanceForProUser(addProUserBalanceDTO);
    }

    @Login
    @Operation(summary = "获取服务器启用状态")
    @GetMapping("/getServerStatus")
    public Result<Boolean> getServerStatus() {
        return Result.success(dynamicInfo.getActive(), "获取成功");
    }

    @Login
    @Operation(summary = "设置服务器启用状态")
    @PostMapping("/setServerStatus")
    public Result<String> setServerStatus(@RequestBody SetServerStatueDTO statueDTO) {
        dynamicInfo.setActive(statueDTO.getActive());
        return Result.success("设置成功");
    }

}
