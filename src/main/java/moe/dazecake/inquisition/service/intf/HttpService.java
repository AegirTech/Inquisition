package moe.dazecake.inquisition.service.intf;

public interface HttpService {

    boolean isOfficialAccountWork(String account, String password);

    boolean isBiliAccountWork(String account, String password);

}
