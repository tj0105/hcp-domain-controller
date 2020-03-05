package org.onosproject.system.domain;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.Ethernet;
import org.onlab.packet.HCPLLDP;
import org.onlab.packet.MacAddress;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.domain.HCPDomainTopoService;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.cluster.ClusterMetadataService;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPPacketInVer10;
import org.onosproject.hcp.protocol.ver10.HCPVportDescriptionVer10;
import org.onosproject.hcp.types.HCPVport;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.link.ProbedLinkProvider;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @Author ldy
 * @Date: 20-3-3 下午9:31
 * @Version 1.0
 */
@Component(immediate = true)
@Service
public class HCPDomainTopologyManager implements HCPDomainTopoService{
    private static final Logger log= LoggerFactory.getLogger(HCPDomainTopologyManager.class);

    private HCPVersion hcpVersion;
    private HCPFactory hcpFactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainController domainController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterMetadataService clusterMetadataService;


    private PacketProcessor hcplldpPacketProcesser=new InternalPacketProcessor();

    private LinkListener linkListener=new InternalLinkListener();
    private HCPSuperMessageListener hcpSuperMessageListener=new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener=new InternalHCPSuperControllerListener();

    private AtomicLong vportNumber =new AtomicLong(1);
    private Map<ConnectPoint,PortNumber> vportMap=new HashMap<>();

    private final static int LLDP_VPORT_LOCAL=0xffff;

    @Activate
    public void activate(){
        hcpVersion=domainController.getHCPVersion();
        hcpFactory= HCPFactories.getFactory(hcpVersion);
        domainController.addMessageListener(hcpSuperMessageListener);
        domainController.addHCPSuperControllerListener(hcpSuperControllerListener);
        linkService.addListener(linkListener);
        packetService.addProcessor(hcplldpPacketProcesser,PacketProcessor.director(0));
        log.info("==============Domain Topology Manager Start===================");
    }

    @Deactivate
    public void deactivate(){
        linkService.removeListener(linkListener);
        domainController.removeMessageListener(hcpSuperMessageListener);
        domainController.removeHCPSuperControllerListener(hcpSuperControllerListener);
        packetService.removeProcessor(hcplldpPacketProcesser);
        vportMap.clear();
        log.info("==============Domain Topology Manager Stopped===================");

    }

    private void addOrUpdateVport(ConnectPoint connectPoint){
        if (vportMap.containsKey(connectPoint)){
            return ;
        }
        else{
            long alllocatedVportNumber=vportNumber.getAndIncrement();
            vportMap.put(connectPoint,PortNumber.portNumber(alllocatedVportNumber));
            HCPVport vport=HCPVport.ofShort((short)alllocatedVportNumber);
            Set<HCPVportState> state=new HashSet<>();
            state.add(HCPVportState.LINK_UP);
            HCPVportDescribtion vportDescribtion=new HCPVportDescriptionVer10
                    .Builder().setPortNo(vport)
                    .setState(state).build();
            HCPVportStatus message=hcpFactory.buildVportStatus()
                    .setReson(HCPVportReason.ADD)
                    .setVportDescribtion(vportDescribtion)
                    .build();
            domainController.write(message);
        }
    }



    @Override
    public PortNumber getLogicalVportNumber(ConnectPoint connectPoint) {
        return vportMap.containsKey(connectPoint)?vportMap.get(connectPoint):PortNumber.portNumber(HCPVport.LOCAL.getPortNumber());
    }

    @Override
    public boolean isOuterPort(ConnectPoint connectPoint) {
        return vportMap.containsKey(connectPoint);
    }

    @Override
    public ConnectPoint getLocationByVport(PortNumber portNumber) {
        for (ConnectPoint connectPoint:vportMap.keySet()){
            if (vportMap.get(connectPoint).equals(portNumber)){
                return connectPoint;
            }
        }
        return null;
    }

    private final String buildSrcMac(){
        String srcMac= ProbedLinkProvider.fingerprintMac(clusterMetadataService.getClusterMetadata());
        String defaultMac=ProbedLinkProvider.defaultMac();
        if (srcMac.equals(defaultMac)){
            log.warn("Could not generate fringeprint,Use default value {} ",defaultMac);
            return defaultMac;
        }
        log.trace("Generate MAC Address {}",srcMac);
        return srcMac;
    }

    private class InternalLinkListener implements LinkListener{

        @Override
        public void event(LinkEvent linkEvent) {

        }
    }

    private class InternalHCPSuperMessageListener implements HCPSuperMessageListener{

        @Override
        public void handleIncommingMessage(HCPMessage message) {
            if (message.getType()!=HCPType.HCP_TOPO_REQUEST)
                return ;
        }

        @Override
        public void handleOutGoingMessage(List<HCPMessage> messages) {

        }
    }

    private class InternalHCPSuperControllerListener implements HCPSuperControllerListener{

        @Override
        public void connectToSuperController(HCPSuper hcpSuper) {
            return ;
        }

        @Override
        public void disconnectSuperController(HCPSuper hcpSuper) {

        }
    }

    private class InternalPacketProcessor implements PacketProcessor{

        @Override
        public void process(PacketContext packetContext) {
            if (packetContext.isHandled()){
                return;
            }
            Ethernet eth=packetContext.inPacket().parsed();
            if (eth==null ||(eth.getEtherType()!=Ethernet.TYPE_LLDP)){
                return ;
            }
            if (!domainController.isConnectToSuper()){
                return ;
            }
            HCPLLDP hcplldp=HCPLLDP.parseHCPLLDP(eth);
            if (hcplldp==null){
                return ;
            }
            PortNumber srcPort=PortNumber.portNumber(hcplldp.getPortNum());
            PortNumber dstPort=packetContext.inPacket().receivedFrom().port();
            DeviceId srcDeviceId=DeviceId.deviceId("of:"+hcplldp.getDpid());
            DeviceId dstDeviceId=packetContext.inPacket().receivedFrom().deviceId();
            ConnectPoint edgeConnectPoint=new ConnectPoint(dstDeviceId,dstPort);

            if (hcplldp.getDomianId()==domainController.getDomainId().getLong()){
                packetContext.block();
                return;
            }

            if (LLDP_VPORT_LOCAL==hcplldp.getVportNum()){
                addOrUpdateVport(edgeConnectPoint);
                HCPLLDP replyhcplldp=HCPLLDP.hcplldp(Long.valueOf(dstDeviceId.toString().substring("of:".length())),
                        Long.valueOf(dstPort.toLong()).intValue(),
                        domainController.getDomainId().getLong(),
                        Long.valueOf(getLogicalVportNumber(edgeConnectPoint).toLong()).intValue());
                Ethernet ethpacket=new Ethernet();
                ethpacket.setEtherType(Ethernet.TYPE_LLDP);
                ethpacket.setDestinationMACAddress(MacAddress.ONOS_LLDP);
                ethpacket.setPad(true);
                ethpacket.setSourceMACAddress(buildSrcMac());
                ethpacket.setPayload(replyhcplldp);
                OutboundPacket outboundPacket=new DefaultOutboundPacket(dstDeviceId
                                        ,builder().setOutput(dstPort).build(), ByteBuffer.wrap(ethpacket.serialize()));
                packetService.emit(outboundPacket);
                packetContext.block();
            }else{
                HCPLLDP sbpHCPlldp=HCPLLDP.hcplldp(hcplldp.getDomianId(),
                        hcplldp.getVportNum(),domainController.getDomainId().getLong(),
                        hcplldp.getVportNum());
                Ethernet ethpacket=new Ethernet();
                ethpacket.setEtherType(Ethernet.TYPE_LLDP);
                ethpacket.setDestinationMACAddress(MacAddress.ONOS_LLDP);
                ethpacket.setPad(true);
                ethpacket.setSourceMACAddress(buildSrcMac());
                ethpacket.setPayload(sbpHCPlldp);

                byte [] frame=ethpacket.serialize();
                HCPPacketIn hcpPacketIn= HCPPacketInVer10.of((int)getLogicalVportNumber(edgeConnectPoint).toLong(),frame);

                ChannelBuffer buffer= ChannelBuffers.dynamicBuffer();
                hcpPacketIn.writeTo(buffer);
                byte []data=new byte[buffer.readableBytes()];
                buffer.readBytes(data,0,buffer.readableBytes());
                HCPSbp hcpSbp=hcpFactory.buildSbp()
                        .setSbpCmpType(HCPSbpCmpType.PACKET_IN)
                        .setSbpCmpData(hcpPacketIn)
                        .build();
                domainController.write(hcpSbp);
                packetContext.block();
            }
        }
    }
}
