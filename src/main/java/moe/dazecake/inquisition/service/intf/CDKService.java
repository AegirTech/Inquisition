package moe.dazecake.inquisition.service.intf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import moe.dazecake.inquisition.constant.enums.CDKWrapper;
import moe.dazecake.inquisition.model.dto.cdk.CreateCDKDTO;
import moe.dazecake.inquisition.model.entity.AccountEntity;
import moe.dazecake.inquisition.model.entity.CDKEntity;
import moe.dazecake.inquisition.model.vo.cdk.CDKListVO;
import moe.dazecake.inquisition.utils.Result;

public interface CDKService {

    /**
     * 激活cdk
     *
     * @param id  用户id
     * @param cdk cdk
     * @return: int cdk激活状态码
     * @author DazeCake
     * @date 2023/1/26 11:05
     */
    Result<String> activateCDK(Long id, String cdk);

    /**
     * 通过cdk创建用户
     *
     * @param accountEntity 用户实体
     * @param cdk           cdk
     * @return: int 创建状态码
     * @author DazeCake
     * @date 2023/1/26 11:06
     */
    Result<String> createUserByCDK(AccountEntity accountEntity, String cdk);

    /**
     * 创建cdk
     *
     * @param createCDKDTO 创建参数
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.cdk.CDKListVO> 创建完的cdk列表信息
     * @author DazeCake
     * @date 2023/1/26 11:16
     */
    Result<String> createCDK(CreateCDKDTO createCDKDTO);

    /**
     * 创建cdk查询条件
     *
     * @param cdkWrapper cdk查询条件
     * @param keyword    关键词
     * @return: com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<moe.dazecake.inquisition.model.entity.CDKEntity>
     * @author DazeCake
     * @date 2023/1/26 11:34
     */
    LambdaQueryWrapper<CDKEntity> createCDKWrapper(CDKWrapper cdkWrapper, String keyword);

    /**
     * 查询cdk列表
     *
     * @param cdkWrapper cdk查询条件
     * @param keyword    关键词
     * @return: moe.dazecake.inquisition.utils.Result<moe.dazecake.inquisition.model.vo.cdk.CDKListVO>
     * @author DazeCake
     * @date 2023/1/26 11:49
     */
    Result<CDKListVO> queryCDKList(CDKWrapper cdkWrapper, String keyword);
}
