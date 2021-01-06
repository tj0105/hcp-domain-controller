package org.onosproject.hcp.protocol.ver10;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.HCPIOT;
import org.onosproject.hcp.types.U32;
import org.onosproject.hcp.util.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @Author ldy
 * @Date: 2021/1/6 上午10:12
 * @Version 1.0
 */
public class HCPIoTReplyVer10 implements HCPIoTReply {
    public final static Logger logger = LoggerFactory.getLogger(HCPIoTReplyVer10.class);

    //version
    public static final byte WIRE_VERSION = 1;
    public static final int MINIMUN_LENGTH = 8;

    private static final long DEFAULT_XID = 0x0L;
    private static final DomainId DEFAULT_DOMAIN_ID = DomainId.None;
    private static final List<HCPIOT> DEFAULT_IOT_LIST = ImmutableList.of();

    //hcp iot reply message field;
    private final long xid;
    private final List<HCPIOT>  iots;
    private final DomainId domainID;

    HCPIoTReplyVer10(long xid, DomainId domainId, List<HCPIOT> iots){
        if (iots == null){
            throw new NullPointerException("HCPIoTReplyVer10: property iots must not be null");
        }
        if (domainId == null){
            throw new NullPointerException("HCPIoTReplyVer10: property domainId must not be null");
        }
        this.xid = xid;
        this.domainID = domainId;
        this.iots = iots;
    }
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_IOT_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public DomainId getDomainId() {
        return domainID;
    }

    @Override
    public List<HCPIOT> getIoTs() {
        return iots;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements HCPMessageWriter<HCPIoTReplyVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPIoTReplyVer10 message) {
            int startIndex = bb.writerIndex();
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(HCPType.HCP_IOT_REPLY.value());
            //temp length
            int lengthIndex = bb.writerIndex();
            bb.writeShort(0);
            //xid;
            bb.writeInt(U32.t(message.xid));
            //domainId
            bb.writeLong(message.domainID.getLong());
            //iots
            ChannelUtils.writeList(bb,message.iots);
            //update length
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);
        }
    }

    static final Reader READER = new Reader();
    static class Reader implements HCPMessageReader<HCPIoTReply>{

        @Override
        public HCPIoTReply readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex = bb.readerIndex();
            //version
            byte version = bb.readByte();
            if (version != (byte) 0x1){
                throw new HCPParseError("Wrong version: Expected = HCPVersion.HCP_10(1), got =" + version);
            }
            byte type = bb.readByte();
            if (type != (byte) HCPType.HCP_IOT_REPLY.value()){
                throw new HCPParseError("Wrong type: Expected = HCPType.HCP_IOT_REPLY(16), got = " + type);
            }
            int length = bb.readShort();
            if (length < MINIMUN_LENGTH){
                throw new HCPParseError("Wrong length: Expected to be > " + MINIMUN_LENGTH + ",was :" + length);
            }
            if (bb.readableBytes() + (bb.readerIndex() - startIndex) < length){
                bb.readerIndex(startIndex);
                return null;
            }
            //xid
            long xid = U32.f(bb.readInt());
            //domainId
            DomainId domainId = DomainId.of(bb.readLong());
            List<HCPIOT> iots = ChannelUtils.readList(bb,length - (bb.readerIndex() - startIndex),HCPIoTVer10.READER);
            return new HCPIoTReplyVer10(xid,domainId,iots);
        }
    }

    static class Builder implements HCPIoTReply.Builder{
        private boolean xidSet;
        private long xid;
        private boolean domainIdSet;
        private DomainId domainId;
        private boolean iotSet;
        private List<HCPIOT> iots;
        @Override
        public HCPIoTReply build() {
            long xid = this.xidSet ? this.xid:DEFAULT_XID;
            List<HCPIOT> iots = this.iotSet ? this.iots: DEFAULT_IOT_LIST;
            if (iots == null){
                throw new NullPointerException("HCPIoTReplyVer10: property iots must not be null");
            }
            DomainId domainId = this.domainIdSet ? this.domainId : DEFAULT_DOMAIN_ID;
            if (domainId == null)
                throw new NullPointerException("HCPIoTReplyVer10: property iots must not be null");
            return new HCPIoTReplyVer10(xid, domainId,iots);
        }
        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_IOT_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPIoTReply.Builder setXid(long xid) {
            this.xidSet = true;
            this.xid = xid;
            return this;
        }

        @Override
        public List<HCPIOT> getIoTs() {
            return iots;
        }

        @Override
        public HCPIoTReply.Builder setIoTs(List<HCPIOT> IotList) {
            this.iotSet = true;
            this.iots = IotList;
            return this;
        }

        @Override
        public DomainId getDomainId() {
            return domainId;
        }

        @Override
        public HCPIoTReply.Builder setDomainId(DomainId domainId) {
            this.domainIdSet = true;
            this.domainId = domainId;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HCPIoTReplyVer10 that = (HCPIoTReplyVer10) o;
        return xid == that.xid && Objects.equals(iots, that.iots) && Objects.equals(domainID, that.domainID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xid, iots, domainID);
    }

    @Override
    public String toString() {
        StringBuilder  b = new StringBuilder("HCPIoTReply:");
        b.append("xid = ").append(xid).append(",");
        b.append("doaminId = ").append(domainID.getLong()).append(",");
        b.append("listIotTs = ").append(iots.toString());
        return b.toString();
    }
}
