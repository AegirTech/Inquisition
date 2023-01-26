package moe.dazecake.inquisition.mapper.mapstruct;

import moe.dazecake.inquisition.model.dto.log.AddLogDTO;
import moe.dazecake.inquisition.model.dto.log.LogDTO;
import moe.dazecake.inquisition.model.entity.LogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LogConvert {

    LogConvert INSTANCE = Mappers.getMapper(LogConvert.class);

    LogEntity toLogEntity(AddLogDTO addLogDTO);

    LogDTO toLogDTO(LogEntity logEntity);

}
