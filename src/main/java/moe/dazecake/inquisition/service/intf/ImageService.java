package moe.dazecake.inquisition.service.intf;

import moe.dazecake.inquisition.utils.Result;

public interface ImageService {

    Result<String> uploadImage(String base64Image);
    

}
