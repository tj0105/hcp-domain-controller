package org.onosproject.hcp.protocol.ver10;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.HCPHost;
import org.onosproject.hcp.types.U32;
import org.onosproject.hcp.util.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-22 下午11:02
 * @Version 1.0
 */
public class HCPHostReplyVer10 implements HCPHostReply {
    public final static Logger logger= LoggerFactory.getLogger(HCPHostReplyVer10.class);

    //version
    public static final byte WIRE_VERSION=1;
    public static final int MINIMUN_LENGTH=8;

    private static final long DEFAULT_XID=0x0L;
    private static final DomainId DEFAULT_DOMAIN_ID=DomainId.None;
    private static final List<HCPHost> DEFAULT_HOST_LIST= ImmutableList.of();

    //hcp host reply message field
    private final long xid;
    private final List<HCPHost> hostlist;
    private final DomainId domainId;

    HCPHostReplyVer10(long xid,DomainId domainid,List<HCPHost> hostlist){
        if (hostlist==null){
            throw new NullPointerException("HCPHostReplyVer10: property hostlist must not be null");
        }
        if (domainid==null){
            throw new NullPointerException("HCPHostReplyVer10: property domainId must not be null");
        }
        this.xid=xid;
        this.hostlist=hostlist;
        this.domainId=domainid;
    }

    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_HOST_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public DomainId getDomainId() {
        return domainId;
    }

    @Override
    public List<HCPHost> getHosts() {
        return hostlist;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }
    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPHostReplyVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPHostReplyVer10 message)  {
            int startIndex=bb.writerIndex();
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(12);
            //temp length
            int lengtIndex=bb.writerIndex();
            bb.writeShort(0);
            //xid;
            bb.writeInt(U32.t(message.xid));
            //domainId
            bb.writeLong(message.domainId.getLong());
            //hosts
            ChannelUtils.writeList(bb,message.hostlist);
            //update length
            int length=bb.writerIndex()-startIndex;
            bb.setShort(lengtIndex,length);
        }
    }
    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPHostReply>{

        @Override
        public HCPHostReply readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte) HCPType.HCP_HOST_REPLY.value())
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_HOST_REPLY(12),got="+type);
            int length=bb.readShort();
            if (length<MINIMUN_LENGTH)
                throw new HCPParseError("Wrong length: Expected to be > " + MINIMUN_LENGTH
                        + ", was: " + length);
            if (bb.readableBytes()+(bb.readerIndex()-startIndex)<length){
                bb.readerIndex(startIndex);
                return null;
            }
            //xid
            long xid= U32.f(bb.readInt());
            //domainId
            DomainId domainId=DomainId.of(bb.readLong());
            List<HCPHost> hosts= ChannelUtils.readList(bb,length-(bb.readerIndex()-startIndex),HCPHostVer10.READER);
            return new HCPHostReplyVer10(xid,domainId,hosts);
        }
    }

    static class Builder implements HCPHostReply.Builder{
        private boolean xidSet;
        private long xid;
        private boolean domainIdSet;
        private DomainId domainId;
        private boolean hostsSet;
        private List<HCPHost> hosts;
        @Override
        public HCPHostReply build() {
            long xid=this.xidSet? this.xid:DEFAULT_XID;
            List<HCPHost> hosts=this.hostsSet ? this.hosts:DEFAULT_HOST_LIST;
            if (hosts==null)
                throw new NullPointerException("HCPHostReplyVer10: property hosts must not be null");
            DomainId domainId=this.domainIdSet?this.domainId:DEFAULT_DOMAIN_ID;
            if (domainId==null)
                throw new NullPointerException("HCPHostReplyVer10: property domainId must not be null");
            return new HCPHostReplyVer10(xid,domainId,hosts);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_HOST_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPHostReply.Builder setXid(long xid) {
            this.xidSet=true;
            this.xid=xid;
            return this;
        }

        @Override
        public List<HCPHost> getHosts() {
            return hosts;
        }

        @Override
        public HCPHostReply.Builder setHosts(List<HCPHost> hostList) {
            this.hostsSet=true;
            this.hosts=hostList;
            return this;
        }

        @Override
        public DomainId getDomainId() {
            return domainId;
        }

        @Override
        public HCPHostReply.Builder setDomainId(DomainId domainId) {
            this.domainIdSet=true;
            this.domainId=domainId;
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
        StringBuilder b=new StringBuilder("HCPHostReply:");
        b.append("xid=").append(xid).append(',');
        b.append("domainId=").append(domainId.getLong()).append(',');
        b.append("listHost=").append(hostlist.toString());
        return b.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HCPHostReplyVer10 other = (HCPHostReplyVer10) obj;
        if (this.xid != other.xid)
            return false;
        if (this.hostlist == null) {
            if (other.hostlist != null)
                return false;
        } else if (!this.hostlist.equals(other.hostlist))
            return false;
        if (this.domainId==null){
            if (other.domainId!=null)
                return false;
        }else if (!this.domainId.equals(other.domainId))
            return false;
        return true;
    }
}
