package org.onosproject.hcp.types;

import com.google.common.hash.PrimitiveSink;
import com.google.common.primitives.UnsignedInts;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.protocol.Writeable;

/**
 * @Author ldy
 * @Date: 20-2-15 下午7:51
 * @Version 1.0
 */
public class HCPVport implements HCPValueType<HCPVport> ,Writeable{
    static final short LENGTH=2;

    private static final short HCP_MAX_SHORT = (short) 0xf00;
    private static final short HCP_IN_PORT_SHORT = (short) 0xff8;
    private static final short HCP_OUT_PORT_SHORT = (short) 0xff9;
    private static final short HCP_FLOOD_SHORT = (short) 0xffb;
    private static final short HCP_ALL_SHORT = (short) 0xffc;
    private static final short HCP_CONTROLLER_SHORT = (short) 0xffd;
    private static final short HCP_LOCAL_SHORT = (short) 0xffe;
    private static final short HCP_NONE_SHORT = (short) 0xfff;

    private final short portNumber;

    public static final HCPVport MAX = new NamedVport(HCP_MAX_SHORT, "max");
    public static final HCPVport IN_PORT = new NamedVport(HCP_IN_PORT_SHORT, "in_port");
    public static final HCPVport OUT_PORT = new NamedVport(HCP_OUT_PORT_SHORT, "out_port");
    public static final HCPVport FLOOD = new NamedVport(HCP_FLOOD_SHORT, "flood");
    public static final HCPVport ALL = new NamedVport(HCP_ALL_SHORT, "all");
    public static final HCPVport CONTROLLER = new NamedVport(HCP_CONTROLLER_SHORT, "controller");
    public static final HCPVport LOCAL = new NamedVport(HCP_LOCAL_SHORT, "local");
    public static final HCPVport NONE = new NamedVport(HCP_NONE_SHORT, "noone");
    private HCPVport(short portNumber){
        this.portNumber=portNumber;
    }

    public static HCPVport ofShort(short portNumber){
        switch (portNumber) {
            case HCP_MAX_SHORT:
                return MAX;
            case HCP_IN_PORT_SHORT:
                return IN_PORT;
            case HCP_OUT_PORT_SHORT:
                return OUT_PORT;
            case HCP_FLOOD_SHORT:
                return FLOOD;
            case HCP_ALL_SHORT:
                return ALL;
            case HCP_CONTROLLER_SHORT:
                return CONTROLLER;
            case HCP_LOCAL_SHORT:
                return LOCAL;
            case HCP_NONE_SHORT:
                return NONE;
            default:
                if (portNumber < 0 || portNumber > HCP_MAX_SHORT)
                    throw new  IllegalArgumentException("Unknown special port number: "
                            + portNumber);
                return new HCPVport(portNumber);
        }
    }

    public static HCPVport readFrom(ChannelBuffer bb) {
        return HCPVport.ofShort(bb.readShort());
    }

    static class NamedVport extends HCPVport {
        private final String name;
        NamedVport(final short portNo, final String name) {
            super(portNo);
            this.name = name;
        }
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public short getPortNumber(){
        return portNumber;
    }
    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public HCPVport applyMask(HCPVport mask) {
        return null;
    }

    @Override
    public int compareTo(HCPVport o) {
        return UnsignedInts.compare(this.portNumber,o.portNumber);
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        sink.putShort((byte)portNumber);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeShort(portNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null)
            return false;
        if (getClass()!= obj.getClass())
            return false;
        HCPVport hcpVport=(HCPVport) obj;
        if (this.portNumber!=hcpVport.portNumber)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime=31;
        int result=1;
        return prime*result+portNumber;

    }

    @Override
    public String toString() {
        return "portNumber=" + portNumber;
    }
}
