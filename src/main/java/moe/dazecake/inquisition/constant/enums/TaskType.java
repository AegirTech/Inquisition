package moe.dazecake.inquisition.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum TaskType {
    DAILY("daily","日常"),
    ROGUE("rogue","傀影与猩红孤钻"),
    ROGUE2("rogue2","水月与深蓝之树"),
    SAND_FIRE("sand_fire","沙中之火"),
    UNKNOWN("unknown", "未知");;

    private String type;
    private String name;

    public static TaskType getByStr(String type){
        for(TaskType taskType : TaskType.values()){
            if(taskType.getType().equals(type)){
                return taskType;
            }
        }
        return UNKNOWN;
    }
}
