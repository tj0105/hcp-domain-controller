package org.onosproject.hcp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.HCPIOT;

import java.util.List;

/**
 * @Author ldy
 * @Date: 2021/1/5 下午5:48
 * @Version 1.0
 */
public interface HCPIoTUpdate extends HCPObject, HCPMessage{
    HCPVersion getVersion();
    HCPType getType();
    long getXid();
    DomainId getDomainId();
    List<HCPIOT> getIoTs();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();

    public interface Builder extends HCPMessage.Builder{
        HCPIoTUpdate build();
        HCPVersion getVersion();
        HCPType getType();
        long getXid();
        Builder setXid(long xid);
        DomainId getDomainId();
        Builder setDomainId(DomainId domainId);
        List<HCPIOT> getIoTs();
        Builder setIoTs(List<HCPIOT> iotList);
    }
}
