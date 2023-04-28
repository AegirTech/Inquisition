package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.goods.AddGoodsDTO;
import moe.dazecake.inquisition.model.dto.goods.UpdateGoodsDTO;
import moe.dazecake.inquisition.model.vo.goods.GoodsInfoVO;
import moe.dazecake.inquisition.utils.Result;

import java.util.ArrayList;

public interface GoodsService {

    Result<ArrayList<GoodsInfoVO>> getGoodsList(Boolean showAll);

    Result<String> addGoods(AddGoodsDTO addGoodsDTO);

    Result<String> updateGoods(UpdateGoodsDTO updateGoodsDTO);

}
