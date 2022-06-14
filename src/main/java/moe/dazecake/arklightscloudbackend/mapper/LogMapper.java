package moe.dazecake.arklightscloudbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.dazecake.arklightscloudbackend.entity.LogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<LogEntity> {
}
