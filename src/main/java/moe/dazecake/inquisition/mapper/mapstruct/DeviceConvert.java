package moe.dazecake.inquisition.mapper.mapstruct;

import moe.dazecake.inquisition.model.dto.device.AddChinacDeviceDTO;
import moe.dazecake.inquisition.model.dto.device.AddCommonDeviceDTO;
import moe.dazecake.inquisition.model.dto.device.AddDeviceDTO;
import moe.dazecake.inquisition.model.dto.device.UpdateDeviceDTO;
import moe.dazecake.inquisition.model.entity.DeviceEntity;
import moe.dazecake.inquisition.model.vo.device.DeviceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DeviceConvert {
    DeviceConvert INSTANCE = Mappers.getMapper(DeviceConvert.class);

    @Mappings({
            @Mapping(target = "delete", defaultValue = "0")
    })
    DeviceEntity toDeviceEntity(AddDeviceDTO addDeviceDTO);

    DeviceEntity toDeviceEntity(UpdateDeviceDTO updateDeviceDTO);

    @Mappings({
            @Mapping(target = "id", constant = "0L"),
            @Mapping(target = "chinac", constant = "0"),
            @Mapping(target = "region", expression = "java(null)"),
            @Mapping(target = "delete", constant = "0"),
            @Mapping(target = "deviceToken", expression = "java(org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(16))")
    })
    AddDeviceDTO toAddDeviceDTO(AddCommonDeviceDTO addCommonDeviceDTO);

    @Mappings({
            @Mapping(target = "id", constant = "0L"),
            @Mapping(target = "delete", constant = "0")
    })
    AddDeviceDTO toAddDeviceDTO(AddChinacDeviceDTO addChinacDeviceDTO);

    DeviceVO toDeviceVO(DeviceEntity deviceEntity);
}
