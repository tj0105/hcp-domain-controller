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
 * @Date: 20-2-23 下午11:13
 * @Version 1.0
 */
public class HCPVportStatusVer10 implements HCPVportStatus{
    public final static Logger logger= LoggerFactory.getLogger(HCPVportStatus.class);

    //version
    static final byte WIRE_VERSION=1;
    static final int LENGTH=24;

    //hcp vport status message field;
    private final long xid;
    private final HCPVportReason reason;
    private final HCPVportDescribtion describtion;

    public HCPVportStatusVer10(long xid, HCPVportReason reason, HCPVportDescribtion vportDesc) {
        this.xid = xid;
        this.reason = reason;
        this.describtion = vportDesc;
    }
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_VPORT_STATUS;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public HCPVportDescribtion getVportDescribtion() {
        return describtion;
    }

    @Override
    public HCPVportReason getReason() {
        return reason;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }

    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPVportStatusVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPVportStatusVer10 message)  {
            int length=bb.writerIndex();
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(HCPType.HCP_VPORT_STATUS.value());
            //length
            bb.writeShort(LENGTH);
            //xid
            bb.writeInt(U32.t(message.xid));
            //reason
            HCPVportReasonSerializerVer10.writeTo(bb,message.reason);
            //description
            bb.writeZero(7);
            message.describtion.writeTo(bb);
        }
    }


    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPVportStatus>{

        @Override
        public HCPVportStatus readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0xe)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_VPORT_STATUS(14),got="+type);
            //length
            int length=bb.readShort();
            if (bb.readableBytes() + (bb.readableBytes() - startIndex) < length) {
                bb.readerIndex(startIndex);
                return null;
            }
            //xid
            long xid=bb.readInt();
            //reason
            HCPVportReason reason=HCPVportReasonSerializerVer10.readFrom(bb);
            //vport description
            HCPVportDescribtion describtion=HCPVportDescriptionVer10.READER.readFrom(bb);
            return new HCPVportStatusVer10(xid,reason,describtion);
        }
    }

    static class Builder implements HCPVportStatus.Builder{
        private long xid;
        private HCPVportReason reason;
        private HCPVportDescribtion describtion;
        @Override
        public HCPVportStatus build() {
            if (reason == null)
                throw new NullPointerException("HCPVportStatusVer10:property reason cannot be null");
            if (describtion == null)
                throw new NullPointerException("HCPVportStatusVer10:property portDesc cannot be null");
            return new HCPVportStatusVer10(xid, reason, describtion);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_VPORT_STATUS;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPVportStatus.Builder setXid(long xid) {
            this.xid=xid;
            return this;
        }

        @Override
        public HCPVportReason getReason() {
            return reason;
        }

        @Override
        public HCPVportStatus.Builder setReson(HCPVportReason reason) {
            this.reason=reason;
            return this;
        }

        @Override
        public HCPVportDescribtion getVportDescribtion() {
            return describtion;
        }

        @Override
        public HCPVportStatus.Builder setVportDescribtion(HCPVportDescribtion vportDescribtion) {
            this.describtion=vportDescribtion;
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
    public int hashCode() {
        final int prime=31;
        int result=1;
        result=prime*result+(U32.t(xid^(xid>>>32)));
        result=prime*result+((reason==null)?0:reason.hashCode());
        result = prime * result + ((describtion == null) ? 0 : describtion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null||getClass()!=obj.getClass())
            return false;
        HCPVportStatusVer10 other=(HCPVportStatusVer10) obj;
        if (this.xid!=other.xid)
            return false;
        if (this.reason==null){
            if (other.reason!=null)
                return false;
        }else if(this.reason.equals(other.reason))
            return false;
        if (this.describtion==null)
            if (other.describtion==null)
                return false;
        else if(this.describtion.equals(other.describtion))
            return false;
        return true;
    }
}
