package moe.dazecake.inquisition.mapper.mapstruct;

import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.vo.account.AccountWithSanVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AccountConvert {

    AccountConvert INSTANCE = Mappers.getMapper(AccountConvert.class);

    AccountWithSanVO toAccountWithSanVO(AccountEntity accountEntity, String san);

    AccountEntity toAccountEntity(AccountDTO accountDTO);

    AccountDTO toAccountDTO(AccountEntity accountEntity);
}
