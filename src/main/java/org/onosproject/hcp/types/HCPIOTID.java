package org.onosproject.hcp.types;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.protocol.Writeable;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author ldy
 * @Date: 2021/1/5 下午2:37
 * @Version 1.0
 */
public class HCPIOTID implements Writeable,PrimitiveSinkable {
    private short length ;
    private byte[] IotID;

    public static final HCPIOTID DEFAULT = HCPIOTID.of("000");
    private HCPIOTID(short length, byte[] IotID){
        this.length = length;
        this.IotID = IotID;
    }

    public static HCPIOTID of(short length, byte [] data){
        return new HCPIOTID(length,data);
    }
    public static HCPIOTID of(String iotID){
        short length = (short) iotID.length();
        byte [] data = iotID.getBytes();
        return new HCPIOTID(length,data);
    }
    public static HCPIOTID read(ChannelBuffer bb){
        short length = bb.readShort();
        byte data[] = new byte[length];
        bb.readBytes(data);
        return of(length,data);
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public byte[] getIotID() {
        return Arrays.copyOf(IotID,length);
    }

    public void setIotID(byte[] iotID) {
        IotID = iotID;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeShort(length);
        bb.writeBytes(IotID);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HCPIOTID hcpiotid = (HCPIOTID) o;
        return length == hcpiotid.length && Arrays.equals(IotID, hcpiotid.IotID);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(length);
        result = 31 * result + Arrays.hashCode(IotID);
        return result;
    }

    @Override
    public String toString() {
        return "IoT_ID={" + new String(getIotID()) +
                '}';
    }
}
