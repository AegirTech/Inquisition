package moe.dazecake.arklightscloudbackend.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import moe.dazecake.arklightscloudbackend.entity.AccountEntity;
import moe.dazecake.arklightscloudbackend.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableScheduling
public class DynamicScheduleTask implements SchedulingConfigurer {

    @Resource
    DynamicInfo dynamicInfo;

    @Resource
    AccountMapper accountMapper;

    @Value("${cron}")
    String cron;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                () -> {
                    log.info("正在刷新任务列表: " + LocalDateTime.now().toLocalTime());
                    if (dynamicInfo.getFreeTaskList().isEmpty()) {
                        dynamicInfo.getFreeTaskList().addAll(
                                accountMapper.selectList(
                                        Wrappers.<AccountEntity>lambdaQuery()
                                                .eq(AccountEntity::getDelete, 0)
                                                .eq(AccountEntity::getTaskType, "daily")
                                )
                        );
                    }
                },
                triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext)
        );
        taskRegistrar.addTriggerTask(
                () -> dynamicInfo.getCounter().forEach(
                        (token, num) -> {
                            if (num == 0) {
                                dynamicInfo.getDeviceStatusMap().put(token, 0);
                                log.warn("设备离线: " + token);
                            } else {
                                --num;
                                dynamicInfo.getCounter().put(token, num);
                            }
                        }
                ),
                triggerContext -> new CronTrigger("0/5 * * * * ?").nextExecutionTime(triggerContext)
        );
    }
}
