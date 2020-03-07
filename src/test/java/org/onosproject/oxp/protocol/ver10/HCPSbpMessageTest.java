package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Ethernet;
import org.onlab.packet.HCPLLDP;
import org.onlab.packet.MacAddress;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPPacketInVer10;
import org.onosproject.hcp.protocol.ver10.HCPSbpVer10;
import org.onosproject.hcp.protocol.ver10.HCPVportStatusVer10;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.onosproject.oxp.protocol.ver10.TestBaseVer10.getMessageFactry;
import static org.onosproject.oxp.protocol.ver10.TestBaseVer10.getMessageReader;

/**
 * @Author ldy
 * @Date: 20-3-7 下午7:51
 * @Version 1.0
 */
public class HCPSbpMessageTest {
    @Test
    public void SbpCmpPacketInTest() throws Exception{
        HCPLLDP sbpHCPlldp=HCPLLDP.hcplldp(111,
                1,
                112,1);
        Ethernet ethpacket=new Ethernet();
        ethpacket.setEtherType(Ethernet.TYPE_LLDP);
        ethpacket.setDestinationMACAddress(MacAddress.ONOS_LLDP);
        ethpacket.setPad(true);
        ethpacket.setSourceMACAddress(MacAddress.ONOS_LLDP);
        ethpacket.setPayload(sbpHCPlldp);

        byte [] frame=ethpacket.serialize();
        HCPPacketIn hcpPacketIn= HCPPacketInVer10.of(2,frame);
        ChannelBuffer buffer= ChannelBuffers.dynamicBuffer();
        Set<HCPSbpFlags> flagsSet=new HashSet<>();
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        HCPSbp hcpSbp=getMessageFactry().buildSbp()
                .setSbpCmpType(HCPSbpCmpType.PACKET_IN)
                .setFlags(flagsSet)
                .setDataLength((short)hcpPacketIn.getData().length)
                .setSbpXid(1)
                .setSbpCmpData(hcpPacketIn)
                .build();
        hcpSbp.writeTo(buffer);
        assertThat(hcpSbp, instanceOf(HCPSbpVer10.class));
        HCPMessage message=getMessageReader().readFrom(buffer);
        System.out.println(message.getType());
        System.out.println(message.getXid());
        assertThat(message, instanceOf(hcpSbp.getClass()));
        HCPSbp messageRev = (HCPSbp) message;
        System.out.println("messageRev sbpcmptype:"+messageRev.getSbpCmpType());
        System.out.println("messageRev sbpFlags:"+messageRev.getFlags());
        System.out.println("messageRev sbpDataLength:"+messageRev.getDataLength());
        System.out.println("messageRev sbpCmpData:"+messageRev.getSbpCmpData());
        HCPPacketIn hcpPacketIn1=(HCPPacketIn) messageRev.getSbpCmpData();
        System.out.println("hcpPacket:"+hcpPacketIn1.getInport());
        System.out.println("hcpPacket:"+hcpPacketIn1.getData());
        Ethernet ethernet=parseEthernet(hcpPacketIn1.getData());
        System.out.println("ethernet: "+ethernet.getEtherType());
        System.out.println("ethernet: "+ethernet.getDestinationMAC());
        System.out.println("ethernet: "+ethernet.getSourceMAC());
        System.out.println("ethernet: "+ethernet.getPayload());
        HCPLLDP hcplldp=HCPLLDP.parseHCPLLDP(ethernet);
        System.out.println("hcplldp domainId:"+hcplldp.getDomianId());
        System.out.println("hcplldp deviceID:"+hcplldp.getDpid());
        System.out.println("hcplldp portNumber: "+hcplldp.getPortNum());
        System.out.println("hcplldp VportNumber: "+hcplldp.getVportNum());
        assertThat(hcpSbp, is(messageRev));



    }

    public Ethernet parseEthernet(byte data[]) {
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data);
        Ethernet eth = null;
        try {
            eth = Ethernet.deserializer().deserialize(buffer.array(), 0, buffer.readableBytes());
        } catch (DeserializationException e) {
            return null;
        }
        return eth;
    }
}
