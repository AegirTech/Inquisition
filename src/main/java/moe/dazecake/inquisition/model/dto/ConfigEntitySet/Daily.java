package moe.dazecake.inquisition.model.dto.ConfigEntitySet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Daily {

    private List<Fight> fight = new ArrayList<>() {
        {
            add(new Fight("jm", 5));
            add(new Fight("ls", 99));
            add(new Fight("ce", 99));
            add(new Fight("1-7", 99));
        }
    };
    private Sanity sanity = new Sanity(0, 0);
    private boolean mail = true;
    private Offer offer = new Offer(
            true,
            false,
            true,
            false,
            false,
            false);
    private boolean friend = true;
    private Infrastructure infrastructure = new Infrastructure(
            true,
            true,
            true,
            true,
            false
    );
    private boolean credit = true;
    private boolean task = true;
    private boolean activity = true;

}