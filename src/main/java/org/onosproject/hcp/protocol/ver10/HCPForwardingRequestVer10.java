package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.apache.maven.lifecycle.internal.LifecycleStarter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.ipfilter.IpFilteringHandler;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPFlowType;
import org.onosproject.hcp.protocol.HCPForwardingRequest;
import org.onosproject.hcp.types.*;
import org.onosproject.hcp.util.ChannelUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Author ldy
 * @Date: 20-2-26 下午4:18
 * @Version 1.0
 */
public class HCPForwardingRequestVer10 implements HCPForwardingRequest{

   private HCPFlowType hcpFlowType;
   private HCPIOT srcIoT;
   private HCPIOT dstIoT;
   private IPv4Address srcIpAddress;
   private IPv4Address dstIpAddress;
   private int inPort;
   private short ethType;
   private byte qos;
   private List<HCPVportHop> vportHopList;

   private HCPForwardingRequestVer10(HCPFlowType hcpFlowType,IPv4Address srcIpAddress,IPv4Address dstIpAddress,
                                     int port,short ethType,byte qos,List<HCPVportHop> vportHops){
       this.hcpFlowType = hcpFlowType;
       this.srcIpAddress=srcIpAddress;
       this.dstIpAddress=dstIpAddress;
       this.inPort=port;
       this.ethType=ethType;
       this.qos=qos;
       this.vportHopList=vportHops;
   }
    private HCPForwardingRequestVer10(HCPFlowType hcpFlowType,HCPIOT srcIoT,HCPIOT dstIoT,
                                      int port,short ethType,byte qos,List<HCPVportHop> vportHops){
        this.hcpFlowType = hcpFlowType;
        this.srcIoT = srcIoT;
        this.dstIoT = dstIoT;
        this.inPort=port;
        this.ethType=ethType;
        this.qos=qos;
        this.vportHopList=vportHops;
    }
   public static HCPForwardingRequestVer10 of(HCPFlowType hcpFlowType,IPv4Address srcIpAddress,IPv4Address dstIpAddress
                                        ,int port,short ethType,byte qos,List<HCPVportHop> vportHops){
       return new HCPForwardingRequestVer10(hcpFlowType,srcIpAddress, dstIpAddress, port, ethType, qos, vportHops);
   }
    public static HCPForwardingRequestVer10 of(HCPFlowType hcpFlowType,HCPIOT srcIoT,HCPIOT dstIoT
            ,int port,short ethType,byte qos,List<HCPVportHop> vportHops){
        return new HCPForwardingRequestVer10(hcpFlowType,srcIoT, dstIoT, port, ethType, qos, vportHops);
    }

   public static HCPForwardingRequest read(ChannelBuffer bb,int dataLength) throws HCPParseError {
       HCPFlowType hcpFlowType = HCPFlowTypeSerializerVer10.readFrom(bb);
       if (hcpFlowType.equals(HCPFlowType.HCP_HOST)){
           IPv4Address srcIpAddress=IPv4Address.read4Bytes(bb);
           IPv4Address dstIpAddress=IPv4Address.read4Bytes(bb);
           int inPort=bb.readInt();
           short ethType=bb.readShort();
           byte qos=bb.readByte();
           List<HCPVportHop> vportHops=ChannelUtils.readList(bb,dataLength,HCPVportHopVer10.READER);
           return of(hcpFlowType,srcIpAddress,dstIpAddress,
                   inPort,ethType,qos,vportHops);
       }else{
           HCPIOT srcIoT = HCPIoTVer10.READER.readFrom(bb);
           HCPIOT dstIoT = HCPIoTVer10.READER.readFrom(bb);
           int inPort=bb.readInt();
           short ethType=bb.readShort();
           byte qos=bb.readByte();
           List<HCPVportHop> vportHops=ChannelUtils.readList(bb,dataLength,HCPVportHopVer10.READER);
           return of(hcpFlowType,srcIoT,dstIoT,inPort,ethType,qos,vportHops);
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
    public int getInport() {
        return inPort;
    }

    @Override
    public IPv4Address getMask() {
        return null;
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
    public List<HCPVportHop> getvportHopList() {
        return vportHopList;
    }

    @Override
    public byte[] getData() {

        ChannelBuffer buffer= ChannelBuffers.dynamicBuffer();
        ChannelUtils.writeList(buffer,vportHopList);
        byte [] data=new byte[buffer.readableBytes()];
        return  data;
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
        bb.writeInt(inPort);
        bb.writeShort(ethType);
        bb.writeByte(qos);
        ChannelUtils.writeList(bb,vportHopList);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HCPForwardingRequestVer10 that = (HCPForwardingRequestVer10) o;
        return inPort == that.inPort && ethType == that.ethType && qos == that.qos && hcpFlowType == that.hcpFlowType && Objects.equals(srcIoT, that.srcIoT)
                && Objects.equals(dstIoT, that.dstIoT) && Objects.equals(srcIpAddress, that.srcIpAddress) &&
                Objects.equals(dstIpAddress, that.dstIpAddress) && Objects.equals(vportHopList, that.vportHopList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hcpFlowType, srcIoT, dstIoT, srcIpAddress, dstIpAddress, inPort, ethType, qos, vportHopList);
    }

    @Override
    public String toString() {
            return "HCPForwardingRequestVer10{" +
                    "flowType=" + hcpFlowType +
                    ", srcIpAddress=" + srcIpAddress +
                    ", dstIpAddress=" + dstIpAddress +
                    ", srcIoT=" + srcIoT+
                    ", dstIoT=" + dstIoT+
                    ", inPort=" + inPort +
                    ", ethType=" + ethType +
                    ", qos=" + qos +
                    ", vportHopList=" + vportHopList.toString() +
                    '}';


    }
}
