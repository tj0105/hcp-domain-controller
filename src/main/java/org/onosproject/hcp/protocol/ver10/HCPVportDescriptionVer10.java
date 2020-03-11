package org.onosproject.hcp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.HCPVport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-2-23 下午10:33
 * @Version 1.0
 */
public class HCPVportDescriptionVer10 implements HCPVportDescribtion {
    public final static Logger logger= LoggerFactory.getLogger(HCPVportDescribtion.class);

    public static final int LENGTH=8;

    //the message is a field of  hcp vport status message field
    private final HCPVport portNo;
    private final Set<HCPVportState> state;

    HCPVportDescriptionVer10(HCPVport portNo,Set<HCPVportState> state){
        if (portNo == null)
            throw new NullPointerException("HCPVportDescriptionVer10: property portNo cannot be null");
        if (state == null)
            throw new NullPointerException("HCPVportDescriptionVer10: property state cannot be null");
        this.portNo=portNo;
        this.state=state;
    }

    @Override
    public HCPVport getPortNo() {
        return portNo;
    }

    @Override
    public Set<HCPVportState> getState() {
        return state;
    }

    @Override
    public HCPVersion getVersion() {
        return HCPVersion.HCP_10;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb,this);
    }

    static final Writer WRITER=new Writer();
    static class Writer implements HCPMessageWriter<HCPVportDescriptionVer10>{

        @Override
        public void write(ChannelBuffer bb, HCPVportDescriptionVer10 message){
            bb.writeShort(message.portNo.getPortNumber());
            HCPVportStateSerializerVer10.writeTo(bb,message.state);
            bb.writeZero(2);
        }
    }

    static final Reader READER=new Reader();
    static class Reader implements HCPMessageReader<HCPVportDescribtion>{

        @Override
        public HCPVportDescribtion readFrom(ChannelBuffer bb) throws HCPParseError {
            HCPVport vport=HCPVport.readFrom(bb);
            Set<HCPVportState> state=HCPVportStateSerializerVer10.readFrom(bb);
            bb.skipBytes(2);
            return new HCPVportDescriptionVer10(vport,state);
        }
    }
    public static class Builder implements HCPVportDescribtion.Builder{
        private boolean portNoSet;
        private HCPVport portNo;
        private boolean stateSet;
        private Set<HCPVportState> states;

        @Override
        public HCPVportDescribtion build() {
            if (portNo==null)
                throw new NullPointerException("HCPVportDescriptionVer10: property portNo must not be bull");
            if (states == null)
                throw new NullPointerException("HCPVportDescriptionVer10: property state cannot be null");
            return new HCPVportDescriptionVer10(portNo,states);
        }

        @Override
        public HCPVport getPortNo() {
            return portNo;
        }

        @Override
        public HCPVportDescribtion.Builder setPortNo(HCPVport portNo) {
            this.portNoSet=true;
            this.portNo=portNo;
            return this;
        }

        @Override
        public Set<HCPVportState> getStage() {
            return states;
        }

        @Override
        public HCPVportDescribtion.Builder setState(Set<HCPVportState> state) {
            this.stateSet=true;
            this.states=state;
            return this;
        }

        @Override
        public HCPVersion getVersion() {
            return HCPVersion.HCP_10;
        }
    }
    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj==null||getClass()!=obj.getClass())
            return false;
        HCPVportDescriptionVer10 other=(HCPVportDescriptionVer10) obj;
        if (this.portNo!=other.portNo)
            return false;
        if (!this.state.equals(other.state))
            return false;
        return true;

    }
}
