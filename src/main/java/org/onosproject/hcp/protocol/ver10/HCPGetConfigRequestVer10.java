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
 * @Date: 20-2-22 下午10:13
 * @Version 1.0
 */
public class HCPGetConfigRequestVer10 implements HCPGetConfigRequest{
    public final static Logger logger= LoggerFactory.getLogger(HCPGetConfigRequestVer10.class);

    //version
    public final static byte WIRE_VERSION=1;
    public final static byte LENGTH=31;

    private final static long DEFAULT_XID=0x0L;

    //hcp get config request message field
    private final long xid;
    HCPGetConfigRequestVer10(long xid){
        this.xid=xid;
    }
    @Override
    public HCPVersion getVersion(){
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_GET_CONFIG_REQUEST;
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
    static class Writer implements HCPMessageWriter<HCPGetConfigRequestVer10>{
        @Override
        public void write(ChannelBuffer bb, HCPGetConfigRequestVer10 message)  {
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(6);
            //length
            bb.writeShort(LENGTH);
            //xid
            bb.writeZero(23);
            bb.writeInt(U32.t(message.xid));
        }
    }
    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPGetConfigRequest>{

        @Override
        public HCPGetConfigRequest readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0x6)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_GET_CONFIG_REQUEST(6),got="+type);
            int length=bb.readShort();
            //xid
            bb.skipBytes(23);
            long xid= U32.f(bb.readInt());
            return new HCPGetConfigRequestVer10(xid);
        }
    }

    static class Builder implements HCPGetConfigRequest.Builder{
       private boolean xidSet;
       private long xid;
        @Override
        public HCPGetConfigRequest build() {
            long xid=this.xidSet ? this.xid:DEFAULT_XID;
            return new HCPGetConfigRequestVer10(xid);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_GET_CONFIG_REQUEST;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPGetConfigRequest.Builder setXid(long xid) {
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
        HCPGetConfigRequestVer10 other=(HCPGetConfigRequestVer10)obj;
        if (this.xid!=other.xid)
            return false;
        return true;
    }
}
