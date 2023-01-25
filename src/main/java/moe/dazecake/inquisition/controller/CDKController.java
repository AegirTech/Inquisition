package moe.dazecake.inquisition.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.mapper.CDKMapper;
import moe.dazecake.inquisition.model.entity.CDKEntity;
import moe.dazecake.inquisition.util.Result;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;

@Tag(name = "CDK接口")
@ResponseBody
@RestController
public class CDKController {

    @Resource
    CDKMapper cdkMapper;

    @Login
    @Operation(summary = "批量创建cdk")
    @PostMapping("/createCDK")
    public Result<ArrayList<CDKEntity>> createCDK(String type, Integer param, String tag, int count) {
        ArrayList<CDKEntity> newCDKList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            newCDKList.add(CDKEntity.builder()
                    .id(0L)
                    .cdk(RandomStringUtils.randomAlphabetic(32))
                    .type(type)
                    .param(param)
                    .tag(tag)
                    .used(0)
                    .build());
        }
        newCDKList.forEach(cdkMapper::insert);
        return new Result<>(200, "success", newCDKList);
    }

    @Login
    @Operation(summary = "通过类型检查库存cdk")
    @GetMapping("/checkCDKByType")
    public Result<ArrayList<CDKEntity>> checkCDKByType(String type) {
        ArrayList<CDKEntity> cdkList = (ArrayList<CDKEntity>) cdkMapper.selectList(
                Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getType, type)
                        .eq(CDKEntity::getUsed, 0)
        );
        return new Result<>(200, "success", cdkList);
    }

    @Login
    @Operation(summary = "通过tag检查库存cdk")
    @GetMapping("/checkCDKByTag")
    public Result<ArrayList<CDKEntity>> checkCDKByTag(String tag) {
        ArrayList<CDKEntity> cdkList = (ArrayList<CDKEntity>) cdkMapper.selectList(
                Wrappers.<CDKEntity>lambdaQuery()
                        .eq(CDKEntity::getTag, tag)
                        .eq(CDKEntity::getUsed, 0)
        );
        return new Result<>(200, "success", cdkList);
    }
}
