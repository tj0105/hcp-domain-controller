package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author ldy
 * @Date: 20-2-23 下午10:56
 * @Version 1.0
 */
public class HCPTopologyRequestVer10 implements HCPTopologyRequest {
    public static final Logger logger= LoggerFactory.getLogger(HCPTopologyRequestVer10.class);

    //version
    public static final byte WIRE_VERSION=1;
    public static final int LENGTH=8;

    private static final long DEFAULT_XID=0x0L;

    //hcp topology request message field;

    private final long xid;

    HCPTopologyRequestVer10(long xid){
        this.xid=xid;
    }
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_TOPO_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements HCPMessageWriter<HCPTopologyRequestVer10> {
        @Override
        public void write(ChannelBuffer bb, HCPTopologyRequestVer10 message){
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(HCPType.HCP_TOPO_REQUEST.value());
            //length
            bb.writeShort(LENGTH);
            //xid
            bb.writeInt((int) message.xid);
        }
    }

    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPTopologyRequest>{

        @Override
        public HCPTopologyRequest readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0x9)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_TOPOLOGY_REQUEST(9),got="+type);
            int length = bb.readShort();
            // xid
            long xid = U32.f(bb.readInt());
            return new HCPTopologyRequestVer10(xid);
        }
    }
    @Override
    public Builder createBuilder() {
        return null;
    }
    static class Builder implements HCPTopologyRequest.Builder{
        private boolean xidSet;
        private long xid;
        @Override
        public HCPTopologyRequest build() {
            long xid=this.xidSet? this.xid:DEFAULT_XID;
            return new HCPTopologyRequestVer10(xid);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_TOPO_REQUEST;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPTopologyRequest.Builder setXid(long xid) {
            this.xidSet=true;
            this.xid=xid;
            return this;
        }
    }
    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null)
            return false;
        if (getClass()!=obj.getClass())
            return false;
        HCPTopologyRequestVer10 other=(HCPTopologyRequestVer10) obj;
        if (xid!=other.xid)
            return false;
        return true;
    }
}
