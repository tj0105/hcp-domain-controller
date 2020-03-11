package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPVportState;
import org.onosproject.hcp.types.HCPVport;

import java.util.EnumSet;
import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-2-19 下午10:34
 * @Version 1.0
 */
public class HCPVportStateSerializerVer10 {
    public final static int LINK_DOWN_VAL=1<<0;
    public final static int BLOCKED_VAL=1<<1;
    public final static int LINK_UP_VAL=1<<2;

    public static Set<HCPVportState> readFrom(ChannelBuffer bb)throws HCPParseError{
        try {
            return ofWireValue(bb.readInt());
        }catch (IllegalArgumentException e){
            throw new HCPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, Set<HCPVportState> set){
        bb.writeInt(toWireValue(set));
    }

    public static void putTo(PrimitiveSink sink,Set<HCPVportState> set){
        sink.putInt(toWireValue(set));
    }

    public static Set<HCPVportState> ofWireValue( int value){
        EnumSet<HCPVportState> set=EnumSet.noneOf(HCPVportState.class);

        if ((value & LINK_DOWN_VAL)!=0)
             set.add(HCPVportState.LINK_DOWN);
        if ((value & BLOCKED_VAL)!=0)
             set.add(HCPVportState.BLOCKED);
        if ((value & LINK_UP_VAL)!=0)
             set.add(HCPVportState.LINK_UP);
        return set;
    }

    public static int toWireValue(Set<HCPVportState> set){
        int wireValue=0;
        for (HCPVportState state:set) {
            switch (state) {
                case LINK_DOWN:
                    wireValue |= LINK_DOWN_VAL;
                    break;
                case BLOCKED:
                    wireValue |= BLOCKED_VAL;
                    break;
                case LINK_UP:
                    wireValue |= LINK_UP_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type HCPVportState in HCP protocol Version 1.0 : " + state);
            }
        }
        return wireValue;
    }
}
