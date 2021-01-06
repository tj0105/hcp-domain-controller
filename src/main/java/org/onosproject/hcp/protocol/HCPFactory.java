package org.onosproject.hcp.protocol;

import org.onosproject.hcp.protocol.errormsg.HCPErrorMsgBuild;
import org.onosproject.hcp.types.HCPVport;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-14 下午8:30
 * @Version 1.0
 */
public interface HCPFactory extends XidGenerator {

    HCPErrorMsgBuild errorMessage();

    HCPHello.Builder buildHello();
    HCPHello hello(List<HCPHelloElem> list);
    HCPEchoRequest.Builder buildEchoRequest();
    HCPEchoReply.Builder buildEchoReply();
    HCPFeaturesRequest.Builder buildFeaturesRequest();
    HCPFeaturesReply.Builder buildFeaturesReply();
    HCPGetConfigRequest.Builder buildGetConfitRequest();
    HCPGetConfigReply.Builder buildGetConfigReply();
    HCPSetConfig.Builder buildSetConfig();
    HCPTopologyRequest.Builder buildTopoRequest();
    HCPTopologyReply.Builder buildTopoReply();
    HCPHostRequest.Builder buildHostRequest();
    HCPHostReply.Builder buildHostReply();
    HCPHostUpdate.Builder buildHostUpdate();
    HCPVportStatus.Builder buildVportStatus();
    HCPSbp.Builder buildSbp();
    HCPIoTRequest.Builder buildIoTRequest();
    HCPIoTReply.Builder buildIoTReply();
    HCPIoTUpdate.Builder buildIoTUpdate();
    HCPMessageReader<HCPMessage> getReader();

    HCPVersion getVersion();

}
