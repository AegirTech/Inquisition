package moe.dazecake.inquisition.model.vo.query;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageQueryVO<T> {

    private Long current;

    private Long total;

    private List<T> records = new ArrayList<>();
}
