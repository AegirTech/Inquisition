package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.model.dto.notice.CreateAnnounceDTO;

public interface NoticeService {

    /**
     * 创建一条公告
     *
     * @param createAnnounceDTO 公告内容
     * @author DazeCake
     * @date 2023/1/26 17:14
     */
    void createAnnouncement(CreateAnnounceDTO createAnnounceDTO);

}
