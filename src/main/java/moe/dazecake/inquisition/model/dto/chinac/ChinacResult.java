package moe.dazecake.inquisition.model.dto.chinac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChinacResult<T> {

    Integer code;

    String message;

    T data;

}
