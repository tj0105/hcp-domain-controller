package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPResourceReply;
import org.onosproject.hcp.protocol.HCPResourceRequest;
import org.onosproject.hcp.types.HCPVportHop;
import org.onosproject.hcp.types.IPv4Address;
import org.onosproject.hcp.util.ChannelUtils;

import java.util.List;

public class HCPResourceReplyVer10 implements HCPResourceReply {
    private IPv4Address srcIpAddress;
    private IPv4Address dstIpAddress;
    private List<HCPVportHop> vportlist;

    HCPResourceReplyVer10(IPv4Address srcIpAddress,IPv4Address dstIpAddress,List<HCPVportHop> list){
        this.srcIpAddress=srcIpAddress;
        this.dstIpAddress=dstIpAddress;
        this.vportlist=list;
    }

    public static HCPResourceReplyVer10 of(IPv4Address srcIpAddress,IPv4Address dstIpAddress,List<HCPVportHop> list){
        return new HCPResourceReplyVer10(srcIpAddress,dstIpAddress,list);
    }

    public static HCPResourceReply read(ChannelBuffer bb,int dataLength) throws HCPParseError{
        IPv4Address srcIpAddress=IPv4Address.read4Bytes(bb);
        IPv4Address dstIpAddress=IPv4Address.read4Bytes(bb);
        List<HCPVportHop> vportHops=ChannelUtils.readList(bb,dataLength,HCPVportHopVer10.READER);
        return of(srcIpAddress,dstIpAddress,vportHops);
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
    public List<HCPVportHop> getvportHopList() {
        return vportlist;
    }

    @Override
    public byte[] getData() {
        ChannelBuffer buffer= ChannelBuffers.dynamicBuffer();
        ChannelUtils.writeList(buffer,vportlist);
        byte [] data=new byte[buffer.readableBytes()];
        return  data;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        srcIpAddress.writeTo(bb);
        dstIpAddress.writeTo(bb);
        ChannelUtils.writeList(bb,vportlist);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        HCPResourceReplyVer10 other = (HCPResourceReplyVer10) obj;
        if (vportlist == null) {
            if (other.vportlist!= null)
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
        result = result * prime+vportlist.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "HCPResourceReplyVer10{" +
                "srcIpAddress=" + srcIpAddress +
                ", dstIpAddress=" + dstIpAddress +
                ", vportlist=" + vportlist +
                '}';
    }
}
