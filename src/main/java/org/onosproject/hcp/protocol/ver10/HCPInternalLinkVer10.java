package org.onosproject.hcp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPMessageReader;
import org.onosproject.hcp.types.HCPInternalLink;
import org.onosproject.hcp.types.HCPVport;

/**
 * @Author ldy
 * @Date: 20-2-24 下午8:26
 * @Version 1.0
 */
abstract class HCPInternalLinkVer10 {
    final static int LENGTH=12;

    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPInternalLink>{

        @Override
        public HCPInternalLink readFrom(ChannelBuffer bb) throws HCPParseError {
            //srcVPort is the domain external port
            //dstVport is the domain external port
            HCPVport srcVport=HCPVport.readFrom(bb);
            HCPVport dstVport=HCPVport.readFrom(bb);

            long BandWidthcapability=bb.readLong();
            int  Delaycapability=bb.readInt();
            int  Hopcapanility=bb.readInt();
            return HCPInternalLink.of(srcVport,dstVport,BandWidthcapability,Delaycapability,Hopcapanility);
        }
    }
}
