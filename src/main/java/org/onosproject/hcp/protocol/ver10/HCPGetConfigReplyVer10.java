package org.onosproject.hcp.protocol.ver10;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-2-22 下午9:32
 * @Version 1.0
 */
public class HCPGetConfigReplyVer10 implements HCPGetConfigReply{
    public static final Logger logger= LoggerFactory.getLogger(HCPGetConfigReplyVer10.class);

    //hcp version
    public static final byte WIRE_VERSION=1;
    public static final int LENGTH=31;

    private static final long DEFAULT_XID=0x0L;
    private static final Set<HCPConfigFlags> DEFAULT_FLAGS= ImmutableSet.<HCPConfigFlags>of();
    private static final byte DEFAULT_PERIOD=5;
    private static final short DEFAULT_MISS_SEND_LENGTH=0;

    //hcp get config reply message field
    private final long xid;
    private final Set<HCPConfigFlags> flags;
    private final byte period;
    private final short missSendLength;

    public static HCPGetConfigReplyVer10 INSTANCE=new HCPGetConfigReplyVer10(DEFAULT_XID,DEFAULT_FLAGS,DEFAULT_PERIOD,DEFAULT_MISS_SEND_LENGTH);

    public HCPGetConfigReplyVer10(long xid,Set<HCPConfigFlags> flags,byte period,short missSendLength){
        if(flags==null){
            throw new NullPointerException("HCPGetConfigReplyVer10: property flags must not be null");
        }
        this.xid=xid;
        this.flags=flags;
        this.period=period;
        this.missSendLength=missSendLength;
    }

    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_GET_CONFIG_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public Set<HCPConfigFlags> getFlags() {
        return flags;
    }

    @Override
    public byte getPeriod() {
        return period;
    }

    @Override
    public short getMissSendLength() {
        return missSendLength;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }

    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPGetConfigReplyVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPGetConfigReplyVer10 message)  {
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(7);
            //length
            bb.writeShort(LENGTH);
            //xid
            bb.writeInt(U32.t(message.xid));
            //flags
            HCPConfigFlagsSerializerVer10.wirteTo(bb,message.flags);
            //period
            bb.writeByte(message.period);
            //miss send length
            bb.writeZero(19);
            bb.writeShort(message.missSendLength);

        }
    }

    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPGetConfigReply>{

        @Override
        public HCPGetConfigReply readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0x7)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_GET_CONFIG_REPLY(7),got="+type);
            //length
            int length=bb.readShort();
            if (bb.readableBytes()+(bb.readerIndex()-startIndex)<length){
                bb.readerIndex(startIndex);
                return null;
            }
            //xid;
            long xid= U32.f(bb.readInt());
            //flags
            Set<HCPConfigFlags> flag=HCPConfigFlagsSerializerVer10.readFrom(bb);
            //period;
            byte period=bb.readByte();
            //miss send length
            bb.writeZero(19);
            short missSendLength=bb.readShort();

            return new HCPGetConfigReplyVer10(xid,flag,period,missSendLength);


        }
    }

    static class Builder implements HCPGetConfigReply.Builder{
        private boolean xidSet;
        private long xid;
        private boolean flagsSet;
        private Set<HCPConfigFlags> flags;
        private boolean periodSet;
        private byte period;
        private boolean missSendLengthSet;
        private short missSendLength;
        @Override
        public HCPGetConfigReply build() {
            long xid=this.xidSet ? this.xid:DEFAULT_XID;
            Set<HCPConfigFlags> flags=this.flagsSet ? this.flags:DEFAULT_FLAGS;
            if (flags==null){
                throw new NullPointerException("HCPGetConfigReplyVer10: property flags must not be null");
            }
            byte period=this.periodSet ? this.period:DEFAULT_PERIOD;
            short missSendLength=this.missSendLengthSet ? this.missSendLength:DEFAULT_MISS_SEND_LENGTH;
            return new HCPGetConfigReplyVer10(xid,flags,period,missSendLength);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_GET_CONFIG_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPGetConfigReply.Builder setXid(long xid) {
            this.xidSet=true;
            this.xid=xid;
            return this;
        }

        @Override
        public Set<HCPConfigFlags> getFlags() {
            return flags;
        }

        @Override
        public HCPGetConfigReply.Builder setFlags(Set<HCPConfigFlags> flags) {
            this.flagsSet=true;
            this.flags=flags;
            return this;
        }

        @Override
        public byte getPeriod() {
            return period;
        }

        @Override
        public HCPGetConfigReply.Builder setPeriod(byte period) {
            this.periodSet=true;
            this.period=period;
            return this;
        }

        @Override
        public short getMissSendLength() {
            return missSendLength;
        }

        @Override
        public HCPGetConfigReply.Builder setMissSendLength(short length) {
            this.missSendLengthSet=true;
            this.missSendLength=length;
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
        if(this==obj)
            return true;
        if (obj==null)
            return false;
        if (getClass()!=obj.getClass())
            return false;
        HCPGetConfigReplyVer10 other=(HCPGetConfigReplyVer10)obj;
        if (xid!=other.xid)
            return false;
        if (flags==null){
            if (other.flags!=null)
                return false;
        }else if(!flags.equals(other.flags))
            return false;
        if (period!=other.period)
            return false;
        if (missSendLength!=other.missSendLength)
            return false;
        return true;
    }
}
