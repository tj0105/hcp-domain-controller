package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPIoTType;
import org.onosproject.hcp.protocol.HCPSbpCmpType;

/**
 * @Author ldy
 * @Date: 2021/1/5 上午11:03
 * @Version 1.0
 */
public class HCPIoTTypeSerializerVer10 {
    public final static byte IOT_EPC_VAL = 0x1e;
    public final static byte IOT_ECODE_VAL = 0x1f;
    public final static byte IOT_OID_VAL = 0x20;

    public static HCPIoTType readFrom(ChannelBuffer bb) throws HCPParseError {
        try{
            return ofWireValue(bb.readByte());
        }catch(IllegalArgumentException e){
            throw new HCPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, HCPIoTType ioTType) {
        bb.writeByte(toWireValue(ioTType));
    }

    public static void putTo(PrimitiveSink sink, HCPIoTType ioTType){
        sink.putByte(toWireValue(ioTType));
    }

    public static HCPIoTType ofWireValue(byte value){
        switch (value){
            case IOT_EPC_VAL:
                return HCPIoTType.IOT_EPC;
            case IOT_ECODE_VAL:
                return HCPIoTType.IOT_ECODE;
            case IOT_OID_VAL:
                return HCPIoTType.IOT_IOD;
            default:
                throw new IllegalArgumentException("Illegal wire value for type HCPIoTType in HCP protocol version:" + value);
        }
    }

    public static byte toWireValue(HCPIoTType hcpIoTType){
        switch (hcpIoTType){
            case IOT_EPC:
                return IOT_EPC_VAL;
            case IOT_ECODE:
                return IOT_ECODE_VAL;
            case IOT_IOD:
                return IOT_OID_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type HCPIoTType in HCP protocol version:" + hcpIoTType);
        }
    }
}
