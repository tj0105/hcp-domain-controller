package org.onosproject.hcp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPIoTState;
import org.onosproject.hcp.protocol.HCPIoTType;
import org.onosproject.hcp.protocol.HCPMessageReader;
import org.onosproject.hcp.types.HCPIOT;
import org.onosproject.hcp.types.HCPIOTID;
import org.onosproject.hcp.types.IPv4Address;

/**
 * @Author ldy
 * @Date: 2021/1/5 下午3:57
 * @Version 1.0
 */
abstract class HCPIoTVer10 {
    static final int LENGHT = 11;
    static final Reader READER = new Reader();
    static class Reader implements HCPMessageReader<HCPIOT> {

        @Override
        public HCPIOT readFrom(ChannelBuffer bb) throws HCPParseError {
            IPv4Address iPv4Address = IPv4Address.read4Bytes(bb);
            HCPIoTType ioTType = HCPIoTTypeSerializerVer10.readFrom(bb);
            HCPIOTID hcpiotid = HCPIOTID.read(bb);
            HCPIoTState hcpIoTState = HCPIoTStateSerializerVer10.readFrom(bb);
            return HCPIOT.of(iPv4Address,ioTType,hcpiotid,hcpIoTState);
        }
    }
}
