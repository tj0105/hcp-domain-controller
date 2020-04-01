package org.onosproject.hcp.types;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.protocol.Writeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author ldy
 * @Date: 20-3-28 上午12:29
 * @Version 1.0
 */
public class HCPVportHop implements Writeable,PrimitiveSinkable {
    private final static Logger log= LoggerFactory.getLogger(HCPVportHop.class);

    private final HCPVport vport;
    private final int hop;

    HCPVportHop(HCPVport vport,int hop){
        this.vport=vport;
        this.hop=hop;
    }


    public HCPVport getVport(){
        return vport;
    }

    public int getHop(){
        return hop;
    }

    public static HCPVportHop of(HCPVport vport,int Hop){
        return new HCPVportHop(vport,Hop);
    }
    @Override
    public void writeTo(ChannelBuffer bb) {
        vport.writeTo(bb);
        bb.writeInt(hop);
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        vport.putTo(sink);
        sink.putInt(hop);
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null)
            return false;
        if (getClass()!=obj.getClass())
            return false;
        HCPVportHop hcpVportHop=(HCPVportHop)obj;
        if (this.vport==null) {
            if (hcpVportHop.vport != null)
                return false;
        }else if (!this.vport.equals(hcpVportHop.vport))
            return false;
        if (this.hop!=hcpVportHop.hop){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HCPVportHop{" +
                "vport=" + vport +
                ", hop=" + hop +
                '}';
    }
}
