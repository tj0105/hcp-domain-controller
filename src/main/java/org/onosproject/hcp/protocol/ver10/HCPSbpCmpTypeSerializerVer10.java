package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPSbp;
import org.onosproject.hcp.protocol.HCPSbpCmpType;

/**
 * @Author ldy
 * @Date: 20-2-19 下午9:04
 * @Version 1.0
 */
public class HCPSbpCmpTypeSerializerVer10 {
    public final static byte NORMAL_VAL=0;
    public final static byte FLOWFORWARDING_REQUESR_VAL=1;
    public final static byte FLOWFORWARDING_REPLY_VAL=2;
    public final static byte PACKET_IN_VAL=3;
    public final static byte PACKET_OUT_VAL=4;
    public final static byte RESOURCE_REQUEST_VAL=5;
    public final static byte RESOURCE_REPLY_VAL=6;

    public static HCPSbpCmpType readFrom(ChannelBuffer bb) throws HCPParseError{
        try{
            return ofWireValue(bb.readByte());
        }catch(IllegalArgumentException e){
            throw new HCPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb,HCPSbpCmpType sbpCmpType){
        bb.writeByte(toWireValue(sbpCmpType));
    }

    public static void putTo(PrimitiveSink sink,HCPSbpCmpType sbpCmpType){
        sink.putByte(toWireValue(sbpCmpType));
    }

    public static HCPSbpCmpType ofWireValue(byte value){
        switch(value){
            case NORMAL_VAL:
                return HCPSbpCmpType.NORMAL;
            case FLOWFORWARDING_REQUESR_VAL:
                return HCPSbpCmpType.FLOW_FORWARDING_REQUEST;
            case FLOWFORWARDING_REPLY_VAL:
                return HCPSbpCmpType.FLOW_FORWARDING_REPLY;
            case PACKET_IN_VAL:
                return HCPSbpCmpType.PACKET_IN;
            case PACKET_OUT_VAL:
                return HCPSbpCmpType.PACKET_OUT;
            case RESOURCE_REQUEST_VAL:
                return HCPSbpCmpType.RESOURCE_REQUEST;
            case RESOURCE_REPLY_VAL:
                return HCPSbpCmpType.RESOURCE_REPLY;
            default:
                throw new IllegalArgumentException("Illegal wire value for type HCPSbpCmpType in HCP protocol version 1.0:"+value);
        }
    }

    public static byte toWireValue(HCPSbpCmpType sbpCmpType){
        switch (sbpCmpType){
            case NORMAL:
                return NORMAL_VAL;
            case FLOW_FORWARDING_REQUEST:
                return FLOWFORWARDING_REQUESR_VAL;
            case FLOW_FORWARDING_REPLY:
                return FLOWFORWARDING_REPLY_VAL;
            case PACKET_IN:
                return PACKET_IN_VAL;
            case PACKET_OUT:
                return PACKET_OUT_VAL;
            case RESOURCE_REQUEST:
                return RESOURCE_REQUEST_VAL;
            case RESOURCE_REPLY:
                return RESOURCE_REPLY_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type HCPSbpCmpType in HCP protocol version 1.0: "+sbpCmpType);
        }
    }
}