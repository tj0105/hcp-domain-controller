package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.protocol.HCPPacketIn;
import org.onosproject.hcp.protocol.HCPPacketOut;

import java.util.Arrays;

/**
 * @Author ldy
 * @Date: 20-2-23 下午9:47
 * @Version 1.0
 */
public class HCPPacketInVer10 implements HCPPacketIn {

    private int InPortNumber;
    private byte[] data;

    private HCPPacketInVer10(int InPortNumber, byte[]data){
        this.InPortNumber=InPortNumber;
        this.data=data;
    }

    public static HCPPacketIn of(int InPortNumber, byte[]data){
        return new HCPPacketInVer10(InPortNumber,data);
    }

    public static HCPPacketIn read(ChannelBuffer bb,int length){
        int inPort=bb.readInt();
        byte data[]=new byte[length];
        bb.readBytes(data);
        return of(inPort,data);
    }
    @Override
    public long getInport() {
        return InPortNumber;
    }

    @Override
    public byte[] getData() {
        return Arrays.copyOf(data,data.length);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeInt(InPortNumber);
        bb.writeBytes(data);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null)
            return false;
        if (getClass()!=obj.getClass())
            return false;
        HCPPacketInVer10 other=(HCPPacketInVer10)obj;
        if (this.InPortNumber!=other.InPortNumber)
            return false;
        if (!Arrays.equals(data,other.data))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int prime=31;
        int result=1;
        result=result*prime+InPortNumber;
        result=result*prime+Arrays.hashCode(data);
        return result;
    }
}
