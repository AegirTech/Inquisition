package moe.dazecake.inquisition.model.local;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkUser {

    private String deviceToken = "";

    private LocalDateTime expirationTime;

}
