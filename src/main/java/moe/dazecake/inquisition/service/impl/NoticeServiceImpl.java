package moe.dazecake.inquisition.service.impl;

import moe.dazecake.inquisition.model.dto.notice.CreateAnnounceDTO;
import moe.dazecake.inquisition.service.intf.NoticeService;
import moe.dazecake.inquisition.utils.DynamicInfo;
import moe.dazecake.inquisition.utils.Encoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    DynamicInfo dynamicInfo;

    @Override
    public void createAnnouncement(CreateAnnounceDTO createAnnounceDTO) {
        dynamicInfo.getAnnouncement().put("title", createAnnounceDTO.getTitle());
        dynamicInfo.getAnnouncement().put("context", createAnnounceDTO.getContext());
        dynamicInfo.getAnnouncement().put("md5", Encoder.MD5(createAnnounceDTO.getContext()));
    }
}
