package org.onosproject.hcp.protocol.ver10;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.PrimitiveSink;
import org.apache.commons.lang.ObjectUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.U16;
import org.onosproject.hcp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-2-22 下午7:31
 * @Version 1.0
 */
public class HCPFeaturesReplyVer10 implements HCPFeaturesReply{
    public static final Logger logger= LoggerFactory.getLogger(HCPFeaturesReplyVer10.class);

    //version
    private static byte WIRE_VERSION=1;
    private static int LENGTH=31;

    private static final long DEFAULT_XID=0x0L;
    private static final DomainId DEFAULT_DOMAIN_ID=DomainId.None;
    public static final Set<HCPCapabilities> DEFAULT_CAPABILITIES= ImmutableSet.<HCPCapabilities>of();

    //hcp feature reply message field
    private final long xid;
    private final DomainId domainId;
    private final HCPSbpType sbpType;
    private final HCPSbpVersion sbpVersion;
    private final Set<HCPCapabilities> capabilities;

    public HCPFeaturesReplyVer10(long xid,DomainId domainId,HCPSbpType sbpType,HCPSbpVersion sbpVersion,Set<HCPCapabilities> capabilities){
        if (domainId==null){
            throw new NullPointerException("HCPFeaturesReplyVer10: property domainId must not be null");
        }
        if (sbpType==null)
            throw new NullPointerException("HCPFeaturesReplyVer10: property sbpType must not be null");
        if (sbpVersion==null)
            throw new NullPointerException("HCPFeaturesReplyVer10: property sbpversion must not be null");
        if (capabilities==null)
            throw new NullPointerException("HCPFeaturesReplyVer10: property capabilities must not be null");
        this.xid=xid;
        this.domainId=domainId;
        this.sbpType=sbpType;
        this.sbpVersion=sbpVersion;
        this.capabilities=capabilities;
    }
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_FEATURES_REPLY;
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
    public HCPSbpType getSbpType() {
        return sbpType;
    }

    @Override
    public HCPSbpVersion getSbpVersion() {
        return sbpVersion;
    }

    @Override
    public Set<HCPCapabilities> getCapabilities() {
        return capabilities;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }


    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPFeaturesReplyVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPFeaturesReplyVer10 message)  {
            //version
            bb.writeByte(1);
            //hcp features reply type
            bb.writeByte(5);
            //length
            bb.writeShort(LENGTH);
            //xid
            bb.writeInt(U32.t(message.xid));
            //domainId
            bb.writeLong(message.domainId.getLong());
            //sbpType
            HCPSbpTypeSerializerVer10.writeTo(bb,message.sbpType);
            //sbpVersion
            message.sbpVersion.writeTo(bb);
            // byte skip 2
            bb.writeZero(9);
            //capabitilies
            HCPCapabilitiesSerializerVer10.writeTo(bb,message.capabilities);
        }
    }
    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPFeaturesReply>{

        @Override
        public HCPFeaturesReply readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0x5)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_FEATURES_REPLY(5),got="+type);
            int length= U16.f(bb.readShort());
            if (length!=LENGTH)
                throw new HCPParseError("Wrong length: Expected to be >= " + LENGTH + ", was: " + length);
            if (bb.readableBytes() + (bb.readerIndex() - startIndex) < length) {
                bb.readerIndex(startIndex);
                return null;
            }

            //xid
            long xid= U32.f(bb.readInt()) ;
            //DomainId
            DomainId domainId=DomainId.of(bb.readLong());
            //sbpType
            HCPSbpType sbpType=HCPSbpTypeSerializerVer10.readFrom(bb);
            //sbpVersion
            HCPSbpVersion sbpVersion=HCPSbpVersion.of(bb.readByte(),HCPVersion.HCP_10);
            bb.skipBytes(9);
            //capabilities
            Set<HCPCapabilities> capabilities=HCPCapabilitiesSerializerVer10.readFrom(bb);
            return new HCPFeaturesReplyVer10(xid,domainId,sbpType,sbpVersion,capabilities);
        }

    }

    static final class Builder implements HCPFeaturesReply.Builder{
        private boolean xidSet;
        private long xid;
        private boolean domainIdSet;
        private DomainId domainId;
        private boolean sbpTypeSet;
        private HCPSbpType sbpType;
        private boolean sbpVersionSet;
        private HCPSbpVersion sbpVersion;
        private boolean capabilitiesSet;
        private Set<HCPCapabilities> capabilities;

        @Override
        public HCPFeaturesReply build() {
            long xid=this.xidSet ? this.xid:DEFAULT_XID;
            DomainId domainId=this.domainIdSet ? this.domainId:DEFAULT_DOMAIN_ID;
            if (domainId==null)
                throw new NullPointerException("Property domainId must not be null");
            if (this.sbpType==null)
                throw new NullPointerException("Property sbpType must not be null");
            HCPSbpType sbpType=this.sbpType;
            if (this.sbpVersion==null)
                throw new NullPointerException("Property sbpVersion must not be null");
            HCPSbpVersion sbpVersion=this.sbpVersion;
            if (this.capabilities==null)
                throw new NullPointerException("Property capabitilies must not be null");
            Set<HCPCapabilities> capabilities=this.capabilities;
            return new HCPFeaturesReplyVer10(xid,domainId,sbpType,sbpVersion,capabilities);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_FEATURES_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPFeaturesReply.Builder setXid(long xid) {
            this.xidSet=true;
            this.xid=xid;
            return this;
        }

        @Override
        public DomainId getDomainId() {
            return domainId;
        }

        @Override
        public HCPFeaturesReply.Builder setDomainId(DomainId domainId) {
            this.domainIdSet=true;
            this.domainId=domainId;
            return this;
        }

        @Override
        public HCPSbpType getSbpType() {
            return sbpType;
        }

        @Override
        public HCPFeaturesReply.Builder setSbpType(HCPSbpType hcpSbpType) {
            this.sbpTypeSet=true;
            this.sbpType=hcpSbpType;
            return this;
        }

        @Override
        public HCPSbpVersion getSbpVersion() {
            return sbpVersion;
        }

        @Override
        public HCPFeaturesReply.Builder setSbpVersion(HCPSbpVersion hcpSbpVersion) {
            this.sbpVersionSet=true;
            this.sbpVersion=hcpSbpVersion;
            return this;
        }

        @Override
        public Set<HCPCapabilities> getCapabilities() {
            return capabilities;
        }

        @Override
        public HCPFeaturesReply.Builder setCapabilities(Set<HCPCapabilities> hcpCapabilities) {
            this.capabilitiesSet=true;
            this.capabilities=hcpCapabilities;
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
        HCPFeaturesReplyVer10 other=(HCPFeaturesReplyVer10) obj;

        if (xid!=other.xid)
            return false;
        if (domainId==null){
            if (other.domainId!=null)
                return false;
        }else if(!domainId.equals(other.domainId))
            return false;
        if (sbpType==null){
            if (other.sbpType!=null)
                return false;
        }else if(!sbpType.equals(other.sbpType))
            return false;
        if (sbpVersion==null){
            if (other.sbpVersion!=null)
                return false;
        }else if(!sbpVersion.equals(other.sbpVersion))
            return false;
        if (capabilities==null){
            if (other.capabilities!=null)
                return false;
        }else if(!capabilities.equals(other.capabilities))
            return false;
        return true;
    }
}
