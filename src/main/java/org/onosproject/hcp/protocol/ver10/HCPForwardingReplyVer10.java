package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPFlowType;
import org.onosproject.hcp.protocol.HCPForwardingReply;
import org.onosproject.hcp.types.*;

import java.util.Objects;

/**
 * @Author ldy
 * @Date: 20-2-22 下午9:18
 * @Version 1.0
 */
public class HCPForwardingReplyVer10 implements HCPForwardingReply{

    private HCPFlowType hcpFlowType;
    private HCPIOT srcIoT;
    private HCPIOT dstIoT;
    private IPv4Address srcIpAddress;
    private IPv4Address dstIpAddress;
    private HCPVport srcVport;
    private HCPVport dstVport;
    private short ethType;
    private byte qos;


    private HCPForwardingReplyVer10(HCPFlowType flowType,IPv4Address srcIpAddress,IPv4Address dstIpAddress,HCPVport srcVport,HCPVport dstVport
                                    ,short ethType, byte qos){
        this.hcpFlowType = flowType;
        this.srcIpAddress=srcIpAddress;
        this.dstIpAddress=dstIpAddress;
        this.srcVport=srcVport;
        this.dstVport=dstVport;
        this.ethType=ethType;
        this.qos=qos;
    }
    private HCPForwardingReplyVer10(HCPFlowType flowType,HCPIOT srcIoT,HCPIOT dstIoT,HCPVport srcVport,HCPVport dstVport
            ,short ethType, byte qos){
        this.hcpFlowType = flowType;
        this.srcIoT = srcIoT;
        this.dstIoT = dstIoT;
        this.srcVport=srcVport;
        this.dstVport=dstVport;
        this.ethType=ethType;
        this.qos=qos;
    }

    public static HCPForwardingReplyVer10 of(HCPFlowType hcpFlowType,IPv4Address srcIpAddress,IPv4Address dstIpAddress,HCPVport srcVport,HCPVport dstVport
                                            ,short ethType, byte qos){
        return new HCPForwardingReplyVer10(hcpFlowType,srcIpAddress,dstIpAddress,srcVport,dstVport,ethType,qos);
    }
    public static HCPForwardingReplyVer10 of(HCPFlowType hcpFlowType,HCPIOT srcIoT,HCPIOT dstIoT,HCPVport srcVport,HCPVport dstVport
            ,short ethType, byte qos){
        return new HCPForwardingReplyVer10(hcpFlowType,srcIoT,dstIoT,srcVport,dstVport,ethType,qos);
    }
    public static HCPForwardingReply read(ChannelBuffer bb) throws HCPParseError {
        HCPFlowType flowType = HCPFlowTypeSerializerVer10.readFrom(bb);
        if (flowType.equals(HCPFlowType.HCP_HOST)){
            IPv4Address srcIpAddress=IPv4Address.read4Bytes(bb);
            IPv4Address dstIpAddress=IPv4Address.read4Bytes(bb);
            HCPVport srcVport=HCPVport.readFrom(bb);
            HCPVport dstVport=HCPVport.readFrom(bb);
            short ethType=bb.readByte();
            byte qos=bb.readByte();
            return of(flowType,srcIpAddress,dstIpAddress,srcVport,dstVport,ethType,qos);
        }else{
            HCPIOT srcIoT = HCPIoTVer10.READER.readFrom(bb);
            HCPIOT dstIoT = HCPIoTVer10.READER.readFrom(bb);
            HCPVport srcVport=HCPVport.readFrom(bb);
            HCPVport dstVport=HCPVport.readFrom(bb);
            short ethType=bb.readByte();
            byte qos=bb.readByte();
            return of(flowType,srcIoT,dstIoT,srcVport,dstVport,ethType,qos);
        }

    }

    @Override
    public HCPFlowType getFLowType() {
        return hcpFlowType;
    }

    @Override
    public HCPIOT getSrcIoT() {
        return srcIoT;
    }

    @Override
    public HCPIOT getDstIoT() {
        return dstIoT;
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
        HCPFlowTypeSerializerVer10.writeTo(bb,hcpFlowType);
        if (hcpFlowType.equals(HCPFlowType.HCP_HOST)){
            srcIpAddress.writeTo(bb);
            dstIpAddress.writeTo(bb);
        }
        else{
            srcIoT.writeTo(bb);
            dstIoT.writeTo(bb);
        }
        srcVport.writeTo(bb);
        dstVport.writeTo(bb);
        bb.writeShort(ethType);
        bb.writeByte(qos);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HCPForwardingReplyVer10 that = (HCPForwardingReplyVer10) o;
        return ethType == that.ethType && qos == that.qos && hcpFlowType == that.hcpFlowType &&
                Objects.equals(srcIoT, that.srcIoT) && Objects.equals(dstIoT, that.dstIoT) &&
                Objects.equals(srcIpAddress, that.srcIpAddress) && Objects.equals(dstIpAddress, that.dstIpAddress) &&
                Objects.equals(srcVport, that.srcVport) && Objects.equals(dstVport, that.dstVport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hcpFlowType, srcIoT, dstIoT, srcIpAddress, dstIpAddress, srcVport, dstVport, ethType, qos);
    }

    @Override
    public String toString() {
        return "HCPForwardingReplyVer10{" +
                "hcpFlowType=" + hcpFlowType +
                ", srcIoT=" + srcIoT +
                ", dstIoT=" + dstIoT +
                ", srcIpAddress=" + srcIpAddress +
                ", dstIpAddress=" + dstIpAddress +
                ", srcVport=" + srcVport +
                ", dstVport=" + dstVport +
                ", ethType=" + ethType +
                ", qos=" + qos +
                '}';
    }
}
