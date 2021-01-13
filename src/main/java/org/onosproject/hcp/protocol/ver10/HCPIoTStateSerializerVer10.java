package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPIoTState;

/**
 * @Author ldy
 * @Date: 2021/1/6 下午4:17
 * @Version 1.0
 */
public class HCPIoTStateSerializerVer10 {
    public final static byte ACTIVE_VAL=0x30;
    public final static byte INACTIVE_VAL=0x31;

    public static HCPIoTState readFrom(ChannelBuffer bb) throws HCPParseError {
        try{
            return ofWireValue(bb.readByte());
        }catch (IllegalArgumentException e){
            throw new HCPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb,HCPIoTState state){
        bb.writeByte(toWireValue(state));
    }

    public static void putTo(PrimitiveSink sink, HCPIoTState state){
        sink.putByte(toWireValue(state));
    }

    public static HCPIoTState ofWireValue(byte value){
        switch (value){
            case ACTIVE_VAL:
                return  HCPIoTState.ACTIVE;
            case INACTIVE_VAL:
                return HCPIoTState.INACTIVE;
            default:
                throw new IllegalArgumentException("Illegal enum value for HCPHostState in HCP protocol Version 1.0 :"+value);
        }
    }

    public static byte toWireValue(HCPIoTState state){
        if (state==null){
            throw new NullPointerException("HCPHostState must cannot be null");
        }
        switch (state){
            case ACTIVE:
                return ACTIVE_VAL;
            case INACTIVE:
                return INACTIVE_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type HCPHostState in HCP protocol Version 1.0: "+state);
        }
    }
}
