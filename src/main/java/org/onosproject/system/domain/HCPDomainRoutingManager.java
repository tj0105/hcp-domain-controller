package org.onosproject.system.domain;



import com.google.common.collect.Table;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.ARP;
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.domain.HCPDomainTopoService;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.NodeId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.floodlightpof.protocol.OFMatch20;
import org.onosproject.floodlightpof.protocol.OFPortStatus;
import org.onosproject.floodlightpof.protocol.table.OFFlowTable;
import org.onosproject.floodlightpof.protocol.table.OFFlowTableResource;
import org.onosproject.floodlightpof.protocol.table.OFTableType;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPPacketInVer10;
import org.onosproject.hcp.types.HCPVport;
import org.onosproject.hcp.types.IPv4Address;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.*;
import org.onosproject.net.table.*;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.pof.controller.Dpid;
import org.onosproject.pof.controller.PofController;
import org.onosproject.pof.controller.PofSwitchListener;
import org.onosproject.pof.controller.RoleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * @Author ldy
 * @Date: 20-3-3 下午11:40
 * @Version 1.0
 */
@Component(immediate = true)
public class HCPDomainRoutingManager {
    private static final Logger log= LoggerFactory.getLogger(HCPDomainRoutingManager.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainController domainController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainTopoService domainTopoService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableStore tableStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableService flowTableService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EdgePortService edgeService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PofController pofController;

    private PacketProcessor packetProcessor=new ReactivePacketProcessor();
    private HCPSuperMessageListener hcpSuperMessageListener=new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener=new InternalHCPSuperControllerListener();
    private PofSwitchListener pofSwitchListener=new InternalDeviceListener();
    public final short SIP=12;


    private ApplicationId applicationId;
    private NodeId local;
    private HCPVersion hcpVersion;
    private HCPFactory hcpfactory;
    private Map<DeviceId,Integer> TableIDMap;

    private boolean flag=false;
    @Activate
    public void activate(){
        applicationId=coreService.registerApplication("org.onosproject.domain.system");
        domainController.addHCPSuperControllerListener(hcpSuperControllerListener);
        pofController.addListener(pofSwitchListener);
        init();

        log.info("=======================HCP Domain Routing Manager================");
    }

    @Deactivate
    public void deactivate(){
        for (DeviceId deviceId:TableIDMap.keySet()){
            reMoveFlowTable(deviceId,TableIDMap.get(deviceId));
        }
        TableIDMap.clear();
        if (!flag){
            return;
        }
        pofController.removeListener(pofSwitchListener);
        domainController.removeHCPSuperControllerListener(hcpSuperControllerListener);
        packetService.removeProcessor(packetProcessor);
        domainController.removeMessageListener(hcpSuperMessageListener);
        log.info("=======================HCP Domain Routing Manager Stopped");
    }
    public void setUp(){
        flag=true;
        hcpVersion=domainController.getHCPVersion();
        hcpfactory=HCPFactories.getFactory(hcpVersion);
        domainController.addMessageListener(hcpSuperMessageListener);
        packetService.addProcessor(packetProcessor,PacketProcessor.director(4));
    }

    public void init(){
        TableIDMap=new HashMap<>();
        for (Device device:deviceService.getAvailableDevices()){
              DeviceId deviceId=device.id();
              int tableId=sendPofFlowTables(deviceId,"FirstEntryTable");
              TableIDMap.put(deviceId,tableId);

        }
    }

    public void changePorts(DeviceId deviceId) {
        if (deviceId.toString().split(":")[0].equals("pof")) {
            for (Port port:deviceService.getPorts(deviceId)) {
                if (!port.annotations().value(AnnotationKeys.PORT_NAME).equals("eth0")) {
                    deviceService.changePortState(deviceId, port.number(), true);
                }
            }
        }
    }

    public int sendPofFlowTables(DeviceId deviceId,String tableName){
        byte globeTableId=(byte) tableStore.getNewGlobalFlowTableId(deviceId, OFTableType.OF_MM_TABLE);
        int tableId=tableStore.parseToSmallTableId(deviceId,globeTableId);

        OFMatch20 srcIp=new OFMatch20();
        srcIp.setFieldId((short)SIP);
        srcIp.setFieldName("srcIp");
        srcIp.setOffset((short)208);
        srcIp.setLength((short)32);

        ArrayList<OFMatch20> match20ArrayList=new ArrayList<>();
        match20ArrayList.add(srcIp);

        OFFlowTable ofFlowTable=new OFFlowTable();
        ofFlowTable.setTableId((byte)tableId);
        ofFlowTable.setTableName(tableName);
        ofFlowTable.setMatchFieldList(match20ArrayList);
        ofFlowTable.setMatchFieldNum((byte)1);
        ofFlowTable.setTableSize(128);
        ofFlowTable.setTableType(OFTableType.OF_MM_TABLE);
        ofFlowTable.setCommand(null);
        ofFlowTable.setKeyLength((short)32);

        FlowTable.Builder flowTable= DefaultFlowTable.builder()
                .withFlowTable(ofFlowTable)
                .forTable(tableId)
                .forDevice(deviceId)
                .fromApp(applicationId);

        flowTableService.applyFlowTables(flowTable.build());

        log.info("table<{}> applied to device<{}> successfully.", tableId, deviceId.toString());

        return tableId;
    }

    public void reMoveFlowTable(DeviceId deviceId,int tableId){
//        flowRuleService.removeFlowRulesById(applicationId);
        log.info("++++ before removeFlowTablesByTableId: {}", tableId);
        flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(tableId));

    }
    private void PacketOut(Ip4Address ip4Address,Ethernet ethernet){
        Set<Host> hosts=hostService.getHostsByIp(ip4Address);
        if (hosts!=null||hosts.size()>0){
            Host dstHost=(Host) hosts.toArray()[0];
            PacketOut(dstHost.location(),ethernet);
        }
        else{
            floodPacketOut(ethernet);
        }
    }
    private void floodPacketOut(Ethernet ethernet){
        TrafficTreatment.Builder builder = null;
        for (ConnectPoint connectPoint:edgeService.getEdgePoints()){
            if (!domainTopoService.isOuterPort(connectPoint)){
                builder=DefaultTrafficTreatment.builder();
                builder.setOutput(connectPoint.port());
                packetService.emit(new DefaultOutboundPacket(connectPoint.deviceId(),builder.build()
                                        ,ByteBuffer.wrap(ethernet.serialize())));
            }

        }
    }
    private void PacketOut(ConnectPoint hostLocation,Ethernet ethernet){
        TrafficTreatment.Builder builder= DefaultTrafficTreatment.builder();
        builder.setOutput(hostLocation.port());
        packetService.emit(new DefaultOutboundPacket(hostLocation.deviceId(),builder.build(), ByteBuffer.wrap(ethernet.serialize())));
        return ;
    }
    private void processPacketOut(PortNumber portNumber,Ethernet ethernet){
        if (portNumber==null){
            return ;
        }
        if (portNumber.toLong()== HCPVport.LOCAL.getPortNumber()){
            if (ethernet.getEtherType()==Ethernet.TYPE_ARP){
                ARP arp=(ARP)ethernet.getPayload();
                PacketOut(Ip4Address.valueOf(arp.getTargetProtocolAddress()),ethernet);
            }
        }else {
            floodPacketOut(ethernet);
        }

    }
    private class ReactivePacketProcessor implements PacketProcessor{

        @Override
        public void process(PacketContext packetContext) {
            if (packetContext.isHandled()){
                return;
            }
            Ethernet ethernet=packetContext.inPacket().parsed();
            if (ethernet == null || ethernet.getEtherType() == Ethernet.TYPE_LLDP) {
                return;
            }
            PortNumber dstPort=packetContext.inPacket().receivedFrom().port();
            DeviceId dstDeviceId=packetContext.inPacket().receivedFrom().deviceId();
            ConnectPoint connectPoint=new ConnectPoint(dstDeviceId,dstPort);
            IpAddress targetAddress;

            if (ethernet.getEtherType()==Ethernet.TYPE_ARP){
                targetAddress= Ip4Address.valueOf(((ARP)ethernet.getPayload()).getTargetProtocolAddress());
            }
            else {
                return ;
            }
            Set<Host> hosts=hostService.getHostsByIp(targetAddress);
            if (hosts !=null && hosts.size()>0){
                return ;
            }
            //构建packetIn数据包发送给上层控制器
            byte []frames=ethernet.serialize();
            HCPPacketIn hcpPacketIn= HCPPacketInVer10.of((int)domainTopoService.getLogicalVportNumber(connectPoint).toLong(),frames);
            Set<HCPSbpFlags> flagsSet = new HashSet<>();
            flagsSet.add(HCPSbpFlags.DATA_EXITS);
            HCPSbp hcpSbp=hcpfactory.buildSbp()
                    .setSbpCmpType(HCPSbpCmpType.PACKET_IN)
                    .setFlags(flagsSet)
                    .setDataLength((short)hcpPacketIn.getData().length)
                    .setSbpXid(1)
                    .setSbpCmpData(hcpPacketIn)
                    .build();
            domainController.write(hcpSbp);
            packetContext.block();
        }
    }
    private class InternalHCPSuperMessageListener implements HCPSuperMessageListener{

        @Override
        public void handleIncommingMessage(HCPMessage message) {
                if (message.getType()!= HCPType.HCP_SBP){
                    return ;
                }
                HCPSbp hcpSbp=(HCPSbp) message;
                switch (hcpSbp.getSbpCmpType()){
                    case PACKET_OUT:
                         HCPPacketOut hcpPacketOut=(HCPPacketOut)hcpSbp.getSbpCmpData();
                         PortNumber portNumber =PortNumber.portNumber(hcpPacketOut.getOutPort());
                         Ethernet ethernet=domainController.parseEthernet(hcpPacketOut.getData());
                         log.info("==========PACKET_OUT======{}===",(ARP)ethernet.getPayload());
                         processPacketOut(portNumber,ethernet);
                         break;
                    default:
                        return;
                }
                return;

        }

        @Override
        public void handleOutGoingMessage(List<HCPMessage> messages) {

        }
    }

    private class InternalHCPSuperControllerListener implements HCPSuperControllerListener{

        @Override
        public void connectToSuperController(HCPSuper hcpSuper) {
             log.info("1111111111111111111111111111111111");
             setUp();
        }

        @Override
        public void disconnectSuperController(HCPSuper hcpSuper) {

        }
    }

    private class InternalDeviceListener implements PofSwitchListener{

        @Override
        public void switchAdded(Dpid dpid) {

        }

        @Override
        public void hanndleConnectionUp(Dpid dpid) {
            DeviceId deviceId=DeviceId.deviceId(Dpid.uri(dpid));
            int tableId=sendPofFlowTables(deviceId,"FirstEntryTable");
            TableIDMap.put(deviceId,tableId);
        }

        @Override
        public void switchRemoved(Dpid dpid) {
            DeviceId deviceId=DeviceId.deviceId(Dpid.uri(dpid));
            log.info("=================Dpid name======{}========",deviceId);
            TableIDMap.remove(deviceId);
        }

        @Override
        public void switchChanged(Dpid dpid) {

        }

        @Override
        public void portChanged(Dpid dpid, OFPortStatus ofPortStatus) {

        }

        @Override
        public void setTableResource(Dpid dpid, OFFlowTableResource ofFlowTableResource) {

        }

        @Override
        public void receivedRoleReply(Dpid dpid, RoleState roleState, RoleState roleState1) {

        }
    }
}
