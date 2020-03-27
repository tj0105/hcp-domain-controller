package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPConfigFlags;

import java.util.EnumSet;
import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-2-19 下午1:36
 * @Version 1.0
 */
public class HCPConfigFlagsSerializerVer10 {
    public final static byte MODE_ADVANCED_VAL=1<<0;
    public final static byte CAPABILITIES_BW_VAL=1<<1;
    public final static byte CAPABILITIES_DELAY_VAL=1<<2;
    public final static byte CAPABILITIES_HOP_VAL=1<<3;
    public final static byte MODE_COMPRESSED_VAL=1<<4;
    public final static byte MODE_TRUEST_VAL=1<<5;

    public static Set<HCPConfigFlags> readFrom(ChannelBuffer bb) throws HCPParseError{
        return ofWireValue(bb.readByte());
    }

    public static void wirteTo(ChannelBuffer bb,Set<HCPConfigFlags> set){
        bb.writeByte(toWireValue(set));
    }

    public static void putTo(PrimitiveSink sink, Set<HCPConfigFlags> set){
        sink.putByte(toWireValue(set));
    }

    public static final Set<HCPConfigFlags> ofWireValue(byte value){
        EnumSet<HCPConfigFlags> flagsEnumSet=EnumSet.noneOf(HCPConfigFlags.class);

        if ((value& MODE_ADVANCED_VAL)!=0){
            flagsEnumSet.add(HCPConfigFlags.MODE_ADVANCED);
        }
        if((value& CAPABILITIES_BW_VAL)!=0){
            flagsEnumSet.add(HCPConfigFlags.CAPABILITIES_BW);
        }
        if ((value&CAPABILITIES_DELAY_VAL)!=0){
            flagsEnumSet.add(HCPConfigFlags.CAPABILITIES_DELAY);
        }
        if ((value&CAPABILITIES_HOP_VAL)!=0){
                flagsEnumSet.add(HCPConfigFlags.CAPABILITIES_HOP);
        }
        if ((value&MODE_COMPRESSED_VAL)!=0){
            flagsEnumSet.add(HCPConfigFlags.MODE_COMPRESSED);
        }
        if ((value&MODE_TRUEST_VAL)!=0){
            flagsEnumSet.add(HCPConfigFlags.MODE_TRUST);
        }

        return flagsEnumSet;
    }

    public static final byte toWireValue(Set<HCPConfigFlags> set){
        byte wireValue=0;
        for (HCPConfigFlags flags: set
             ) {
            switch (flags){
                case MODE_ADVANCED:
                    wireValue |= MODE_ADVANCED_VAL;
                    break;
                case CAPABILITIES_BW:
                    wireValue |= CAPABILITIES_BW_VAL;
                    break;
                case CAPABILITIES_DELAY:
                    wireValue |= CAPABILITIES_DELAY_VAL;
                    break;
                case CAPABILITIES_HOP:
                    wireValue |= CAPABILITIES_HOP_VAL;
                    break;
                case MODE_COMPRESSED:
                    wireValue |=MODE_COMPRESSED_VAL;
                    break;
                case MODE_TRUST:
                    wireValue |= MODE_TRUEST_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enums value for type HCPConfigFlags in Version 1.0: "+flags);
            }

        }
        return wireValue;
    }



}
