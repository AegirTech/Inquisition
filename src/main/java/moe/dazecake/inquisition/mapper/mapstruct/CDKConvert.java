package moe.dazecake.inquisition.mapper.mapstruct;

import moe.dazecake.inquisition.model.dto.cdk.CDKDTO;
import moe.dazecake.inquisition.model.entity.CDKEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CDKConvert {

    CDKConvert INSTANCE = Mappers.getMapper(CDKConvert.class);

    CDKDTO toCDKDTO(CDKEntity cdkEntity);

}
