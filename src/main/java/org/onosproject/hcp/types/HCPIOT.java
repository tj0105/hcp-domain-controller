package org.onosproject.hcp.types;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.protocol.HCPIoTState;
import org.onosproject.hcp.protocol.HCPIoTType;
import org.onosproject.hcp.protocol.Writeable;
import org.onosproject.hcp.protocol.ver10.HCPIoTStateSerializerVer10;
import org.onosproject.hcp.protocol.ver10.HCPIoTTypeSerializerVer10;

import java.util.Objects;

/**
 * @Author ldy
 * @Date: 2021/1/5 下午2:33
 * @Version 1.0
 */
public class HCPIOT implements Writeable, PrimitiveSinkable {
    private IPv4Address iPv4Address;
    private HCPIoTType ioTType;
    private HCPIOTID hcpiotid;
    private HCPIoTState ioTState;

    private  HCPIOT(IPv4Address iPv4Address, HCPIoTType hcpIoTType,HCPIOTID hcpiotid, HCPIoTState hcpIoTState){
        this.iPv4Address = iPv4Address;
        this.ioTType = hcpIoTType;
        this.hcpiotid = hcpiotid;
        this.ioTState = hcpIoTState;
    }

    public IPv4Address getiPv4Address(){
        return iPv4Address;
    }

    public HCPIoTType getIoTType(){
        return ioTType;
    }

    public HCPIOTID getHcpiotid(){
        return hcpiotid;
    }
    public HCPIoTState getHCPIoTState(){
        return ioTState;
    }

    public static HCPIOT of (IPv4Address iPv4Address, HCPIoTType hcpIoTType, HCPIOTID hcpiotid,HCPIoTState hcpIoTState){
        if (iPv4Address == null){
            throw new NullPointerException("Property ipv4address must not be null");
        }
        if (hcpIoTType == null){
            throw new NullPointerException("Property hcpIoTType must not be null");
        }
        if (hcpiotid == null){
            throw new NullPointerException("Property hcpiotId must not be null");
        }
        return new HCPIOT(iPv4Address,hcpIoTType,hcpiotid,hcpIoTState);
    }
    @Override
    public void writeTo(ChannelBuffer bb) {
        iPv4Address.writeTo(bb);
        HCPIoTTypeSerializerVer10.writeTo(bb,ioTType);
        hcpiotid.writeTo(bb);
        HCPIoTStateSerializerVer10.writeTo(bb,ioTState);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HCPIOT hcpiot = (HCPIOT) o;
        return Objects.equals(iPv4Address, hcpiot.iPv4Address) && ioTType == hcpiot.ioTType && Objects.equals(hcpiotid, hcpiot.hcpiotid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iPv4Address, ioTType, hcpiotid);
    }

    @Override
    public String toString() {
        return "HCPIOT{" +
                "iPv4Address=" + iPv4Address +
                ", ioTType=" + ioTType +
                ", hcpiotid=" + hcpiotid +
                ", hcpiotState =" + ioTState +
                '}';
    }
}
