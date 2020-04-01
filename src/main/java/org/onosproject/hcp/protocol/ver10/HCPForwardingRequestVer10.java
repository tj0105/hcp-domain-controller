package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.apache.maven.lifecycle.internal.LifecycleStarter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.ipfilter.IpFilteringHandler;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPForwardingRequest;
import org.onosproject.hcp.types.*;
import org.onosproject.hcp.util.ChannelUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-26 下午4:18
 * @Version 1.0
 */
public class HCPForwardingRequestVer10 implements HCPForwardingRequest{

   private IPv4Address srcIpAddress;
   private IPv4Address dstIpAddress;
   private int inPort;
   private short ethType;
   private byte qos;
   private List<HCPVportHop> vportHopList;

   private HCPForwardingRequestVer10(IPv4Address srcIpAddress,IPv4Address dstIpAddress,
                                     int port,short ethType,byte qos,List<HCPVportHop> vportHops){
       this.srcIpAddress=srcIpAddress;
       this.dstIpAddress=dstIpAddress;
       this.inPort=port;
       this.ethType=ethType;
       this.qos=qos;
       this.vportHopList=vportHops;
   }

   public static HCPForwardingRequestVer10 of(IPv4Address srcIpAddress,IPv4Address dstIpAddress
                                        ,int port,short ethType,byte qos,List<HCPVportHop> vportHops){
       return new HCPForwardingRequestVer10(srcIpAddress,
                                            dstIpAddress,
                                            port,
                                            ethType,
                                            qos,
                                            vportHops);
   }

   public static HCPForwardingRequest read(ChannelBuffer bb,int dataLength) throws HCPParseError {
       IPv4Address srcIpAddress=IPv4Address.read4Bytes(bb);
       IPv4Address dstIpAddress=IPv4Address.read4Bytes(bb);
       int inPort=bb.readInt();
       short ethType=bb.readShort();
       byte qos=bb.readByte();
       List<HCPVportHop> vportHops=ChannelUtils.readList(bb,dataLength,HCPVportHopVer10.READER);
       return of(srcIpAddress,dstIpAddress,
                 inPort,ethType,qos,vportHops);
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
        srcIpAddress.writeTo(bb);
        dstIpAddress.writeTo(bb);
        bb.writeInt(inPort);
        bb.writeShort(ethType);
        bb.writeByte(qos);
        ChannelUtils.writeList(bb,vportHopList);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj) return true;
        if (obj==null||getClass()!=obj.getClass())
            return false;
        HCPForwardingRequestVer10 other=(HCPForwardingRequestVer10) obj;
        if (inPort != other.inPort) return false;
        if (ethType != other.ethType) return false;
        if (qos != other.qos) return false;
        if (srcIpAddress != null ? !srcIpAddress.equals(other.srcIpAddress) : other.srcIpAddress != null) return false;
        if (dstIpAddress != null ? !dstIpAddress.equals(other.dstIpAddress) : other.dstIpAddress != null) return false;
        if (this.vportHopList!=other.vportHopList)
            return false;
        return true;
   }

    @Override
    public int hashCode() {
       final int prime = 31;
       int result=1;
       result=result*prime+(srcIpAddress!=null?srcIpAddress.hashCode():0);
       result=result*prime+(dstIpAddress!=null?dstIpAddress.hashCode():0);
       result=result*prime+inPort;
       result=result*prime+ U16.f(ethType);
       result=result*prime+ U8.f(qos);
       result=result*prime+ vportHopList.hashCode();
       return result;
    }

    @Override
    public String toString() {
        return "HCPForwardingRequestVer10{" +
                "srcIpAddress=" + srcIpAddress.toString() +
                ", dstIpAddress=" + dstIpAddress.toString()+
                ", inPort=" + inPort +
                ", ethType=" + ethType +
                ", qos=" + qos +
                ", vportHopList=" + vportHopList.toString() +
                '}';
    }
}
