package org.onosproject.hcp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPMessageReader;
import org.onosproject.hcp.types.HCPInternalLink;
import org.onosproject.hcp.types.HCPVport;
import org.onosproject.hcp.types.HCPVportHop;

/**
 * @Author ldy
 * @Date: 20-3-28 上午12:42
 * @Version 1.0
 */
abstract class HCPVportHopVer10 {

    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPVportHop>{

        @Override
        public HCPVportHop readFrom(ChannelBuffer bb) throws HCPParseError {
            HCPVport hcpVport=HCPVport.readFrom(bb);
            int hop=bb.readInt();

            return  HCPVportHop.of(hcpVport,hop);
        }
    }
}
