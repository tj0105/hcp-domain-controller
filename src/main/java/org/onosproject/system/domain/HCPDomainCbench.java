package org.onosproject.system.domain;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ethernet;

import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.floodlightpof.protocol.action.OFAction;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPForwardingRequestVer10;
import org.onosproject.hcp.types.HCPVportHop;
import org.onosproject.hcp.types.IPv4Address;
import org.onosproject.net.flow.instructions.DefaultPofActions;
import org.onosproject.net.flow.instructions.DefaultPofInstructions;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Author ldy
 * @Date: 20-6-8 下午10:06
 * @Version 1.0
 */
@Component(immediate =true )
public class HCPDomainCbench {

    private static final Logger log= LoggerFactory.getLogger(HCPDomainCbench.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainController domainController;
    private HCPSuperControllerListener hcpSuperControllerListener = new InternalHCPSuperControllerListener();
    private HCPSuperMessageListener hcpSuperMessageListener = new InternalHCPSuperMessageListener();
    private PacketProcessor packetProcessor=new InternalPacketProcessor();
    private HCPFactory hcpfactory;
    private HCPVersion hcpVersion;
    private double sum_packet=0;
    private PacketContext Packet=null;
    private Set<HCPSbpFlags> flagsSet = new HashSet<>();
    private ApplicationId applicationId;
    private IPv4Address srcIp=null;
    private IPv4Address dstIp=null;
    private List<HCPVportHop> vportHops;
    private int sum=0;
    private static final int PACKET_BUFFER = 5000000;
    private BlockingDeque<PacketContext> handleQueue =
            new LinkedBlockingDeque<>(PACKET_BUFFER);
//    private Queue<PacketContext> contextQueue=new LinkedList<>();
    @Activate
    public void activate() {
        applicationId = coreService.registerApplication("org.onosproject.domain.system");
       domainController.addHCPSuperControllerListener(hcpSuperControllerListener);
        vportHops=new ArrayList<>();
    }

    @Deactivate
    public void deactivate() {
        log.info("======================sum==process flow_reply====={}",sum);
        domainController.removeHCPSuperControllerListener(hcpSuperControllerListener);
        domainController.removeMessageListener(hcpSuperMessageListener);
        packetService.removeProcessor(packetProcessor);
//        contextQueue.clear();
        handleQueue.clear();
        vportHops.clear();
        log.info("================HCPDomainCbench Stop=================");
    }

    public void setUp(){
        hcpVersion=domainController.getHCPVersion();
        hcpfactory=HCPFactories.getFactory(hcpVersion);
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        domainController.addMessageListener(hcpSuperMessageListener);
        srcIp=IPv4Address.of("10.0.0.1");
        dstIp=IPv4Address.of("10.0.0.2");
        vportHops=new ArrayList<>();
        packetService.addProcessor(packetProcessor, PacketProcessor.director(1));
        log.info("==============HCPDomainCbench Start===================");

    }
    public void SendFlowRequestToSuper(){
        HCPForwardingRequest hcpForwardingRequest= HCPForwardingRequestVer10.
                of(srcIp,dstIp,1,Ethernet.TYPE_IPV4,(byte)3,vportHops);
        HCPSbp hcpSbp=hcpfactory.buildSbp()
                .setSbpCmpType(HCPSbpCmpType.FLOW_FORWARDING_REQUEST)
                .setFlags(flagsSet)
                .setDataLength((short)hcpForwardingRequest.getData().length)
                .setSbpCmpData(hcpForwardingRequest)
                .setSbpXid(1)
                .build();
        domainController.write(hcpSbp);

    }
    public void packetOut(PacketContext packetContext,short port){
        List<OFAction> actions = new ArrayList<>();
        actions.add(DefaultPofActions.output((short)0, (short)0, (short)0, port).action());
        packetContext.treatmentBuilder().add(DefaultPofInstructions.applyActions(actions));
        packetContext.send();
    }
    private class InternalPacketProcessor implements PacketProcessor{

        @Override
        public void process(PacketContext packetContext) {
            if (packetContext.isHandled()){
                return;
            }
            InboundPacket pkt=packetContext.inPacket();
            Ethernet ethernet=pkt.parsed();
            if (ethernet==null){
                return ;
            }
//            if (sum==0){
//
//            }
            sum_packet++;
            if (sum_packet>0){

                if (handleQueue.size()>PACKET_BUFFER){
                    return;
                }
//                contextQueue.offer(packetContext);
                handleQueue.offer(packetContext);
                SendFlowRequestToSuper();
                return ;
            }
            packetOut(packetContext,(short)2);
        }
    }

    private class InternalHCPSuperMessageListener implements HCPSuperMessageListener{

        @Override
        public void handleIncommingMessage(HCPMessage message) {
            if(message.getType()!= HCPType.HCP_SBP){
                return ;
            }
            sum++;
//            packetOut(contextQueue.poll(),(short)2);
            packetOut(handleQueue.poll(),(short)2);

        }

        @Override
        public void handleOutGoingMessage(List<HCPMessage> messages) {

        }
    }
    private class InternalHCPSuperControllerListener implements HCPSuperControllerListener {

        @Override
        public void connectToSuperController(HCPSuper hcpSuper) {
            log.info("44444444444444444444444444444444444");
            setUp();
        }

        @Override
        public void disconnectSuperController(HCPSuper hcpSuper) {

        }
    }
}
