package org.onosproject.hcp.protocol.ver10;

import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.errormsg.HCPErrorMsgBuild;
import org.onosproject.hcp.types.XidGenerators;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-24 下午10:35
 * @Version 1.0
 */
public class HCPFactoryVer10 implements HCPFactory{

    //singleInstance
    public final static HCPFactoryVer10 INSTANCE=new HCPFactoryVer10();
    //XidGenerator
    private static final XidGenerator xidGenerator=XidGenerators.global();
    @Override
    public HCPErrorMsgBuild errorMessage() {
        return HCPErrorMessageBuildVer10.INSTANCE;
    }

    @Override
    public HCPHello.Builder buildHello() {
        return new HCPHelloVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPHello hello(List<HCPHelloElem> list) {
        return new HCPHelloVer10(nextXid());
    }

    @Override
    public HCPEchoRequest.Builder buildEchoRequest() {
        return new HCPEchoRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPEchoReply.Builder buildEchoReply() {
        return new HCPEchoReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPFeaturesRequest.Builder buildFeaturesRequest() {
        return new HCPFeaturesRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPFeaturesReply.Builder buildFeaturesReply() {
        return new HCPFeaturesReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPGetConfigRequest.Builder buildGetConfitRequest() {
        return new HCPGetConfigRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPGetConfigReply.Builder buildGetConfigReply() {
        return new HCPGetConfigReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPSetConfig.Builder buildSetConfig() {
        return new HCPSetConfigVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPTopologyRequest.Builder buildTopoRequest() {
        return new HCPTopologyRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPTopologyReply.Builder buildTopoReply() {
        return new HCPTopologyReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPHostRequest.Builder buildHostRequest() {
        return new HCPHostRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPHostReply.Builder buildHostReply() {
        return new HCPHostReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPHostUpdate.Builder buildHostUpdate() {
        return new HCPHostUpdateVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPVportStatus.Builder buildVportStatus() {
        return new HCPVportStatusVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPSbp.Builder buildSbp() {
        return new HCPSbpVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPIoTRequest.Builder buildIoTRequest() {
        return new HCPIoTRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPIoTReply.Builder buildIoTReply() {
        return new HCPIoTReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPIoTUpdate.Builder buildIoTUpdate() {
        return new HCPIoTUpdateVer10.Builder().setXid(nextXid());
    }

    @Override
    public HCPMessageReader<HCPMessage> getReader() {
        return  HCPMessageVer10.READER;
    }

    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public long nextXid() {
        return xidGenerator.nextXid();
    }
}
