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
 * @Date: 20-2-22 下午8:54
 * @Version 1.0
 */
public class HCPFeaturesRequestVer10 implements HCPFeaturesRequest {
    public static final Logger logger= LoggerFactory.getLogger(HCPFeaturesReplyVer10.class);

    public static final byte WIRE_VERSION=1;
    public static final int LENGTH=31;

    private static final long DEFAULT_XID=0x0L;

    //hcp features request message field
    private final long xid;

    //Immutable default instance;
    public static final HCPFeaturesRequestVer10 INSTANCE=new HCPFeaturesRequestVer10(DEFAULT_XID);

    HCPFeaturesRequestVer10(long xid){
        this.xid=xid;
    }
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_FEATURES_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }
    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPFeaturesRequestVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPFeaturesRequestVer10 message)  {
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(4);
            //length
            bb.writeShort(LENGTH);
            bb.writeZero(23);
            bb.writeInt(U32.t(message.xid));

        }
    }
    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPFeaturesRequest>{

        @Override
        public HCPFeaturesRequest readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //veriosn
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0x4)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_FEATURES_REQUEST(4),got="+type);
            int legth=bb.readShort();
            bb.skipBytes(23);
            long xid= U32.f(bb.readInt());

            return new HCPFeaturesRequestVer10(xid);
        }
    }
    static class Builder implements HCPFeaturesRequest.Builder{
        //hcp features request message field
        private boolean xidSet;
        private long xid;
        @Override
        public HCPFeaturesRequest build() {
            long xid=this.xidSet?this.xid:DEFAULT_XID;
            return new HCPFeaturesRequestVer10(xid);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_FEATURES_REQUEST;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPFeaturesRequest.Builder setXid(long xid) {
            this.xidSet=true;
            this.xid=xid;
            return this;
        }
    }
    @Override
    public Builder createBuilder() {
        return null;
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
        HCPFeaturesRequestVer10 other=(HCPFeaturesRequestVer10) obj;
        if (xid!=other.xid)
            return false;
        return true;
    }
}
