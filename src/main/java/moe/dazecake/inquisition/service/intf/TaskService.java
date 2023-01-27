package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.account.AccountDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.utils.Result;

public interface TaskService {

    /**
     * 获取任务
     *
     * @param deviceToken 设备标识
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.dto.account.AccountDTO>
     * @author DazeCake
     * @date 2023/1/26 23:58
     */
    Result<AccountDTO> getTask(String deviceToken);

    /**
     * 完成任务上报
     *
     * @param deviceToken 设备标识
     * @param imageUrl    图片地址
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 0:06
     */
    Result<String> completeTask(String deviceToken, String imageUrl);

    /**
     * 任务失败上报
     *
     * @param deviceToken 设备标识
     * @param type        失败类型
     * @param imageUrl    图片地址
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 0:09
     */
    Result<String> failTask(String deviceToken, String type, String imageUrl);

    /**
     * 临时插队
     *
     * @param id 账号id
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 0:12
     */
    Result<String> tempInsertTask(Long id);

    /**
     * 临时移除任务
     *
     * @param id 账号id
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 0:14
     */
    Result<String> tempRemoveTask(Long id);

    /**
     * 立即从数据库重载全部任务
     *
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 0:20
     */
    Result<String> forceLoadAllTask();

    /**
     * 立即强制释放一设备的上锁任务
     *
     * @param deviceToken 设备标识
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 0:22
     */
    Result<String> forceUnlockOneTask(String deviceToken);

    /**
     * 立即强制释放整个上锁队列
     *
     * @return: moe.dazecake.inquisition.utils.Result<java.lang.String>
     * @author DazeCake
     * @date 2023/1/27 0:25
     */
    Result<String> forceUnlockTaskList();

    boolean checkActivationTime(AccountEntity account);

    boolean checkFreeze(AccountEntity account);

    void lockTask(String deviceToken, AccountEntity account);

    void log(String deviceToken, AccountEntity account, String level, String title, String content, String imgUrl);

    void errorHandle(AccountEntity account, String deviceToken, String type);

    void forceHaltTask(Long id);

    void calculatingSan();

}
