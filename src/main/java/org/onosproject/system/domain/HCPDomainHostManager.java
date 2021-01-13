package org.onosproject.system.domain;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onlab.util.Tools;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.api.domain.HCPDomainHostService;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPIoTStateSerializerVer10;
import org.onosproject.hcp.protocol.ver10.HCPIoTTypeSerializerVer10;
import org.onosproject.hcp.types.*;
import org.onosproject.hcp.types.MacAddress;
import org.onosproject.hcp.util.HexString;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.PortNumber;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author ldy
 * @Date: 20-2-29 下午9:08
 * @Version 1.0
 */
@Component(immediate =true )
@Service
public class HCPDomainHostManager implements HCPDomainHostService {
    private static final Logger log= LoggerFactory.getLogger(HCPDomainHostManager.class);

    private HCPVersion hcpVersion;
    private HCPFactory hcpFactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;


    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainController domainController;

    private PacketProcessor hcpiotPacketProcesser = new ReactivePacketProcessor();

    private HostListener hostListener=new InternalHostListener();
    private HCPSuperMessageListener hcpSuperMessageListener=new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener=new InternalHCPSuperControllerListener();

    private ScheduledExecutorService executorService;

    //store the IOT
    private Map<HCPIOTID,ConnectPoint> hcpiotidConnectPointMap = new HashMap<>();
    private Map<HCPIOTID,HCPIOT> hcpiotidhcpiotMap = new HashMap<>();
    //判断是否 connect SuperController
    private boolean flag=false;
    private final byte IOT_ONLINE = 0x30;
    private final byte IOT_OFFLINE = 0x31;
    private final byte IOT_COMMUNICATE = 0x32;
    @Activate
    public void activate() {
        domainController.addHCPSuperControllerListener(hcpSuperControllerListener);
        log.info("============Domain HostManager started===========");
    }


    @Deactivate
    public void deactivate(){
        domainController.removeHCPSuperControllerListener(hcpSuperControllerListener);
        if (!flag){
            return ;
        }
        if (executorService!=null){
            executorService.shutdown();
        }
        domainController.removeMessageListener(hcpSuperMessageListener);
        hostService.removeListener(hostListener);
        packetService.removeProcessor(hcpiotPacketProcesser);
        log.info("============Domain HostManager stoped===========");
    }

    public void init(){
        flag=true;
        hcpVersion=domainController.getHCPVersion();
        hcpFactory=HCPFactories.getFactory(hcpVersion);
//        executorService= Executors.newSingleThreadScheduledExecutor
//                (Tools.groupedThreads("hcp/hostupdate", "hcp-hostupdate-%d", log));
//        executorService.scheduleAtFixedRate(new HostUpdateTesk(),domainController.getPeriod(),domainController.getPeriod(), TimeUnit.SECONDS);
        domainController.addMessageListener(hcpSuperMessageListener);
        hostService.addListener(hostListener);
        packetService.addProcessor(hcpiotPacketProcesser,1);
        log.info("Host Manager have successful activate");
    }

    private void updateExisHosts(HCPHostRequest hcpHostRequest){
        List<HCPHost> hcpHosts=new ArrayList<>();
        for (Host host:hostService.getHosts())
            hcpHosts.addAll(toHCPHosts(host,HCPHostState.ACTIVE));
        if (hcpHosts.isEmpty()){
            return ;
        }
        sendHostChangeMessage(hcpHosts,hcpHostRequest);
    }

    private void updateHost(Host host){
        sendHostChangeMessage(toHCPHosts(host,HCPHostState.ACTIVE),null);
    }

    private void removeHost(Host host){
        sendHostChangeMessage(toHCPHosts(host,HCPHostState.ACTIVE),null);
    }

    private void sendHostChangeMessage(List<HCPHost> hcpHosts,HCPHostRequest hcpHostRequest){
        HCPMessage hcpMessage=null;
        if (null != hcpHostRequest){
            hcpMessage=hcpFactory.buildHostReply()
                    .setDomainId(domainController.getDomainId())
                    .setHosts(hcpHosts)
                    .build();
        }else{
            hcpMessage =hcpFactory.buildHostUpdate()
                    .setDomainId(domainController.getDomainId())
                    .setHosts(hcpHosts)
                    .build();
        }

        if (!domainController.isConnectToSuper()){
            return;
        }
//        log.info("DomainId: {} host update, num:{}",domainController.getDomainId(),hcpHosts.size());
        domainController.write(hcpMessage);
    }

    private List<HCPHost> toHCPHosts(Host host,HCPHostState hcpHostState){
        List<HCPHost> hosts=new ArrayList<>();
        for (IpAddress ip:host.ipAddresses()){
            IPv4Address iPv4Address=IPv4Address.of(ip.toOctets());
            MacAddress  macAddress=MacAddress.of(host.mac().toBytes());
            HCPHost hcpHost=HCPHost.of(iPv4Address,macAddress, hcpHostState);
            hosts.add(hcpHost);
        }
        return hosts;
    }

    @Override
    public ConnectPoint getConnectionByIoTId(HCPIOTID hcpiotid) {
        return hcpiotidConnectPointMap.get(hcpiotid);
    }

    @Override
    public HCPIOT getHCPIOTByIoTId(HCPIOTID hcpiotid) {
        return hcpiotidhcpiotMap.get(hcpiotid);
    }

    private class InternalHostListener implements HostListener {
        @Override
        public void event(HostEvent event) {
            Host updatedHost = null;
            Host removedHost = null;
            List<HCPHost> hcpHosts = new ArrayList<>();
            switch (event.type()) {
                case HOST_ADDED:
                    updatedHost = event.subject();
                    break;
                case HOST_REMOVED:
                    removedHost = event.subject();
                    break;
                case HOST_UPDATED:
                    updatedHost = event.subject();
                    removedHost = event.prevSubject();
                    break;
                default:
            }
            if (null != removedHost) {
                hcpHosts.addAll(toHCPHosts(removedHost,HCPHostState.INACTIVE));
            }
            if (null != updatedHost) {
                hcpHosts.addAll(toHCPHosts(updatedHost,HCPHostState.ACTIVE));
            }
//            log.info("=============hcpHost======{}",hcpHosts.toString());
            sendHostChangeMessage(hcpHosts,null);
        }
    }

    private class InternalHCPSuperMessageListener implements HCPSuperMessageListener{

        @Override
        public void handleIncommingMessage(HCPMessage message) {
                if (message.getType()!=HCPType.HCP_HOST_REQUEST)
                    return;
                updateExisHosts((HCPHostRequest)message);
        }

        @Override
        public void handleOutGoingMessage(List<HCPMessage> messages) {

        }
    }

    private class InternalHCPSuperControllerListener implements  HCPSuperControllerListener{

        @Override
        public void connectToSuperController(HCPSuper hcpSuper) {
            log.info("2222222222222222222222222222");
            init();
            updateExisHosts(null);
        }

        @Override
        public void disconnectSuperController(HCPSuper hcpSuper) {

        }
    }

    /**
     * Process the IOT online and offline
     */
    private class ReactivePacketProcessor implements PacketProcessor{

        @Override
        public void process(PacketContext packetContext) {
            if (packetContext.isHandled())
                return;
            Ethernet ethernet = packetContext.inPacket().parsed();
            if (ethernet == null || ethernet.getEtherType() == Ethernet.TYPE_LLDP){
                return;
            }
            if (ethernet.getEtherType() != Ethernet.TYPE_IPV4){
                return ;
            }
            ConnectPoint connectPoint = packetContext.inPacket().receivedFrom();
            IPv4 iPv4Packet = (IPv4) ethernet.getPayload();
            String dstIp = Ip4Address.valueOf(iPv4Packet.getDestinationAddress()).toString();
            if (!dstIp.equals("10.0.0.0")){
                return ;
            }
            InboundPacket inboundPacket = packetContext.inPacket();
            IPv4Address srcIp = IPv4Address.of(Ip4Address.valueOf(iPv4Packet.getSourceAddress()).toString());
            byte packet_type = (byte) Integer.parseInt(HexString.parseInboundPacket(inboundPacket,42,1),16);
            byte iot_type = (byte) Integer.parseInt(HexString.parseInboundPacket(inboundPacket,43,1),16);
            if (packet_type == IOT_COMMUNICATE){
                return ;
            }
            String iot_id = null;
            if (iot_type == HCPIoTTypeSerializerVer10.IOT_EPC_VAL){
                iot_id = HexString.parseInboundPacket(inboundPacket,44,22);
            }else if (iot_type == HCPIoTTypeSerializerVer10.IOT_ECODE_VAL){
                iot_id = HexString.parseInboundPacket(inboundPacket,44,18);
            }else if (iot_type == HCPIoTTypeSerializerVer10.IOT_OID_VAL){
                iot_id = HexString.parseInboundPacket(inboundPacket,44,16);
            }else{
                return ;
            }
            HCPIOTID hcpiotid = HCPIOTID.of(iot_id);
            HCPIOT hcpiot = HCPIOT.of(srcIp,HCPIoTTypeSerializerVer10.ofWireValue(iot_type),hcpiotid,HCPIoTStateSerializerVer10.ofWireValue(packet_type));
            if (packet_type == IOT_ONLINE){
                hcpiotidConnectPointMap.put(hcpiotid,connectPoint);
                hcpiotidhcpiotMap.put(hcpiotid,hcpiot);
            }else if(packet_type == IOT_OFFLINE){

                hcpiotidhcpiotMap.remove(hcpiotid);
                hcpiotidConnectPointMap.remove(hcpiotid);
            }else{
                return ;
            }
            HCPIoTReply hcpIoTReply = hcpFactory.buildIoTReply()
                    .setDomainId(domainController.getDomainId())
                    .setIoTs(new ArrayList<HCPIOT>(){{add(hcpiot);}})
                    .build();
            domainController.write(hcpIoTReply);
            packetContext.block();
        }
    }
    class HostUpdateTesk implements Runnable{

        @Override
        public void run() {
            updateExisHosts(null);
        }
    }


}
