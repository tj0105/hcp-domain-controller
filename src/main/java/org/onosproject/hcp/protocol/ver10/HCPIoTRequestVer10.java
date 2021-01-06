package org.onosproject.hcp.protocol.ver10;

import com.google.common.eventbus.DeadEvent;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.U16;
import org.onosproject.hcp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @Author ldy
 * @Date: 2021/1/5 下午5:56
 * @Version 1.0
 */
public class HCPIoTRequestVer10 implements HCPIoTRequest {

    public final static Logger logger = LoggerFactory.getLogger(HCPIoTRequestVer10.class);

    //version
    private static final byte WIRE_VERSION = 1;
    private static final int LENGTH = 8;

    private static final long DEFAULT_XID = 0x0L;

    // hcp update message filed
    private final long xid;

    HCPIoTRequestVer10(long xid){
        this.xid = xid;
    }
    
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_IOT_REQUEST;
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
    static class Writer implements HCPMessageWriter<HCPIoTRequestVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPIoTRequestVer10 message) {
            //hcp version
            bb.writeByte(WIRE_VERSION);
            //hcp type
            bb.writeByte(HCPType.HCP_IOT_REQUEST.value());
            //length
            bb.writeShort(LENGTH);
            //xid
            bb.writeInt((int) message.xid);
        }
    }

    static final Reader READER = new Reader();
    static class Reader implements HCPMessageReader<HCPIoTRequest>{

        @Override
        public HCPIoTRequest readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex = bb.readerIndex();
            //version
            byte version = bb.readByte();
            if(version != (byte) 0x1) {
                throw new HCPParseError("Wrong version: Expected = HCPVersion.HCP_10(1), got = " + version);
            }
            byte type = bb.readByte();
            if (type != (byte) HCPType.HCP_IOT_REQUEST.value()){
                throw new HCPParseError("Wrong type: Expected = HCPType.HCP_IOT_REQUEST(16), got =" + type);
            }
            int length = U16.f(bb.readShort());
            if (length != LENGTH){
                throw new HCPParseError("Wrong length: Expected to be = "+ LENGTH + ",was:" + length);
            }
            if (bb.readableBytes() + (bb.readerIndex() - startIndex) < length){
                bb.readerIndex(startIndex);
                return null;
            }
            long xid = U32.f(bb.readInt());
            return new HCPIoTRequestVer10(xid);
        }
    }

    static class Builder implements HCPIoTRequest.Builder{
        private boolean xidSet;
        private long xid;
        @Override
        public HCPIoTRequest build() {
            long xid = this.xidSet ? this.xid: DEFAULT_XID;
            return new HCPIoTRequestVer10(xid);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_IOT_REQUEST;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPIoTRequest.Builder setXid(long xid) {
            this.xidSet = true;
            this.xid = xid;
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
    public String toString() {
       StringBuilder b = new StringBuilder("HCPIoTRequest:");
       b.append("xid=").append(xid);
       return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HCPIoTRequestVer10 that = (HCPIoTRequestVer10) o;
        return xid == that.xid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xid);
    }
}
