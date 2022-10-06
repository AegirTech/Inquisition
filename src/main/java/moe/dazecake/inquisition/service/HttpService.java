package moe.dazecake.inquisition.service;

import org.springframework.web.multipart.MultipartFile;

public interface HttpService {

    boolean isOfficialAccountWork(String account, String password);

    boolean isBiliAccountWork(String account, String password);

    String uploadFile(MultipartFile file,String md5,boolean isBate);

    String getDownloadUrl(boolean isBate,boolean isMD5);

    void updateLatestMD5();

}
