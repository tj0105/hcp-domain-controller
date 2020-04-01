package org.onosproject.hcp.types;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.omg.PortableInterceptor.INACTIVE;
import org.onosproject.hcp.protocol.Writeable;


import org.onosproject.net.meter.Band;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author ldy
 * @Date: 20-2-15 下午8:34
 * @Version 1.0
 */
public class HCPInternalLink implements Writeable,PrimitiveSinkable {
    private final static Logger logger= LoggerFactory.getLogger(HCPInternalLink.class);


    private final HCPVport srcVPort;
    private final HCPVport dstVport;
    private final long BandwidthCapability;
    private final int DelayCapability;
    private final int HopCapability;

    HCPInternalLink(HCPVport srcVPort,HCPVport dstVport,long capability,int delayCapability,int hopCapability){
        this.srcVPort=srcVPort;
        this.dstVport=dstVport;
        this.BandwidthCapability=capability;
        this.DelayCapability=delayCapability;
        this.HopCapability=hopCapability;
    }

    public HCPVport getDstVport() {
        return dstVport;
    }

    public HCPVport getSrcVPort() {
        return srcVPort;
    }

    public long getBandwidthCapability() {
        return BandwidthCapability;
    }
    public int getDelayCapability(){
        return DelayCapability;
    }
    public int getHopCapability(){
        return HopCapability;
    }

    public static HCPInternalLink of(HCPVport srcVPort,HCPVport dstVport,long bandwidthcapability,int delayCapability,int hopCapability){
        return new HCPInternalLink(srcVPort,dstVport,bandwidthcapability,delayCapability,hopCapability);
    }

    public static HCPInternalLink of(HCPVport srcVPort,HCPVport dstVport,long bandwidthcapability){
        return new HCPInternalLink(srcVPort,dstVport,bandwidthcapability,0,0);
    }

    public static HCPInternalLink of(HCPVport srcVPort,HCPVport dstVport,long bandwidthcapability,int hopCapability){
        return new HCPInternalLink(srcVPort,dstVport,bandwidthcapability,0,hopCapability);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        srcVPort.writeTo(bb);
        dstVport.writeTo(bb);
        bb.writeLong(BandwidthCapability);
        bb.writeInt(DelayCapability);
        bb.writeInt(HopCapability);
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        srcVPort.putTo(sink);
        dstVport.putTo(sink);
        sink.putLong(BandwidthCapability);
        sink.putInt(DelayCapability);
        sink.putInt(HopCapability);
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj){
            return true;
        }
        if (obj==null)
            return false;
        if (getClass()!=obj.getClass())
            return false;
        HCPInternalLink other=(HCPInternalLink)obj ;
        if (this.srcVPort==null){
            if(other.srcVPort!=null)
                return false;
        }else if (!this.srcVPort.equals(other.srcVPort))
            return false;
        if (this.dstVport==null){
            if (other.dstVport!=null)
                return false;
        }
        else if (!this.srcVPort.equals(other.srcVPort)){
            return false;
        }
        if (this.BandwidthCapability!=other.BandwidthCapability)
            return false;
        if (this.DelayCapability!=other.DelayCapability)
            return false;
        if (this.HopCapability!=other.HopCapability)
            return false;
        return true;
    }
}
