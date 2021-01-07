package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPFlowType;

/**
 * @Author ldy
 * @Date: 2021/1/6 下午6:05
 * @Version 1.0
 */
public class HCPFlowTypeSerializerVer10 {
    public final static byte HCP_HOST_VAL = 0x1;
    public final static byte HCP_IOT_VAL = 0x2;

    public static HCPFlowType readFrom(ChannelBuffer bb) throws HCPParseError{
        try {
            return ofWireValue(bb.readByte());
        }catch (IllegalArgumentException e){
            throw new HCPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, HCPFlowType hcpFlowType) {
        bb.writeByte(toWireValue(hcpFlowType));
    }

    public static void putTo(PrimitiveSink sink, HCPFlowType hcpFlowType){
        sink.putByte(toWireValue(hcpFlowType));
    }

    public static HCPFlowType ofWireValue(byte value){
        switch (value){
            case HCP_HOST_VAL:
                return HCPFlowType.HCP_HOST;
            case HCP_IOT_VAL:
                return HCPFlowType.HCP_IOT;
            default:
                throw new IllegalArgumentException("Illegal enum value for HCPFlowType in HCP protocol Version 1.0: "+ value);
        }
    }

    public static byte toWireValue(HCPFlowType hcpFlowType){
        if (hcpFlowType == null){
            throw new NullPointerException("HCPFlowType must not be null");
        }
        switch(hcpFlowType){
            case HCP_HOST:
                return HCP_HOST_VAL;
            case HCP_IOT:
                return HCP_IOT_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for HCPFlowType in HCP protocol Version 1.0: "+ hcpFlowType);
        }
    }
}
