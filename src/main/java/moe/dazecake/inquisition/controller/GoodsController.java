package moe.dazecake.inquisition.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import moe.dazecake.inquisition.annotation.Login;
import moe.dazecake.inquisition.annotation.UserLogin;
import moe.dazecake.inquisition.model.dto.goods.AddGoodsDTO;
import moe.dazecake.inquisition.model.dto.goods.UpdateGoodsDTO;
import moe.dazecake.inquisition.model.vo.goods.GoodsInfoVO;
import moe.dazecake.inquisition.service.impl.GoodsServiceImpl;
import moe.dazecake.inquisition.utils.JWTUtils;
import moe.dazecake.inquisition.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;

@Tag(name = "商品接口")
@ResponseBody
@RestController
public class GoodsController {

    @Resource
    GoodsServiceImpl goodsService;

    @Login
    @Operation(summary = "获取商品列表(admin)")
    @GetMapping("/getGoodsListByAdmin")
    public Result<ArrayList<GoodsInfoVO>> getGoodsListByAdmin() {
        return goodsService.getGoodsList(true);
    }

    @UserLogin
    @Operation(summary = "获取商品列表")
    @GetMapping("/getGoodsList")
    public Result<ArrayList<GoodsInfoVO>> getGoodsList() {
        return goodsService.getGoodsList(false);
    }

    @Login
    @Operation(summary = "增加商品")
    @PostMapping("/addGoods")
    public Result<String> addGoods(AddGoodsDTO addGoodsDTO) {
        return goodsService.addGoods(addGoodsDTO);
    }

    @Login
    @Operation(summary = "更新商品")
    @PostMapping("/updateGoods")
    public Result<String> updateGoods(UpdateGoodsDTO updateGoodsDTO) {
        return goodsService.updateGoods(updateGoodsDTO);
    }

    @UserLogin
    @Operation(summary = "获取商品购买url")
    @GetMapping("/getGoodsPayUrl")
    public Result<String> getGoodsPayUrl(@RequestHeader("Authorization") String token, Long goodsId, String payType) {
        return goodsService.getGoodsPayUrl(JWTUtils.getId(token), goodsId, payType);
    }

}
