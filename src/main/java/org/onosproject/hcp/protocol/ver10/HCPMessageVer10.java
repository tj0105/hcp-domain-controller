package org.onosproject.hcp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;

/**
 * @Author ldy
 * @Date: 20-2-23 下午9:16
 * @Version 1.0
 */
public final class HCPMessageVer10 {
    //version=1.0
    public final static byte WIRE_VERSION=1;
    public final static int MINIMUN_LENGTH=8;

    private HCPMessageVer10(){

    }

    public final static Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPMessage>{

        @Override
        public HCPMessage readFrom(ChannelBuffer bb) throws HCPParseError {
            if (bb.readableBytes()<MINIMUN_LENGTH)
                return null;
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            if (version != (byte) 0x1) {
                throw new HCPParseError("Wrong version: Expected=HCPVERSION.OF_10(1), got=" + version);
            }
            byte type=bb.readByte();
            bb.readerIndex(startIndex);
            switch (type){
                case (byte)0x0:
                    // discriminator value HCPType=0 for class HCPHelloVer10
                    return HCPHelloVer10.READER.readFrom(bb);
                case (byte)0x1:
                    // discriminator value HCPType=1 for class HCPErroMessageVer10
                    return HCPErrorMessageVer10.READER.readFrom(bb);
                case (byte)0x2:
                    // discriminator value HCPType=2 for class HCPEchoRequestVer10
                    return HCPEchoRequestVer10.READER.readFrom(bb);
                case (byte)0x3:
                    // discriminator value HCPType=3 for class HCPEchoReplyVer10
                    return HCPEchoReplyVer10.READER.readFrom(bb);
                case (byte)0x4:
                    // discriminator value HCPType=4 for class HCPFeatureRequestVer10
                    return HCPFeaturesRequestVer10.READER.readFrom(bb);
                case (byte)0x5:
                    // discriminator value HCPType=5 for class HCPFeaturesReplyVer10
                    return HCPFeaturesReplyVer10.READER.readFrom(bb);
                case (byte)0x6:
                    // discriminator value HCPType=6 for class HCPGetConfigRequestVer10
                    return HCPGetConfigRequestVer10.READER.readFrom(bb);
                case (byte)0x7:
                    // discriminator value HCPType=7 for class HCPGetConfigReplyVer10
                    return HCPGetConfigReplyVer10.READER.readFrom(bb);
                case (byte)0x8:
                    // discriminator value HCPType=8 for class HCPSetConfigVer10
                    return HCPSetConfigVer10.READER.readFrom(bb);
                case (byte)0x9:
                    // discriminator value HCPType=9 for class HCPTopologyRequestVer10
                    return HCPTopologyRequestVer10.READER.readFrom(bb);
                case (byte)0xa:
                    //discriminator value HCPType=10 for class HCPTopologyReplyVer10
                    return HCPTopologyReplyVer10.READER.readFrom(bb);
                case (byte)0xb:
                    //discriminator value HCPType=11 for class HCPHostRequestVer10
                    return HCPHostRequestVer10.READER.readFrom(bb);
                case (byte)0xc:
                    //discriminator value HCPType=12 for class HCPHostReplyVer10
                    return HCPHostReplyVer10.READER.readFrom(bb);
                case (byte)0xd:
                    //discriminator value HCPType=13 for class HCPHostUpdateVer10
                    return HCPHostUpdateVer10.READER.readFrom(bb);
                case (byte)0xe:
                    //discriminator value HCPType=14 for class HCPVportStatusVer10
                    return HCPVportStatusVer10.READER.readFrom(bb);
                case (byte)0xf:
                    //discriminator value HCPType=15 for class HCPSbpMessageVer10
                    return HCPSbpVer10.READER.readFrom(bb);
                case (byte)0x10:
                    //discriminator value HCPType=16 for class HCPIoTRequestMessageVer10
                    return HCPIoTRequestVer10.READER.readFrom(bb);
                case (byte)0x11:
                    //discriminator value HCPType=17 for class HCPIoTReplyMessageVer10
                    return HCPIoTReplyVer10.READER.readFrom(bb);
                case (byte) 0x12:
                    //discriminator value HCPType=18 for class HCPIoTUpdateMessageVer10
                    return HCPIoTUpdateVer10.READER.readFrom(bb);
                default:
                    throw new HCPParseError("Unknown value for discriminator type of class HCPMessageVer10: " + type);

            }
        }
    }
}

