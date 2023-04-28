package moe.dazecake.inquisition.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum GoodsType {

    DAILY("daily", "日常"),
    ROGUE("rogue", "傀影与猩红孤钻"),
    ROGUE2("rogue2", "水月与深蓝之树"),
    SAND_FIRE("sand_fire", "沙中之火"),

    SINGLE("single", "单次"),
    REPEAT("repeat", "重复"),

    UNKNOWN("unknown", "未知");



    private String type;
    private String name;

    public static GoodsType getByStr(String type) {
        for (GoodsType goodsType : GoodsType.values()) {
            if (goodsType.getType().equals(type)) {
                return goodsType;
            }
        }
        return UNKNOWN;
    }

}
