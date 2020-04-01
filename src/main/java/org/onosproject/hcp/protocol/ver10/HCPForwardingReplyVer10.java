package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.protocol.HCPForwardingReply;
import org.onosproject.hcp.types.*;

/**
 * @Author ldy
 * @Date: 20-2-22 下午9:18
 * @Version 1.0
 */
public class HCPForwardingReplyVer10 implements HCPForwardingReply{

    private IPv4Address srcIpAddress;
    private IPv4Address dstIpAddress;
    private HCPVport srcVport;
    private HCPVport dstVport;
    private short ethType;
    private byte qos;


    private HCPForwardingReplyVer10(IPv4Address srcIpAddress,IPv4Address dstIpAddress,HCPVport srcVport,HCPVport dstVport
                                    ,short ethType, byte qos){
        this.srcIpAddress=srcIpAddress;
        this.dstIpAddress=dstIpAddress;
        this.srcVport=srcVport;
        this.dstVport=dstVport;
        this.ethType=ethType;
        this.qos=qos;
    }

    public static HCPForwardingReplyVer10 of(IPv4Address srcIpAddress,IPv4Address dstIpAddress,HCPVport srcVport,HCPVport dstVport
                                            ,short ethType, byte qos){
        return new HCPForwardingReplyVer10(srcIpAddress,dstIpAddress,srcVport,dstVport,ethType,qos);
    }

    public static HCPForwardingReply read(ChannelBuffer bb){
        IPv4Address srcIpAddress=IPv4Address.read4Bytes(bb);
        IPv4Address dstIpAddress=IPv4Address.read4Bytes(bb);
        HCPVport srcVport=HCPVport.readFrom(bb);
        HCPVport dstVport=HCPVport.readFrom(bb);
        short ethType=bb.readByte();
        byte qos=bb.readByte();
        return of(srcIpAddress,dstIpAddress,srcVport,dstVport,ethType,qos);
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
    public HCPVport getSrcVport() {
        return srcVport;
    }

    @Override
    public HCPVport getDstVport() {
        return dstVport;
    }

    @Override
    public short getEthType() {
        return ethType;
    }

    @Override
    public byte getQos() {
        return qos;
    }

    @Override
    public DomainId getDomainId() {
        return null;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        srcIpAddress.writeTo(bb);
        dstIpAddress.writeTo(bb);
        srcVport.writeTo(bb);
        dstVport.writeTo(bb);
        bb.writeShort(ethType);
        bb.writeByte(qos);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if (obj==null || getClass()!=obj.getClass())return false;
        HCPForwardingReplyVer10 other=(HCPForwardingReplyVer10) obj;
        if (this.srcVport!=other.srcVport)
            return false ;
        if (this.dstVport!=other.dstVport)
            return false ;
        if (this.ethType!=other.ethType)
            return false ;
        if (this.qos!=other.qos)
            return false ;
        if (srcIpAddress != null ? !srcIpAddress.equals(other.srcIpAddress) : other.srcIpAddress != null) return false;
        if (dstIpAddress != null ? !dstIpAddress.equals(other.dstIpAddress) : other.dstIpAddress != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result=1;
        final int prime=31;
        result=result*prime+(srcIpAddress != null ? srcIpAddress.hashCode() : 0);
        result=result*prime+(dstIpAddress != null ? dstIpAddress.hashCode() : 0);
        result=result*prime+srcVport.hashCode();
        result=result*prime+dstVport.hashCode();
        result=result*prime+ U16.f(ethType);
        result=result*prime+ U8.f(qos);
        return result;
    }
}
