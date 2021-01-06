package org.onosproject.hcp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @Author ldy
 * @Date: 2021/1/5 下午4:25
 * @Version 1.0
 */
public interface HCPIoTRequest  extends HCPMessage,HCPObject{
    HCPVersion getVersion();
    HCPType getType();
    long getXid();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends HCPMessage.Builder{
        HCPIoTRequest build();
        HCPVersion getVersion();
        HCPType getType();
        long getXid();
        Builder setXid(long xid);
    }
}
