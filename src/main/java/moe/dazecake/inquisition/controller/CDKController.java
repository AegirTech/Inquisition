package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.constant.enums.CDKWrapper;
import moe.dazecake.inquisition.model.dto.cdk.CreateCDKDTO;
import moe.dazecake.inquisition.model.vo.cdk.CDKListVO;
import moe.dazecake.inquisition.service.impl.CDKServiceImpl;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Tag(name = "CDK接口")
@ResponseBody
@RestController
public class CDKController {

    @Resource
    CDKServiceImpl cdkService;

    @Login
    @Operation(summary = "批量创建cdk")
    @PostMapping("/createCDK")
    public Result<String> createCDK(@RequestBody CreateCDKDTO createCDKDTO) {
        return cdkService.createCDK(createCDKDTO);
    }

    @Login
    @Operation(summary = "通过类型检查库存cdk")
    @GetMapping("/checkCDKByType")
    public Result<CDKListVO> checkCDKByType(String keyword) {
        return cdkService.queryCDKList(CDKWrapper.TYPE, keyword);
    }

    @Login
    @Operation(summary = "通过tag检查库存cdk")
    @GetMapping("/checkCDKByTag")
    public Result<CDKListVO> checkCDKByTag(String keyword) {
        return cdkService.queryCDKList(CDKWrapper.TAG, keyword);
    }
}
