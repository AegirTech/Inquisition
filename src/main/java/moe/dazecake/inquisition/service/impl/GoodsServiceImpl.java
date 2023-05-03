package moe.dazecake.inquisition.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import moe.dazecake.inquisition.mapper.GoodsMapper;
import moe.dazecake.inquisition.mapper.mapstruct.GoodsConvert;
import moe.dazecake.inquisition.model.dto.goods.AddGoodsDTO;
import moe.dazecake.inquisition.model.dto.goods.UpdateGoodsDTO;
import moe.dazecake.inquisition.model.entity.GoodsEntity;
import moe.dazecake.inquisition.model.vo.goods.GoodsInfoVO;
import moe.dazecake.inquisition.service.intf.GoodsService;
import moe.dazecake.inquisition.utils.Result;

import javax.annotation.Resource;
import java.util.ArrayList;

public class GoodsServiceImpl implements GoodsService {

    @Resource
    GoodsMapper goodsMapper;

    @Resource
    PayServiceImpl payService;

    @Override
    public Result<ArrayList<GoodsInfoVO>> getGoodsList(Boolean showAll) {
        var goodsList = new ArrayList<GoodsInfoVO>();


        if (showAll) {
            for (GoodsEntity goods : goodsMapper.selectList(Wrappers.<GoodsEntity>lambdaQuery()
                    .eq(GoodsEntity::getOnSale, 1))) {
                goodsList.add(GoodsConvert.INSTANCE.toGoodsInfoVO(goods));
            }
        } else {
            for (GoodsEntity goods : goodsMapper.selectList(null)) {
                goodsList.add(GoodsConvert.INSTANCE.toGoodsInfoVO(goods));
            }
        }
        return Result.success(goodsList, "查询成功");
    }

    @Override
    public Result<String> addGoods(AddGoodsDTO addGoodsDTO) {
        goodsMapper.insert(GoodsConvert.INSTANCE.toGoodsEntity(addGoodsDTO));
        return Result.success("添加成功");
    }

    @Override
    public Result<String> updateGoods(UpdateGoodsDTO updateGoodsDTO) {
        goodsMapper.updateById(GoodsConvert.INSTANCE.toGoodsEntity(updateGoodsDTO));
        return Result.success("更新成功");
    }

    @Override
    public Result<String> getGoodsPayUrl(Long userId, Long goodsId, String payType) {
        var goods = goodsMapper.selectById(goodsId);
        if (goods.getOnSale() != 1) {
            return Result.forbidden("商品已下架");
        }

        var url = payService.createBill(userId, goods.getParams(), goods.getType(), goods.getPrice(), payType, "/user/home/");
        if (url != null) {
            return Result.success(url, "创建订单成功");
        } else {
            return Result.failed("创建订单失败，请稍后尝试");
        }
    }
}
