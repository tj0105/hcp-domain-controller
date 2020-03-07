package org.onosproject.hcp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.HCPHost;
import org.onosproject.hcp.types.HCPValueType;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-13 下午7:05
 * @Version 1.0
 */
public interface HCPHostReply extends HCPObject,HCPMessage {
    HCPVersion getVersion();
    HCPType getType();
    long getXid();
    DomainId getDomainId();
    List<HCPHost> getHosts();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();

    public interface Builder extends HCPMessage.Builder{
        HCPHostReply build();
        HCPVersion getVersion();
        HCPType getType();
        long getXid();
        Builder setXid(long xid);
        List<HCPHost> getHosts();
        Builder setHosts(List<HCPHost> hostList);
        DomainId getDomainId();
        Builder setDomainId(DomainId domainId);

    }
}
