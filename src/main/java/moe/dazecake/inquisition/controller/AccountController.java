package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.dto.account.AccountIDDTO;
import moe.dazecake.inquisition.model.dto.account.AddAccountDTO;
import moe.dazecake.inquisition.model.vo.account.AccountWithSanVO;
import moe.dazecake.inquisition.model.vo.query.PageQueryVO;
import moe.dazecake.inquisition.service.impl.AccountServiceImpl;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

@Tag(name = "账号接口")
@ResponseBody
@RestController
public class AccountController {

    @Resource
    AccountServiceImpl accountService;

    @Login
    @Operation(summary = "增加账号")
    @PostMapping("/addAccount")
    public Result<String> addAccount(@RequestBody AddAccountDTO addAccountDTO) {
        accountService.addAccount(addAccountDTO);
        return Result.success(null, "添加成功");
    }


    @Login
    @Operation(summary = "从速通迁移账号")
    @PostMapping("/transferAccountFromArkLights")
    public Result<String> transferAccountFromArkLights(@RequestBody HashMap<String, String> accountJson) {
        return Result.success(null, "已成功添加" + accountService.transferAccount(accountJson) + "个账号");
    }


    @Login
    @Operation(summary = "删除账号")
    @PostMapping("/delAccount")
    public Result<String> delAccount(@RequestBody AccountIDDTO accountIDDTO) {
        accountService.deleteAccount(accountIDDTO.getId());
        return Result.success(null, "删除成功");
    }

    @Login
    @Operation(summary = "分页查询账号")
    @GetMapping("/showAccount")
    public Result<PageQueryVO<AccountWithSanVO>> showAccount(Long current, Long size) {
        return Result.success(accountService.queryAllAccount(current, size), "查询成功");
    }

    @Login
    @Operation(summary = "搜索账号")
    @GetMapping("/searchAccount")
    public Result<PageQueryVO<AccountWithSanVO>> searchAccount(Long current, Long size, String keyword) {
        return Result.success(accountService.queryAccount(current, size, keyword), "查询成功");
    }

    @Login
    @Operation(summary = "更新账号")
    @PostMapping("/updateAccount")
    public Result<String> updateAccount(@RequestBody AccountDTO accountDTO) {
        accountService.updateAccount(accountDTO);
        return Result.success(null, "更新成功");
    }

    @Login
    @Operation(summary = "重置刷新次数")
    @PostMapping("/resetRefresh")
    public Result<String> resetRefresh(@RequestBody AccountIDDTO accountIDDTO) {
        accountService.resetAccountRefresh(accountIDDTO.getId(), 1);
        return Result.success(null, "重置成功");
    }

    @Login
    @Operation(summary = "账号立即作战")
    @PostMapping("/startAccountByAdmin")
    public Result<String> startAccountByAdmin(@RequestBody AccountIDDTO accountIDDTO) {
        return Result.success(null, accountService.forceFightAccount(accountIDDTO.getId(), true));
    }

    @Login
    @Operation(summary = "重置账号动态信息")
    @PostMapping("/resetAccountDynamicInfo")
    public Result<String> fixAccount(@RequestBody AccountIDDTO accountIDDTO) {
        return Result.success(null, accountService.resetAccountDynamicInfo(accountIDDTO.getId()));
    }
}
