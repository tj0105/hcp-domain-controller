package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onlab.packet.*;
import org.onlab.packet.MacAddress;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.*;
import org.onosproject.hcp.types.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    @Test
    public void SbpForwardRequestTest() throws HCPParseError{
        System.out.println(IPv4Address.FULL_MASK);
        IpAddress ipAddress=IpAddress.valueOf("192.168.109.13");
        IPv4Address srcipAddress=IPv4Address.of(ipAddress.toString());
        IPv4Address dstIpaddress=IPv4Address.of("192.168.109.14");
        ChannelBuffer buffer=ChannelBuffers.dynamicBuffer();
        List<HCPVportHop> vportHops=new ArrayList<>();
        HCPVportHop vportHop=HCPVportHop.of(HCPVport.ofShort((short)1),0);
//        HCPVportHop vportHop2=HCPVportHop.of(HCPVport.ofShort((short)2),6);
        vportHops.add(vportHop);
//        vportHops.add(vportHop2);
        HCPFlowType hcpFlowType = HCPFlowType.HCP_HOST;
        HCPFlowType hcpFlowType1 = HCPFlowType.HCP_IOT;
        HCPIOTID srcIoTId = HCPIOTID.of("ABCDEFG12345");
        HCPIOTID dstIoTId = HCPIOTID.of("12345678ABC");
        HCPIOT srcIoT = HCPIOT.of(HCPIoTType.IOT_EPC,srcIoTId,HCPIoTState.ACTIVE);
        HCPIOT dstIoT = HCPIOT.of(HCPIoTType.IOT_EPC,dstIoTId,HCPIoTState.ACTIVE);

        HCPForwardingRequestVer10 forwardingRequestVer10=HCPForwardingRequestVer10
                .of(hcpFlowType,srcipAddress,dstIpaddress,10,(short)2,(byte)6,vportHops);
        HCPForwardingRequestVer10 forwardingRequestVer11=HCPForwardingRequestVer10
                .of(hcpFlowType1,srcIoT,dstIoT,10,(short)2,(byte)6,vportHops);
        System.out.println(forwardingRequestVer11.toString());
        System.out.println(forwardingRequestVer10.toString());
        Set<HCPSbpFlags> flagsSet=new HashSet<>();
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        HCPSbp hcpSbp =getMessageFactry().buildSbp()
                .setFlags(flagsSet)
                .setSbpCmpType(HCPSbpCmpType.FLOW_FORWARDING_REQUEST)
                .setDataLength((short)forwardingRequestVer11.getData().length)
                .setSbpCmpData(forwardingRequestVer11)
                .setSbpXid(1)
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
        HCPForwardingRequest hcpForwardingRequest=(HCPForwardingRequest) messageRev.getSbpCmpData();
        System.out.println("hcpforwarding request flowType:"+hcpForwardingRequest.getFLowType());
        System.out.println("hcpforwarding request dstIot:"+hcpForwardingRequest.getDstIoT());
        System.out.println("hcpforwarding request srcIot:"+hcpForwardingRequest.getSrcIoT());
        System.out.println("hcpforwarding request dstIpaddress:"+hcpForwardingRequest.getDstIpAddress());
        System.out.println("hcpforwarding request srcIpaddress:"+hcpForwardingRequest.getSrcIpAddress());
        System.out.println("hcpforwarding request type:"+hcpForwardingRequest.getEthType());
        System.out.println("hcpforwarding request qos:"+hcpForwardingRequest.getQos());
        List<HCPVportHop> vportHops1=hcpForwardingRequest.getvportHopList();
        vportHops1.forEach(hcpVportHop -> {
            System.out.println("vportNumber:"+hcpVportHop.getVport().getPortNumber()+","+"hop:"+hcpVportHop.getHop());

        });

    }

    @Test
    public void SbpReourceRequestTest() throws HCPParseError {
        IPv4Address srcipAddress=IPv4Address.of("192.168.109.12");
        IPv4Address dstIpaddress=IPv4Address.of("192.168.109.13");
        Set<HCPConfigFlags> set=new HashSet<>();
        set.add(HCPConfigFlags.CAPABILITIES_HOP);
        ChannelBuffer buffer=ChannelBuffers.dynamicBuffer();
        HCPResourceRequestVer10 resourceRequestVer10=HCPResourceRequestVer10
                .of(srcipAddress,dstIpaddress,set);
        Set<HCPSbpFlags> flagsSet=new HashSet<>();
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        HCPSbp hcpSbp =getMessageFactry().buildSbp()
                .setFlags(flagsSet)
                .setSbpCmpType(HCPSbpCmpType.RESOURCE_REQUEST)
                .setDataLength((short)resourceRequestVer10.getData().length)
                .setSbpCmpData(resourceRequestVer10)
                .setSbpXid(1)
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
        HCPResourceRequest hcpResourceRequest=(HCPResourceRequest) messageRev.getSbpCmpData();
        System.out.println("hcpforwarding request dstIpaddress:"+hcpResourceRequest.getDstIpAddress().toString());
        System.out.println("hcpforwarding request srcIpaddress:"+hcpResourceRequest.getSrcIpAddress());
//        System.out.println("hcpforwarding request type:"+(Ethernet.TYPE_IPV4==hcpForwardingRequest.getEthType()));
        System.out.println("hcpforwarding request configFlags:"+hcpResourceRequest.getFlags());
//        List<HCPVportHop> vportHops1=hcpForwardingRequest.getvportHopList();
//        vportHops1.forEach(hcpVportHop -> {
//            System.out.println("vportNumber:"+hcpVportHop.getVport().getPortNumber()+","+"hop:"+hcpVportHop.getHop());
//
//        });

    }

    @Test
    public void SbpForwardingReplyTest() throws HCPParseError {
        ConcurrentHashMap<IpAddress,Map<HCPVport,String>> mapConcurrentHashMap=new ConcurrentHashMap<>();
        ChannelBuffer buffer=ChannelBuffers.dynamicBuffer();
        IPv4Address srcipAddress=IPv4Address.of("192.168.109.12");
        IPv4Address dstIpaddress=IPv4Address.of("192.168.109.13");
        IpAddress srcIp=IpAddress.valueOf(srcipAddress.toString());
        Map<HCPVport,String> map=mapConcurrentHashMap.get(srcIp);
        if (map==null){
            map=new HashMap<>();
            mapConcurrentHashMap.put(srcIp,map);
        }
        map.put(HCPVport.ofShort((short)1),"ssss");
        map.put(HCPVport.ofShort((short)1),"ssssSSS");

        System.out.println(mapConcurrentHashMap.toString());
        HCPVport  srcvport=HCPVport.IN_PORT;
//        System.out.println(srcvport.equals(HCPVport.OUT_PORT));
        HCPVport  dstpport=HCPVport.ofShort((short)1);
        HCPFlowType hcpFlowType =HCPFlowType.HCP_HOST;
        HCPFlowType hcpFlowType1 = HCPFlowType.HCP_IOT;
        HCPIOTID srcIoTId = HCPIOTID.of("ABCDEFG12345");
        HCPIOTID dstIoTId = HCPIOTID.of("12345678ABC");
        HCPIOT srcIoT = HCPIOT.of(HCPIoTType.IOT_EPC,srcIoTId,HCPIoTState.ACTIVE);
        HCPIOT dstIoT = HCPIOT.of(HCPIoTType.IOT_EPC,dstIoTId,HCPIoTState.ACTIVE);
        HCPForwardingReply forwardingReply=HCPForwardingReplyVer10
                .of(hcpFlowType,srcipAddress,dstIpaddress,srcvport,dstpport,Ethernet.TYPE_IPV4,(byte)6);
        HCPForwardingReply forwardingReply1=HCPForwardingReplyVer10
                .of(hcpFlowType1,srcIoT,dstIoT,srcvport,dstpport,Ethernet.TYPE_IPV4,(byte)6);
        System.out.println(forwardingReply1.toString());
        System.out.println(forwardingReply.toString());
        Set<HCPSbpFlags> flagsSet=new HashSet<>();
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        HCPSbp hcpSbp =getMessageFactry().buildSbp()
                .setFlags(flagsSet)
                .setSbpCmpType(HCPSbpCmpType.FLOW_FORWARDING_REPLY)
                .setDataLength((short)forwardingReply1.getData().length)
                .setSbpCmpData(forwardingReply1)
                .setSbpXid(1)
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
        HCPForwardingReply hcpForwardingReply=(HCPForwardingReply) messageRev.getSbpCmpData();
        System.out.println("hcpforwarding request flowType:"+hcpForwardingReply.getFLowType());
        System.out.println("hcpforwarding request dstIpaddress:"+hcpForwardingReply.getDstIpAddress());
        System.out.println("hcpforwarding request srcIpaddress:"+hcpForwardingReply.getSrcIpAddress());
        System.out.println("hcpforwarding request dstIoT:"+hcpForwardingReply.getDstIoT());
        System.out.println("hcpforwarding request srcIoT:"+hcpForwardingReply.getSrcIoT());
        System.out.println("hcpforwarding request type:"+(Ethernet.TYPE_IPV4==hcpForwardingReply.getEthType()));
        System.out.println("hcpforwarding request qos:"+hcpForwardingReply.getQos());
        System.out.println("hcpforwardint reply srcVPort:"+hcpForwardingReply.getSrcVport());
        System.out.println("hcpforwardint reply dstVPort:"+hcpForwardingReply.getDstVport());
        HCPVport dstVport=hcpForwardingReply.getDstVport();
//        System.out.println(HCPVport.ofShort((short)1).equals(dstpport));
//        if (!mapConcurrentHashMap.containsKey(IpAddress.valueOf(hcpForwardingReply.getSrcIpAddress().toString()))){
//            System.out.println("null");
//        }
//        Map<HCPVport,String> stringMap=mapConcurrentHashMap.get(IpAddress.valueOf(hcpForwardingReply.getSrcIpAddress().toString()));
//        if (!stringMap.containsKey(dstVport)){
//            System.out.println("null1");
//        }
//        System.out.println(stringMap.get(hcpForwardingReply.getDstVport()));

    }
    @Test
    public void SbpResourceReplyTest() throws HCPParseError {
        IPv4Address srcipAddress=IPv4Address.of("192.168.109.12");
        IPv4Address dstIpaddress=IPv4Address.of("192.168.109.13");
        ChannelBuffer buffer=ChannelBuffers.dynamicBuffer();
        List<HCPVportHop> vportHops=new ArrayList<>();
        HCPVportHop vportHop=HCPVportHop.of(HCPVport.ofShort((short)1),3);
        HCPVportHop vportHop2=HCPVportHop.of(HCPVport.ofShort((short)2),6);
        vportHops.add(vportHop);
        vportHops.add(vportHop2);
        HCPResourceReplyVer10 resourceReplyVer10=HCPResourceReplyVer10
                .of(dstIpaddress,vportHops);
        Set<HCPSbpFlags> flagsSet=new HashSet<>();
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        HCPSbp hcpSbp =getMessageFactry().buildSbp()
                .setFlags(flagsSet)
                .setSbpCmpType(HCPSbpCmpType.RESOURCE_REPLY)
                .setDataLength((short)resourceReplyVer10.getData().length)
                .setSbpCmpData(resourceReplyVer10)
                .setSbpXid(1)
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
        HCPResourceReply hcpResourceRequest=(HCPResourceReply) messageRev.getSbpCmpData();
        System.out.println("hcpforwarding request dstIpaddress:"+hcpResourceRequest.getDstIpAddress().toString());
//        System.out.println("hcpforwarding request srcIpaddress:"+hcpResourceRequest.getSrcIpAddress());
//        System.out.println("hcpforwarding request type:"+(Ethernet.TYPE_IPV4==hcpForwardingRequest.getEthType()));

        List<HCPVportHop> vportHops1=hcpResourceRequest.getvportHopList();
        vportHops1.forEach(hcpVportHop -> {
            System.out.println("vportNumber:"+hcpVportHop.getVport().getPortNumber()+","+"hop:"+hcpVportHop.getHop());

        });

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
