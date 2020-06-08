package org.onosproject.hcp.types;

import com.google.common.hash.PrimitiveSink;
import org.apache.commons.lang.ObjectUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.protocol.HCPHostState;
import org.onosproject.hcp.protocol.Writeable;
import org.onosproject.hcp.protocol.ver10.HCPHostStateSerializerVer10;

/**
 * @Author ldy
 * @Date: 20-2-14 下午7:47
 * @Version 1.0
 */
public class HCPHost implements Writeable,PrimitiveSinkable {
    private  IPv4Address iPv4Address;
    private  MacAddress macAddress;
    private  HCPHostState hostState;

    private HCPHost(IPv4Address iPv4Address,MacAddress macAddress,HCPHostState hcpHostState){
        this.iPv4Address=iPv4Address;
        this.macAddress=macAddress;
        this.hostState=hcpHostState;
    }

    public IPv4Address getiPv4Address() {
        return iPv4Address;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public HCPHostState getHostState() {
        return hostState;
    }

    public static HCPHost of(IPv4Address iPv4Address,MacAddress macAddress,HCPHostState hostState){
        if (iPv4Address==null)
            throw new NullPointerException("Property ipv4address must not be null");
        if (macAddress==null)
            throw new NullPointerException("Property Macaddress must not be null");
        if (hostState==null)
            throw new NullPointerException("Property hoststate must not be null");
        return new HCPHost(iPv4Address,macAddress,hostState);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        iPv4Address.writeTo(bb);
        macAddress.write6Bytes(bb);
        HCPHostStateSerializerVer10.writeTo(bb,hostState);

    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public int hashCode() {
        int prime=31;
        int result=1;
        result=result*prime+((iPv4Address==null)?0:iPv4Address.hashCode());
        result=result*prime+((macAddress==null)?0:macAddress.hashCode());
        result=result*prime+((hostState==null)?0:hostState.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null)
            return false;
        if (getClass()!=obj.getClass())
            return false;
        HCPHost other=(HCPHost) obj;
        if (this.iPv4Address!=null){
            if (other.iPv4Address==null)
                return false;
        }else if(!this.iPv4Address.equals(other.iPv4Address))
            return false;
        if (this.macAddress!=null){
            if (other.macAddress==null)
                return false;
        }else if(!this.macAddress.equals(other.macAddress))
            return false;
        if (this.hostState!=null){
            if (other.hostState==null)
                return false;
        }else if (!this.hostState.equals(other.hostState))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "HCPHost{" +
                "iPv4Address=" + iPv4Address +
                ", macAddress=" + macAddress +
                ", hostState=" + hostState +
                '}';
    }
}
