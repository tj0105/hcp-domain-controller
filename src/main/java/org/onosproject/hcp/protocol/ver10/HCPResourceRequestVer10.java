package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onlab.packet.IpAddress;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPConfigFlags;
import org.onosproject.hcp.protocol.HCPResourceRequest;
import org.onosproject.hcp.types.IPv4Address;
import org.onosproject.hcp.types.U16;
import org.onosproject.hcp.types.U8;

import java.util.Set;

public class HCPResourceRequestVer10 implements HCPResourceRequest{
    private IPv4Address srcIpAddress;
    private IPv4Address dstIpAddress;
    private Set<HCPConfigFlags> flagsSet;

    HCPResourceRequestVer10(IPv4Address src, IPv4Address dst,Set<HCPConfigFlags> flags){
        this.srcIpAddress=src;
        this.dstIpAddress=dst;
        this.flagsSet=flags;
    }

    public static HCPResourceRequestVer10 of(IPv4Address srcIpAddress,IPv4Address dstIpAddress,Set<HCPConfigFlags> flags){
        return new HCPResourceRequestVer10(srcIpAddress,dstIpAddress,flags);
    }

    public static HCPResourceRequest read(ChannelBuffer bb) throws HCPParseError{
        IPv4Address srcIpAddress=IPv4Address.read4Bytes(bb);
        IPv4Address dstIpAddress=IPv4Address.read4Bytes(bb);
        Set<HCPConfigFlags> flags=HCPConfigFlagsSerializerVer10.readFrom(bb);
        return of(srcIpAddress,dstIpAddress,flags);
    }
    @Override
    public IPv4Address getSrcIpAddress() {
        return srcIpAddress;
    }

    @Override
    public IPv4Address getDstIpAddress() {
        return dstIpAddress;
    }

    @Override
    public Set<HCPConfigFlags> getFlags(){
        return flagsSet;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        srcIpAddress.writeTo(bb);
        dstIpAddress.writeTo(bb);
        HCPConfigFlagsSerializerVer10.wirteTo(bb,flagsSet);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        HCPResourceRequestVer10 other = (HCPResourceRequestVer10) obj;
        if (flagsSet == null) {
            if (other.flagsSet != null)
                return false;
        }
        if (srcIpAddress != null ? !srcIpAddress.equals(other.srcIpAddress) : other.srcIpAddress != null)
            return false;
        if (dstIpAddress != null ? !dstIpAddress.equals(other.dstIpAddress) : other.dstIpAddress != null)
            return false;
        return true;

    }
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = result * prime + (srcIpAddress != null ? srcIpAddress.hashCode() : 0);
        result = result * prime + (dstIpAddress != null ? dstIpAddress.hashCode() : 0);
        result = result * prime+flagsSet.hashCode();
        return result;
    }


}
