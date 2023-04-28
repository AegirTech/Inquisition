package moe.dazecake.inquisition.mapper.mapstruct;

import moe.dazecake.inquisition.model.dto.goods.AddGoodsDTO;
import moe.dazecake.inquisition.model.dto.goods.UpdateGoodsDTO;
import moe.dazecake.inquisition.model.entity.GoodsEntity;
import moe.dazecake.inquisition.model.vo.goods.GoodsInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GoodsConvert {
    GoodsConvert INSTANCE = Mappers.getMapper(GoodsConvert.class);

    GoodsInfoVO toGoodsInfoVO(GoodsEntity goodsEntity);

    GoodsEntity toGoodsEntity(AddGoodsDTO addGoodsDTO);

    GoodsEntity toGoodsEntity(UpdateGoodsDTO updateGoodsDTO);
}
