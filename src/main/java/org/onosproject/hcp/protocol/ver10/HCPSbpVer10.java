package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.HCPSbpData;
import org.onosproject.hcp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-2-26 下午8:40
 * @Version 1.0
 */
public class HCPSbpVer10  implements HCPSbp{

    private static final Logger logger= LoggerFactory.getLogger(HCPSbpVer10.class);

    //version 1
    private final static byte WIRE_VERSION=1;

    //
    private final long xid;
    private final HCPSbpCmpType sbpCmpType;
    private final Set<HCPSbpFlags> flags;
    private final short dataLength;
    private final long sbpXid;
    private final HCPSbpData sbpData;
    private final HCPSbpCmpData sbpCmpData;

    HCPSbpVer10(long  xid,HCPSbpCmpType sbpCmpType,Set<HCPSbpFlags> flags,
                short dataLength,long sbpXid,HCPSbpData sbpData,HCPSbpCmpData sbpCmpData){
        if (sbpCmpType == null)
            throw new NullPointerException("HCPSbpVer10: Property sbpCmdType must not be null");
        if (flags == null)
            throw new NullPointerException("HCPSbpVer10: Property flags must not be null");
        this.xid=xid;
        this.sbpCmpType=sbpCmpType;
        this.flags=flags;
        this.dataLength=dataLength;
        this.sbpXid=sbpXid;
        this.sbpData=sbpData;
        this.sbpCmpData=sbpCmpData;
    }
    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public HCPType getType() {
        return HCPType.HCP_SBP;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public HCPSbpCmpType getSbpCmpType() {
        return sbpCmpType;
    }

    @Override
    public Set<HCPSbpFlags> getFlags() {
        return flags;
    }

    @Override
    public short getDataLength() {
        return dataLength;
    }

    @Override
    public long getSbpXid() {
        return sbpXid;
    }

    @Override
    public HCPSbpCmpData getSbpCmpData() {
        return sbpCmpData;
    }

    @Override
    public HCPSbpData getSbpData() {
        return sbpData;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }

    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPSbpVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPSbpVer10 message){
            int startIndex=bb.writerIndex();
            //version
            bb.writeByte(WIRE_VERSION);
            //type
            bb.writeByte(HCPType.HCP_SBP.value());
            //tmp length
            int lengthIndex=bb.writerIndex();
            bb.writeShort(0);
            //xid
            bb.writeInt(U32.t(message.xid));
            //sbpcmpType
            HCPSbpCmpTypeSerializerVer10.writeTo(bb,message.sbpCmpType);
            //flags
            HCPSbpFlagsSerializerVer10.writeTo(bb,message.flags);
            //datalength
            bb.writeShort(message.dataLength);
            //sbpxid
            bb.writeInt(U32.t(message.sbpXid));

            switch (message.sbpCmpType){
                case NORMAL:
                    bb.writeBytes(message.sbpData.getData());
                    break;
                case FLOW_FORWARDING_REPLY:
                    message.sbpCmpData.writeTo(bb);
                    break;
                case FLOW_FORWARDING_REQUEST:
                    message.sbpCmpData.writeTo(bb);
                    break;
                case PACKET_IN:
                    message.sbpCmpData.writeTo(bb);
                    break;
                case PACKET_OUT:
                    message.sbpCmpData.writeTo(bb);
                    break;
                case RESOURCE_REQUEST:
                    message.sbpCmpData.writeTo(bb);
                    break;
                case RESOURCE_REPLY:
                    message.sbpCmpData.writeTo(bb);
                default:
            }

            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);


        }
    }

    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPSbp>{

        @Override
        public HCPSbp readFrom(ChannelBuffer bb) throws HCPParseError {
            int startIndex=bb.readerIndex();
            //version
            byte version=bb.readByte();
            //check version
            if(version!=(byte)0x1)
                throw new HCPParseError("Wrong version:Expected=HCPVersion.HCP_10(1), got="+version);
            //type
            byte type=bb.readByte();
            if (type!=(byte)0xf)
                throw new HCPParseError("Wrong type:Expected=HCPType.HCP_SBP(15),got="+type);
            int length=bb.readShort();
            if (bb.readableBytes()+(bb.readerIndex()-startIndex)<length){
                bb.readerIndex(startIndex);
                return null;
            }

            //xid
            long xid= U32.f(bb.readInt());
            //sbpCmpType
            HCPSbpCmpType sbpCmpType=HCPSbpCmpTypeSerializerVer10.readFrom(bb);
            //flags
            Set<HCPSbpFlags> flags=HCPSbpFlagsSerializerVer10.readFrom(bb);
            //dataLength
            int dataLength=bb.readShort();
            //sbpxid
            long sbpXid=U32.f(bb.readInt());
            //sbpData
            HCPSbpData sbpData=null;
            HCPSbpCmpData sbpCmpData=null;
            switch (sbpCmpType){
                case NORMAL:
                    sbpData=HCPSbpData.read(bb,length-(bb.readerIndex()-startIndex),HCPVersion.HCP_10);
                    break;
                case FLOW_FORWARDING_REPLY:
                    sbpCmpData=HCPForwardingReplyVer10.read(bb);
                    break;
                case FLOW_FORWARDING_REQUEST:
                    sbpCmpData=HCPForwardingRequestVer10.read(bb,dataLength);
                    break;
                case PACKET_IN:
                    sbpCmpData=HCPPacketInVer10.read(bb,dataLength);
                    break;
                case PACKET_OUT:
                    sbpCmpData=HCPPacketOutVer10.read(bb,dataLength);
                    break;
                case RESOURCE_REQUEST:
                    sbpCmpData=HCPResourceRequestVer10.read(bb);
                    break;
                case RESOURCE_REPLY:
                    sbpCmpData=HCPResourceReplyVer10.read(bb,dataLength);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal vaule for sbpcmptype in hcp version 1.0:"+sbpCmpType);
            }
            return new HCPSbpVer10(xid,sbpCmpType,flags,(short)dataLength,sbpXid,sbpData,sbpCmpData);
        }
    }
    static class Builder implements HCPSbp.Builder{
        private  long xid;
        private  HCPSbpCmpType sbpCmpType;
        private  Set<HCPSbpFlags> flags;
        private  short dataLength;
        private  long sbpXid;
        private  HCPSbpData sbpData;
        private  HCPSbpCmpData sbpCmpData;
        @Override
        public HCPSbp build() {
            if (sbpCmpType == null)
                throw new NullPointerException("HCPSbpVer10: Property sbpCmdType must not be null");
            if (flags == null)
                throw new NullPointerException("HCPSbpVer10: Property flags must not be null");
            return new HCPSbpVer10(xid,sbpCmpType,flags,dataLength,sbpXid,sbpData,sbpCmpData);
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }

        @Override
        public HCPType getType() {
            return HCPType.HCP_SBP;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public HCPSbp.Builder setXid(long xid) {
            this.xid=xid;
            return this;
        }

        @Override
        public HCPSbpCmpType getSbpCmpType() {
            return sbpCmpType;
        }

        @Override
        public HCPSbp.Builder setSbpCmpType(HCPSbpCmpType sbpCmpType) {
            this.sbpCmpType=sbpCmpType;
            return this;
        }

        @Override
        public Set<HCPSbpFlags> getFlags() {
            return flags;
        }

        @Override
        public HCPSbp.Builder setFlags(Set<HCPSbpFlags> flagsSet) {
            this.flags=flagsSet;
            return this;
        }

        @Override
        public short getDataLength() {
            return dataLength;
        }

        @Override
        public HCPSbp.Builder setDataLength(short length) {
            this.dataLength=length;
            return this;
        }

        @Override
        public long getSbpXid() {
            return sbpXid;
        }

        @Override
        public HCPSbp.Builder setSbpXid(long xid) {
            this.sbpXid=xid;
            return this;
        }

        @Override
        public HCPSbpData getSbpData() {
            return sbpData;
        }

        @Override
        public HCPSbp.Builder setSbpData(HCPSbpData hcpSbpData) {
            this.sbpData=hcpSbpData;
            return this;
        }

        @Override
        public HCPSbpCmpData getSbpCmpData() {
            return sbpCmpData;
        }

        @Override
        public HCPSbp.Builder setSbpCmpData(HCPSbpCmpData sbpCmpData) {
            this.sbpCmpData=sbpCmpData;
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
        if (obj==null||getClass()!=obj.getClass())
            return false ;
        HCPSbpVer10 other=(HCPSbpVer10) obj;
        if (xid!=other.xid)
            return false;
        if (sbpCmpType==null){
            if(other.sbpCmpType!=null){
                return false;
            }
        }else if(!sbpCmpType.equals(other.sbpCmpType))
            return false ;
        if (dataLength!=other.dataLength)
            return false ;
        if (sbpXid!=other.sbpXid)
            return false ;
        if (sbpData==null){
            if(other.sbpData!=null){
                return false;
            }
        }else if(!sbpData.equals(other.sbpData))
            return false ;
        if (sbpCmpData==null){
            if(other.sbpCmpData!=null){
                return false;
            }
        }else if(!sbpCmpData.equals(other.sbpCmpData))
            return false ;
        return true;
    }


}
