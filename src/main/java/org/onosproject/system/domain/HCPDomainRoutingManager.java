package org.onosproject.system.domain;


import com.google.common.collect.Table;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.domain.HCPDomainTopoService;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.NodeId;
import org.onosproject.common.DefaultTopology;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.floodlightpof.protocol.OFMatch20;
import org.onosproject.floodlightpof.protocol.OFPortStatus;
import org.onosproject.floodlightpof.protocol.action.OFAction;
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
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.instructions.DefaultPofActions;
import org.onosproject.net.flow.instructions.DefaultPofInstructions;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.*;
import org.onosproject.net.table.*;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.pof.controller.Dpid;
import org.onosproject.pof.controller.PofController;
import org.onosproject.pof.controller.PofSwitchListener;
import org.onosproject.pof.controller.RoleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ldy
 * @Date: 20-3-3 下午11:40
 * @Version 1.0
 */
@Component(immediate = true)
public class HCPDomainRoutingManager {
    private static final Logger log = LoggerFactory.getLogger(HCPDomainRoutingManager.class);

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableStore flowTableStore;

    private PacketProcessor packetProcessor = new ReactivePacketProcessor();
    private HCPSuperMessageListener hcpSuperMessageListener = new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener = new InternalHCPSuperControllerListener();
    private PofSwitchListener pofSwitchListener = new InternalDeviceListener();
    private DeviceListener deviceListener = new InternalListener();
    public final short SIP = 12;


    private ApplicationId applicationId;
    private NodeId local;
    private HCPVersion hcpVersion;
    private HCPFactory hcpfactory;
    private ConcurrentHashMap<DeviceId, Integer> TableIDMap;
    private static final char[] map = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private boolean flag = false;

    @Activate
    public void activate() {
        applicationId = coreService.registerApplication("org.onosproject.domain.system");
        domainController.addHCPSuperControllerListener(hcpSuperControllerListener);
//        pofController.addListener(pofSwitchListener);
        deviceService.addListener(deviceListener);
        init();
        log.info("=======================HCP Domain Routing Manager================");
    }

    @Deactivate
    public void deactivate() {
        for (DeviceId deviceId : TableIDMap.keySet()) {
            reMoveFlowTable(deviceId, TableIDMap.get(deviceId));
        }
        TableIDMap.clear();
        if (!flag) {
            return;
        }
        deviceService.removeListener(deviceListener);
//        pofController.removeListener(pofSwitchListener);
        domainController.removeHCPSuperControllerListener(hcpSuperControllerListener);
        packetService.removeProcessor(packetProcessor);
        domainController.removeMessageListener(hcpSuperMessageListener);
        log.info("=======================HCP Domain Routing Manager Stopped");
    }

    public void setUp() {
        flag = true;
        hcpVersion = domainController.getHCPVersion();
        hcpfactory = HCPFactories.getFactory(hcpVersion);
        domainController.addMessageListener(hcpSuperMessageListener);
        packetService.addProcessor(packetProcessor, PacketProcessor.director(4));
    }

    public void init() {
        TableIDMap = new ConcurrentHashMap<>();
        for (Device device : deviceService.getAvailableDevices()) {
            DeviceId deviceId = device.id();
            int tableId = sendPofFlowTables(deviceId, "FirstEntryTable");
            TableIDMap.put(deviceId, tableId);

        }
    }

    public void reMoveFlowTable(DeviceId deviceId, int tableId) {
        flowRuleService.removeFlowRulesById(applicationId);
        log.info("++++ before removeFlowTablesByTableId: {}", tableId);
        flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(tableId));

    }

    public int sendPofFlowTables(DeviceId deviceId, String tableName) {
        byte globeTableId = (byte) tableStore.getNewGlobalFlowTableId(deviceId, OFTableType.OF_MM_TABLE);
        int tableId = tableStore.parseToSmallTableId(deviceId, globeTableId);

        OFMatch20 srcIp = new OFMatch20();
        srcIp.setFieldId((short) SIP);
        srcIp.setFieldName("srcIp");
        srcIp.setOffset((short) 208);
        srcIp.setLength((short) 32);

        ArrayList<OFMatch20> match20ArrayList = new ArrayList<>();
        match20ArrayList.add(srcIp);

        OFFlowTable ofFlowTable = new OFFlowTable();
        ofFlowTable.setTableId((byte) tableId);
        ofFlowTable.setTableName(tableName);
        ofFlowTable.setMatchFieldList(match20ArrayList);
        ofFlowTable.setMatchFieldNum((byte) 1);
        ofFlowTable.setTableSize(128);
        ofFlowTable.setTableType(OFTableType.OF_MM_TABLE);
        ofFlowTable.setCommand(null);
        ofFlowTable.setKeyLength((short) 32);

        FlowTable.Builder flowTable = DefaultFlowTable.builder()
                .withFlowTable(ofFlowTable)
                .forTable(tableId)
                .forDevice(deviceId)
                .fromApp(applicationId);

        flowTableService.applyFlowTables(flowTable.build());

        log.info("table<{}> applied to device<{}> successfully.", tableId, deviceId.toString());

        return tableId;
    }

    private void installFlowRule(DeviceId deviceId, int tableId, String srcIp, int port, int pority) {
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> mathchList = new ArrayList<>();
        mathchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIp, "FFFFFFFF"));
        trafficSelector.add(Criteria.matchOffsetLength(mathchList));

        TrafficTreatment.Builder trafficTreatMent = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, port).action();
        actions.add(action_output);
        trafficTreatMent.add(DefaultPofInstructions.applyActions(actions));
        log.info("action_out:{}", action_output);

        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatMent.build())
                .withPriority(pority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

    }

    private void processIpv4InDomain(Host srcHost, Host dstHost, IpAddress srcAddress) {
        DeviceId srcDeviceId = srcHost.location().deviceId();
        DeviceId dstDeviceId = dstHost.location().deviceId();
        String srcIP = IpAddressToHexString(srcAddress).toString();
        if (srcDeviceId.equals(dstDeviceId)) {
            int tableId = TableIDMap.get(srcDeviceId);
            installFlowRule(srcDeviceId, tableId, srcIP, (int) dstHost.location().port().toLong(), 1);
            return;
        }
//        log.info("===========srcDeviceiD:{},dstDeviceId:{}",srcDeviceId,dstDeviceId);
        Topology topology = topologyService.currentTopology();
        DefaultTopology defaultTopology = (DefaultTopology) topology;
//        log.info("==================Topology:{}{}",topology.linkCount(),topology.deviceCount());
        Set<Path> paths = defaultTopology.getPaths(srcDeviceId, dstDeviceId);
//        log.info("===============paths:{}",paths.toString());
        Path path = (Path) paths.toArray()[0];
        for (Link link : path.links()) {
//            log.info("==============link:{}=============",link.toString());
            DeviceId deviceId = link.src().deviceId();
            int tableId = TableIDMap.get(deviceId);
            installFlowRule(deviceId, tableId, srcIP, (int) link.src().port().toLong(), 1);
        }
        int tableID1 = TableIDMap.get(dstDeviceId);
        installFlowRule(dstDeviceId, tableID1, srcIP,
                (int) dstHost.location().port().toLong(), 1);
    }

    private void PacketOut(Ip4Address ip4Address, Ethernet ethernet) {
        Set<Host> hosts = hostService.getHostsByIp(ip4Address);
        if (hosts != null || hosts.size() > 0) {
            Host dstHost = (Host) hosts.toArray()[0];
            PacketOut(dstHost.location(), ethernet);
        } else {
            floodPacketOut(ethernet);

        }
    }


    private void floodPacketOut(Ethernet ethernet) {
        TrafficTreatment.Builder builder = null;
        for (ConnectPoint connectPoint : edgeService.getEdgePoints()) {
            if (!domainTopoService.isOuterPort(connectPoint)) {
                builder = DefaultTrafficTreatment.builder();
                builder.setOutput(connectPoint.port());
                packetService.emit(new DefaultOutboundPacket(connectPoint.deviceId(), builder.build()
                        , ByteBuffer.wrap(ethernet.serialize())));
            }

        }
    }

    private void PacketOut(ConnectPoint hostLocation, Ethernet ethernet) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
        builder.setOutput(hostLocation.port());
        packetService.emit(new DefaultOutboundPacket(hostLocation.deviceId(), builder.build(), ByteBuffer.wrap(ethernet.serialize())));
        return;
    }

    private void processPacketOut(PortNumber portNumber, Ethernet ethernet) {
        if (portNumber == null) {
            return;
        }
        if (portNumber.toLong() == HCPVport.LOCAL.getPortNumber()) {
            if (ethernet.getEtherType() == Ethernet.TYPE_ARP) {
                ARP arp = (ARP) ethernet.getPayload();
                PacketOut(Ip4Address.valueOf(arp.getTargetProtocolAddress()), ethernet);
            }
        } else {
            floodPacketOut(ethernet);
        }

    }

    private void SendFlowRequestToSuper() {


    }

    private class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext packetContext) {
            if (packetContext.isHandled()) {
                return;
            }
            Ethernet ethernet = packetContext.inPacket().parsed();
            if (ethernet == null || ethernet.getEtherType() == Ethernet.TYPE_LLDP) {
                return;
            }
            PortNumber dstPort = packetContext.inPacket().receivedFrom().port();
            DeviceId dstDeviceId = packetContext.inPacket().receivedFrom().deviceId();
            ConnectPoint connectPoint = new ConnectPoint(dstDeviceId, dstPort);
            IpAddress targetAddress = null;
            IpAddress srcAddress = null;
            if (ethernet.getEtherType() == Ethernet.TYPE_ARP) {
                srcAddress = Ip4Address.valueOf(((ARP) ethernet.getPayload()).getSenderProtocolAddress());
                targetAddress = Ip4Address.valueOf(((ARP) ethernet.getPayload()).getTargetProtocolAddress());
            } else if (ethernet.getEtherType() == Ethernet.TYPE_IPV4) {
                srcAddress = Ip4Address.valueOf(((IPv4) ethernet.getPayload()).getSourceAddress());
                targetAddress = Ip4Address.valueOf(((IPv4) ethernet.getPayload()).getDestinationAddress());
                log.info("==============srcAddress:{},targetAddress:{}======", srcAddress.toString(), targetAddress.toString());
            }
            Set<Host> hosts = hostService.getHostsByIp(targetAddress);
            if (hosts != null && hosts.size() > 0) {
                if (ethernet.getEtherType() == Ethernet.TYPE_IPV4) {
                    Set<Host> hosts1 = hostService.getHostsByIp(srcAddress);
                    processIpv4InDomain((Host) hosts1.toArray()[0], (Host) hosts.toArray()[0], srcAddress);
                }
                return;
            }
            //构建packetIn数据包发送给上层控制器
            if (ethernet.getEtherType() == Ethernet.TYPE_ARP) {
                byte[] frames = ethernet.serialize();
                HCPPacketIn hcpPacketIn = HCPPacketInVer10.of((int) domainTopoService.getLogicalVportNumber(connectPoint).toLong(), frames);
                Set<HCPSbpFlags> flagsSet = new HashSet<>();
                flagsSet.add(HCPSbpFlags.DATA_EXITS);
                HCPSbp hcpSbp = hcpfactory.buildSbp()
                        .setSbpCmpType(HCPSbpCmpType.PACKET_IN)
                        .setFlags(flagsSet)
                        .setDataLength((short) hcpPacketIn.getData().length)
                        .setSbpXid(1)
                        .setSbpCmpData(hcpPacketIn)
                        .build();
                domainController.write(hcpSbp);
                packetContext.block();
            } else if (ethernet.getEtherType() == Ethernet.TYPE_IPV4) {
                return;
            }

        }
    }

    private class InternalHCPSuperMessageListener implements HCPSuperMessageListener {

        @Override
        public void handleIncommingMessage(HCPMessage message) {
            if (message.getType() != HCPType.HCP_SBP) {
                return;
            }
            HCPSbp hcpSbp = (HCPSbp) message;
            switch (hcpSbp.getSbpCmpType()) {
                case PACKET_OUT:
                    HCPPacketOut hcpPacketOut = (HCPPacketOut) hcpSbp.getSbpCmpData();
                    PortNumber portNumber = PortNumber.portNumber(hcpPacketOut.getOutPort());
                    Ethernet ethernet = domainController.parseEthernet(hcpPacketOut.getData());
                    log.info("==========PACKET_OUT======{}===", (ARP) ethernet.getPayload());
                    processPacketOut(portNumber, ethernet);
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

    private class InternalHCPSuperControllerListener implements HCPSuperControllerListener {

        @Override
        public void connectToSuperController(HCPSuper hcpSuper) {
            log.info("1111111111111111111111111111111111");
            setUp();
        }

        @Override
        public void disconnectSuperController(HCPSuper hcpSuper) {

        }
    }

    private class InternalDeviceListener implements PofSwitchListener {


        @Override
        public void switchAdded(Dpid dpid) {

        }

        @Override
        public void hanndleConnectionUp(Dpid dpid) {

        }

        @Override
        public void switchRemoved(Dpid dpid) {

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

    private void removeOraddDevice(DeviceId deviceId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!TableIDMap.containsKey(deviceId)) {
            int tabaleId = sendPofFlowTables(deviceId, "FirstEntryTable");
            TableIDMap.put(deviceId, tabaleId);
            return;
        }
        reMoveFlowTable(deviceId, TableIDMap.get(deviceId));
        TableIDMap.remove(deviceId);
    }

    private class InternalListener implements DeviceListener {
        @Override
        public void event(DeviceEvent deviceEvent) {
            log.info("==============deviceEvent==========={}======", deviceEvent.type());
            DeviceId deviceId = deviceEvent.subject().id();
            switch (deviceEvent.type()) {
                case DEVICE_ADDED:
                    int tabaleId = sendPofFlowTables(deviceId, "FirstEntryTable");
                    TableIDMap.put(deviceId, tabaleId);
                    log.info("=====================TableIdMap========={}=====", TableIDMap.toString());
                    break;
                case DEVICE_AVAILABILITY_CHANGED:
                    removeOraddDevice(deviceId);
                    log.info("=====================TableIdMap========={}=====", TableIDMap.toString());
                    break;
                default:
                    break;
            }
            return;
//
        }
    }

    private StringBuffer IpAddressToHexString(IpAddress ipAddress) {
        StringBuffer stringBuffer = new StringBuffer();
        byte[] bytes = ipAddress.toOctets();
        for (byte b : bytes) {
            stringBuffer.append(toHex(b));
        }
        return stringBuffer;
    }

    private String toHex(int num) {
        if (num == 0) return "00";
        String result = "";
        while (num != 0) {
            int x = num & 0xF;
            result = map[(x)] + result;
            num = (num >>> 4);
        }
        if (num >= 16 && result.length() == 1) {
            return result + "0";
        }
        return "0" + result;
    }
}
