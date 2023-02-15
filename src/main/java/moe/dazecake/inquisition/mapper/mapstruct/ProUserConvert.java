package moe.dazecake.inquisition.mapper.mapstruct;

import moe.dazecake.inquisition.model.dto.prouser.CreateProUserDTO;
import moe.dazecake.inquisition.model.dto.prouser.ProUserDTO;
import moe.dazecake.inquisition.model.entity.ProUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProUserConvert {

    ProUserConvert INSTANCE = Mappers.getMapper(ProUserConvert.class);

    @Mappings({
            @Mapping(target = "id", constant = "0L"),
            @Mapping(target = "balance", constant = "0.0"),
            @Mapping(target = "authorization", expression = "java(org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(16))"),
            @Mapping(target = "delete", constant = "0")
    })
    ProUserEntity toProUserEntity(CreateProUserDTO createProUserDTO);

    ProUserEntity toProUserEntity(ProUserDTO proUserDTO);

    ProUserDTO toProUserDTO(ProUserEntity proUserEntity);
}
