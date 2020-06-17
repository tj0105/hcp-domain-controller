package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.U16;
import org.onosproject.hcp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-22 下午10:30
 * @Version 1.0
 */
public class HCPHelloVer10 implements HCPHello {
    public final static Logger logger= LoggerFactory.getLogger(HCPHelloVer10.class);

    //version
    public final static byte WIRE_VERSION=1;
    public final static int LENGTH=31;

    private static final long DEFAULT_XID=0x0L;

    //hcp hello message field
    private final long xid;

    HCPHelloVer10(long xid){
        this.xid=xid;
    }
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_HELLO;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public List<HCPHelloElem> getElements() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property of the helloElem was not support in hcp version 1.0");
    }

    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPHello>{

        @Override
        public HCPHello readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0x0)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_HELLO(0),got="+type);
            int length= U16.f(bb.readShort());
            if (length!=LENGTH)
                throw new HCPParseError("Wrong length: Expected=8(8), got=" + length);
            if (bb.readableBytes() + (bb.readerIndex() - startIndex) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(startIndex);
                return null;
            }
            if (logger.isTraceEnabled()){
                logger.trace("readFrom-length={}",length);
            }
            bb.skipBytes(23);
            long xid= U32.f(bb.readInt());


            HCPHelloVer10 hcpHelloVer10=new HCPHelloVer10(xid);
            if (logger.isTraceEnabled()) {
                logger.trace("readFrom - read={}", hcpHelloVer10);
            }
            return hcpHelloVer10;
        }
    }

    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPHelloVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPHelloVer10 message) {
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(0);
            //length
            bb.writeShort(LENGTH);
            //xid
            bb.writeZero(23);
            bb.writeInt(U32.t(message.xid));

            //skip to 31


        }
    }
    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }

    static class Builder implements HCPHello.Builder{
        private boolean xidSet;
        private long xid;
        @Override
        public HCPHello build() {
            long xid=this.xidSet ? this.xid:DEFAULT_XID;
            return new HCPHelloVer10(xid);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_HELLO;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPHello.Builder setXid(long xid) {
            this.xidSet=true;
            this.xid=xid;
            return this;
        }

        @Override
        public List<HCPHelloElem> getElements() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("property hello elem value was not supported in hcp protocol version 1.0");
        }

        @Override
        public HCPHello.Builder setElemntes(List<HCPHelloElem> elemntes) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("property hello elem value was not supported in hcp protocol version 1.0");
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
    public String toString() {
        StringBuilder b=new StringBuilder("HCPHelloVer10:");
        b.append("xid=").append(xid);
        return b.toString();
    }

    @Override
    public int hashCode() {
        int prime=31;
        int result=1;
        result =prime*U32.t(xid^(xid>>>32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null)
            return false;
        if (getClass()!=obj.getClass())
            return false;
        HCPHelloVer10 other=(HCPHelloVer10)obj;
        if (xid!=other.xid)
            return false;
        return true;
    }
}
