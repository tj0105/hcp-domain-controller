package org.onosproject.system.domain;


import com.google.common.collect.Table;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import jnr.ffi.annotations.In;
import org.apache.felix.scr.annotations.*;
import org.onlab.graph.DefaultEdgeWeigher;
import org.onlab.graph.ScalarWeight;
import org.onlab.graph.Weight;
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
import org.onosproject.hcp.protocol.ver10.HCPForwardingRequestVer10;
import org.onosproject.hcp.protocol.ver10.HCPPacketInVer10;
import org.onosproject.hcp.protocol.ver10.HCPResourceReplyVer10;
import org.onosproject.hcp.types.*;
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
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.*;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.table.*;
import org.onosproject.net.topology.*;
import org.onosproject.pof.controller.Dpid;
import org.onosproject.pof.controller.PofController;
import org.onosproject.pof.controller.PofSwitchListener;
import org.onosproject.pof.controller.RoleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.onosproject.net.host.HostEvent.Type.HOST_ADDED;

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainTopoService hcpDomainTopoServie;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService LinkServie;

    private HostListener hostListener=new InternalHostListener();
    private PacketProcessor packetProcessor = new ReactivePacketProcessor();
    private HCPSuperMessageListener hcpSuperMessageListener = new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener = new InternalHCPSuperControllerListener();
    private PofSwitchListener pofSwitchListener = new InternalDeviceListener();
    private DeviceListener deviceListener = new InternalListener();
    public final short SIP = 12;
    public final short DIP = 13;

    private ApplicationId applicationId;
    private NodeId local;
    private HCPVersion hcpVersion;
    private HCPFactory hcpfactory;
    private ConcurrentHashMap<DeviceId, Integer> TableIDMap;
    private ConcurrentHashMap<IpAddress,Map<HCPVport,Path>> ipaddressPathMap;
    private List<TopologyVertex> topologyVertexList=new ArrayList<>();
    private HashMap<TopologyVertex,List<TopologyEdge>> topologyEdgeHashMap=new HashMap<>();
    private HashMap<String,Link> src_dst_Link=new HashMap<>();
    private static final char[] map = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public  DefaultTopology defaultTopology;
    public  static ProviderId RouteproviderId=new ProviderId("USTC","intra-route");

    private LinkWeigher BANDWIDTH_WEIGHT=new graphBanwidthWeigth();
    private boolean flag = false;
    private boolean drl_flag=true;

    //DRL socket
    private Socket DRL_Socket;
    private InputStream DRL_input;
    private InputStreamReader DRL_input_reader;
    private BufferedReader bufferedReader;
    private OutputStream DRL_output;
    private PrintWriter printWriter;
    private String DRL_IP="127.0.0.1";
    private int DRL_PORT=11000;
    private boolean DRL_TRAIN_COMPLETE=false;
    private int DEVICEID_LENGTH=0;
    @Activate
    public void activate() {
//        applicationId = coreService.registerApplication("org.onosproject.domain.system");
//        domainController.addHCPSuperControllerListener(hcpSuperControllerListener);
////        pofController.addListener(pofSwitchListener);
//        deviceService.addListener(deviceListener);
//        init();
        log.info("=======================HCP Domain Routing Manager================");
    }

    @Deactivate
    public void deactivate() {
//        for (DeviceId deviceId : TableIDMap.keySet()) {
//            reMoveFlowTable(deviceId, TableIDMap.get(deviceId));
//        }
//        TableIDMap.clear();
//        ipaddressPathMap.clear();
//        topologyEdgeHashMap.clear();
//        topologyVertexList.clear();
//        src_dst_Link.clear();
//        if (!flag) {
//            return;
//        }
//        deviceService.removeListener(deviceListener);
////        pofController.removeListener(pofSwitchListener);
//        domainController.removeHCPSuperControllerListener(hcpSuperControllerListener);
//        packetService.removeProcessor(packetProcessor);
//        domainController.removeMessageListener(hcpSuperMessageListener);
        log.info("=======================HCP Domain Routing Manager Stopped");
    }

    public void setUp() {
        flag = true;
        hcpVersion = domainController.getHCPVersion();
        hcpfactory = HCPFactories.getFactory(hcpVersion);
        domainController.addMessageListener(hcpSuperMessageListener);
        packetService.addProcessor(packetProcessor, PacketProcessor.director(4));
        if (drl_flag){
            try {
                Thread.sleep(10000);
                Topology topology = topologyService.currentTopology();
                defaultTopology = (DefaultTopology) topology;
                int min=9999;
                topologyVertexList=new ArrayList<>(defaultTopology.getGraph().getVertexes());
//                log.info("=======before=====topologyVertexList===={}",topologyVertexList.toString());
                topologyVertexList.sort((p1,p2)->Integer.parseInt(p1.deviceId().toString().split(":")[1],16)>
                        Integer.parseInt(p2.deviceId().toString().split(":")[1],16)?1:Integer.parseInt(p1.deviceId().toString().split(":")[1],16)<
                        Integer.parseInt(p2.deviceId().toString().split(":")[1],16)?-1:0);
//                log.info("=======after=====topologyVertexList===={}",topologyVertexList.toString());
                for (TopologyVertex topologyVertex:topologyVertexList){
                    int device_Id=Integer.parseInt(topologyVertex.toString().split(":")[1],16);
                    if (min>device_Id){
                        min=device_Id;
                    }
                    List<TopologyEdge> topologyEdgeList=new ArrayList<>(defaultTopology.getGraph().getEdgesFrom(topologyVertex));
                    topologyEdgeHashMap.put(topologyVertex,topologyEdgeList);
                }
                DEVICEID_LENGTH=min-1;
                for (Link link:LinkServie.getActiveLinks()) {
                    DeviceId srcDevice=link.src().deviceId();
                    DeviceId dstDevice=link.dst().deviceId();
                    StringBuffer stringBuffer=new StringBuffer();
                    stringBuffer.append(Integer.parseInt(srcDevice.toString().split(":")[1],16)-DEVICEID_LENGTH);
                    stringBuffer.append(",");
                    stringBuffer.append(Integer.parseInt(dstDevice.toString().split(":")[1],16)-DEVICEID_LENGTH);
                    src_dst_Link.put(stringBuffer.toString(),link);
                }
                DRL_Socket=new Socket(DRL_IP,DRL_PORT);
                DRL_input=DRL_Socket.getInputStream();
                DRL_input_reader=new InputStreamReader(DRL_input);
                bufferedReader=new BufferedReader(DRL_input_reader);
                DRL_output=DRL_Socket.getOutputStream();
                printWriter=new PrintWriter(DRL_output);
                new Thread(new drl_start()).start();
            }catch (Exception e){
                log.debug("=============hcpdomainRoutingManager debug={}",e.getMessage());
            }

        }
    }

    public void init() {
        TableIDMap = new ConcurrentHashMap<>();
        ipaddressPathMap=new ConcurrentHashMap<>();
        for (Device device : deviceService.getAvailableDevices()) {
            DeviceId deviceId = device.id();
            int tableId = sendPofFlowTables(deviceId, "FirstEntryTable");
            TableIDMap.put(deviceId, tableId);
        }
    }
    class drl_start implements Runnable{

        @Override
        public void run() {
            boolean flag=true;
            try{
                if (flag){
                    StringBuffer stringBuffer=new StringBuffer();
                    stringBuffer.append("1\n");
//                    for (TopologyVertex topologyVertex: topologyVertexList) {
//                        stringBuffer.append(Integer.parseInt(topologyVertex.toString().split(":")[1],16));
//                        for (TopologyEdge topologyEdge:topologyEdgeHashMap.get(topologyVertex)) {
//                            stringBuffer.append(",");
//                            stringBuffer.append(Integer.parseInt(topologyEdge.dst().toString().split(":")[1],16)-DEVICEID_LENGTH);
//                        }
//                        stringBuffer.append(":"+"10"+"\n");
//                    }
                    flag=false;
//                    stringBuffer.deleteCharAt(stringBuffer.length()-1);
                    log.info("==========drl message stringbuffer====={}",stringBuffer.toString());
                    printWriter.print(stringBuffer.toString());
                    printWriter.flush();
                }
                String receve=bufferedReader.readLine();
                if (receve.equals("2")){
                    StringBuffer stringBuffer1=new StringBuffer();
                    stringBuffer1.append("3\n");
                    for (int i = 0; i < defaultTopology.linkCount()-1; i++) {
                        stringBuffer1.append("10,");
                    }
                    stringBuffer1.append("10:");
                    stringBuffer1.append(9);
                    stringBuffer1.append(",");
                    stringBuffer1.append(12);
                    stringBuffer1.append(",5");
                    printWriter.println(stringBuffer1.toString());
                    printWriter.flush();
                    log.info("==========drl message stringbuffer====={}",stringBuffer1.toString());
                    String message=bufferedReader.readLine();
                    log.info("=======messsage={}======DRL have been to Train================",message);
                    Thread.sleep(5000);

                    StringBuffer stringBuffer2=new StringBuffer();
                    stringBuffer2.append("3\n");
                    for (int i = 0; i < defaultTopology.linkCount()-1; i++) {
                        stringBuffer2.append("10,");
                    }
                    stringBuffer2.append("10:");
                    stringBuffer2.append(9);
                    stringBuffer2.append(",");
                    stringBuffer2.append(12);
                    stringBuffer2.append(",5");
                    log.info("==========send========current time=={}=",System.currentTimeMillis());
                    printWriter.println(stringBuffer2.toString());
                    printWriter.flush();
                    String message1=bufferedReader.readLine();
                    log.info("==========result========current time=={}=",System.currentTimeMillis());
                    log.info("====result===messsage={}=====================",message1);
                    DRL_TRAIN_COMPLETE=true;
                }
            }catch (Exception e){
                log.debug("=============drl start debug={}",e.getMessage());
            }
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
        srcIp.setFieldId((short) DIP);
        srcIp.setFieldName("dstIp");
        srcIp.setOffset((short) 240);
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

    private void installFlowRule(DeviceId deviceId, int tableId, String dstIp, int port, int pority) {
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> mathchList = new ArrayList<>();
        mathchList.add(Criteria.matchOffsetLength(DIP, (short) 240, (short) 32, dstIp, "FFFFFFFF"));
        trafficSelector.add(Criteria.matchOffsetLength(mathchList));

        TrafficTreatment.Builder trafficTreatMent = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, port).action();
        actions.add(action_output);
        trafficTreatMent.add(DefaultPofInstructions.applyActions(actions));
//        log.info("deviceId:{},IpAddress:{},action_out:{}",deviceId,dstIp, action_output);

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

    private Path getvportPath(IpAddress ipAddress,HCPVport vport){
        if (!ipaddressPathMap.containsKey(ipAddress)){
            return null;
        }
        Map<HCPVport,Path> vportPathMap=ipaddressPathMap.get(ipAddress);
        if (!vportPathMap.containsKey(vport)){
            return null;
        }
        return vportPathMap.get(vport);
    }

    /**
     * calculate the path between the srcHost and dstHost.
     * install the flow entry throw the path;
     * @param srcHost
     * @param dstHost
     * @param srcAddress
     * @param dstAddress
     */
    private void processIpv4InDomain(Host srcHost, Host dstHost, IpAddress srcAddress,IpAddress dstAddress) {
        DeviceId srcDeviceId = srcHost.location().deviceId();
        DeviceId dstDeviceId = dstHost.location().deviceId();
        String srcIP = IpAddressToHexString(srcAddress).toString();
        String dstIp = IpAddressToHexString(dstAddress).toString();
        if (srcDeviceId.equals(dstDeviceId)) {
            int tableId = TableIDMap.get(srcDeviceId);
            installFlowRule(srcDeviceId, tableId, dstIp, (int) dstHost.location().port().toLong(), 10);
            installFlowRule(srcDeviceId,tableId,srcIP,(int)srcHost.location().port().toLong(),10);
            return;
        }
        Path path=null;
        //  qi fa shi suan fa dedao de jiguo
        if (!drl_flag){
            log.info("===========srcDeviceiD:{},dstDeviceId:{}",srcDeviceId,dstDeviceId);

            Topology topology = topologyService.currentTopology();
            DefaultTopology defaultTopology = (DefaultTopology) topology;
//        log.info("==================Topology:{}{}",topology.linkCount(),topology.deviceCount());
            Set<Path> paths = defaultTopology.getPaths(srcDeviceId, dstDeviceId,BANDWIDTH_WEIGHT);
//        log.info("===============paths:{}",paths.toString());
            if (paths==null){
                paths=defaultTopology.getPaths(srcDeviceId,dstDeviceId);
            }
            path= (Path) paths.toArray()[0];
        }
        else{
           path=getDRLPath(srcDeviceId,dstDeviceId,srcIP,dstIp);
           if (path==null){
               Topology topology = topologyService.currentTopology();
               DefaultTopology defaultTopology = (DefaultTopology) topology;
//        log.info("==================Topology:{}{}",topology.linkCount(),topology.deviceCount());
               Set<Path> paths = defaultTopology.getPaths(srcDeviceId, dstDeviceId,BANDWIDTH_WEIGHT);
//        log.info("===============paths:{}",paths.toString());
               if (paths==null){
                   paths=defaultTopology.getPaths(srcDeviceId,dstDeviceId);
               }
               path= (Path) paths.toArray()[0];
           }
        }

        // install the rule for the deviceId
        log.info("===========path========={}=======",path.toString());
        for (Link link : path.links()) {
//            log.info("==============link:{}=============",link.toString());
            DeviceId deviceId = link.src().deviceId();
            int tableId = TableIDMap.get(deviceId);
            installFlowRule(deviceId, tableId, dstIp, (int) link.src().port().toLong(), 10);
            int dsttableid=TableIDMap.get(link.dst().deviceId());
            installFlowRule(link.dst().deviceId(),dsttableid,srcIP,(int)link.dst().port().toLong(),10);
        }
        int tableID1 = TableIDMap.get(dstDeviceId);
        int tableId2 = TableIDMap.get(srcDeviceId);
        installFlowRule(dstDeviceId, tableID1, dstIp,
                (int) dstHost.location().port().toLong(), 10);
        installFlowRule(srcDeviceId,tableId2,srcIP,(int)srcHost.location().port().toLong(),10);
    }

    /**
     * get the path through the drl model
     * @param srcDevice srchost devices
     * @param dstDevice dsthost devices
     * @param srcIp request srcIp
     * @param dstIp request dstIp
     * @return path
     */
    private Path getDRLPath(DeviceId srcDevice,DeviceId dstDevice,String srcIp,String dstIp){
        List<Link> links=new ArrayList<>();
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("3\n");
        for (int i = 0; i < defaultTopology.linkCount()-1; i++) {
            stringBuffer.append("10,");
        }
        stringBuffer.append("10:");
        stringBuffer.append(Integer.parseInt(srcDevice.toString().split(":")[1],16)-DEVICEID_LENGTH);
        stringBuffer.append(",");
        stringBuffer.append(Integer.parseInt(dstDevice.toString().split(":")[1],16)-DEVICEID_LENGTH);
        stringBuffer.append(",5");
//        log.info("request information:srcAddress={},dstAddress={},src_device={},dst_device={},message={}",
//                srcIp,dstIp,srcDevice.toString(),dstDevice.toString(),stringBuffer.toString());
        try {
//            log.info("==========================before==time===DRL request===={}===",System.currentTimeMillis());
            printWriter.println(stringBuffer.toString());
            printWriter.flush();
            String message=bufferedReader.readLine();
//            log.info("==========================before==time===DRL request===={}===",System.currentTimeMillis());
            if (message.equals("False")){
                return null;
            }
//            log.info("===========DRL message={}======",message);
            String mess[]=message.split(",");
            for (int i = 0; i <mess.length-1 ; i++) {
                String temp=mess[i]+","+mess[i+1];
                links.add(src_dst_Link.get(temp));
            }
//            if(mess[0].equals("5")){
//                return null;
//            }
//            String deci[]=mess[1].split(",");
//            DeviceId [] deviceIds=new DeviceId[deci.length];
//            for(int i=0;i<mess.length;i++){
//                deviceIds[i]=DeviceId.deviceId("pof:"+String.format("%016x",deci[i]));
//            }
//            for(int i=0;i<deviceIds.length-1;i++){
//                Set<Link> linkSet=LinkServie.getDeviceLinks(deviceIds[i]);
//                for (Link link:linkSet){
//                    if (link.dst().deviceId()==deviceIds[i+1]){
//                        links.add(link);
//                        break;
//                    }
//                }
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DefaultPath(RouteproviderId,links,new ScalarWeight(1));
    }
    /***
     * process the packetOut from the SuperController.
     * @param portNumber
     * @param ethernet
     */
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
    private void HostToEdgeHop(){
        Set<HCPConfigFlags> hcpConfigFlags=new HashSet<>();
        hcpConfigFlags.add(HCPConfigFlags.CAPABILITIES_HOP);
        for (Host host:hostService.getHosts()){
            IPv4Address iPv4Address=IPv4Address.of((host.ipAddresses().toArray()[0]).toString());
            processResourceRequest(iPv4Address,hcpConfigFlags);
        }
    }
    private void processResourceRequest(IPv4Address dstIpv4Address,Set<HCPConfigFlags> flags){
        IpAddress dstIpaddress=IpAddress.valueOf(dstIpv4Address.toString());
        List<HCPVportHop> vportHops=new ArrayList<>();
        if (flags.contains(HCPConfigFlags.CAPABILITIES_HOP)) {
            HashMap pathmap=new HashMap<>();
            ipaddressPathMap.put(dstIpaddress,pathmap);
            Set<Host> dsthostSet = hostService.getHostsByIp(dstIpaddress);
            Set<ConnectPoint> connectPointSet=hcpDomainTopoServie.getVPortConnectPoint();
            Host dstHost=(Host)dsthostSet.toArray()[0];
            DefaultTopology topology=(DefaultTopology)topologyService.currentTopology();
            for (ConnectPoint connectPoint:connectPointSet){
                DeviceId dstDeviceId=connectPoint.deviceId();
                if (dstHost.location().deviceId().equals(dstDeviceId)){
                    HCPVport vport=HCPVport.ofShort(
                            (short) hcpDomainTopoServie.getLogicalVportNumber(connectPoint).toLong());
                    HCPVportHop hcpVportHop=HCPVportHop.of(vport,0);
                    vportHops.add(hcpVportHop);
                    continue;
                }
                Set<Path> paths=topology.getPaths(dstHost.location().deviceId(),dstDeviceId,BANDWIDTH_WEIGHT);
                Path path=(Path)paths.toArray()[0];
                HCPVport vport=HCPVport.ofShort(
                        (short) hcpDomainTopoServie.getLogicalVportNumber(connectPoint).toLong());
                HCPVportHop hcpVportHop;
                if (path==null){
                    hcpVportHop=HCPVportHop.of(vport,1000);
                }
                else{
                    hcpVportHop=HCPVportHop.of(vport,path.links().size());
                    pathmap.put(vport,path);
                }
                vportHops.add(hcpVportHop);

            }
            sendResourceFlowToSuper(dstIpv4Address,vportHops);
        }

    }
    private void processFlowForwardingReply(IPv4Address srcIpv4address,IPv4Address dstIpv4Address,
                                            HCPVport srcVport,HCPVport dstVPort,short type,byte qos){
//        log.info("============================process Flow_Reply=======srcVport={}==dstVport={}============",srcVport.toString(),dstVPort.toString());
        IpAddress srcAddress= IpAddress.valueOf(srcIpv4address.toString());
        IpAddress dstAddress= IpAddress.valueOf(dstIpv4Address.toString());
        String srcIp=IpAddressToHexString(srcAddress).toString();
        String dstIp=IpAddressToHexString(dstAddress).toString();
        if (srcVport.equals(HCPVport.IN_PORT)){
//            log.info("============in the in port here");
            ConnectPoint connectPoint=hcpDomainTopoServie.getLocationByVport(PortNumber.portNumber(dstVPort.getPortNumber()));
            Set<Host> hostSet=hostService.getHostsByIp(dstAddress);
            Host dstHost=(Host)hostSet.toArray()[0];
            Path path=null;
            if (!drl_flag){
               path=getvportPath(dstAddress,dstVPort);
            }
            else{
                path=getDRLPath(connectPoint.deviceId(),dstHost.location().deviceId(),srcIp,dstIp);
            }
//            log.info("=====================path========={}",path.toString());

            if (path==null){
                int tableId=TableIDMap.get(dstHost.location().deviceId());
                installFlowRule(dstHost.location().deviceId(),tableId,dstIp,(int)dstHost.location().port().toLong(),10);
                installFlowRule(dstHost.location().deviceId(),tableId,srcIp,(int)connectPoint.port().toLong(),10);
                return ;
            }
            for (Link link:path.links()){
                if (link.src().deviceId().equals(connectPoint.deviceId())){
                    int TableId=TableIDMap.get(link.src().deviceId());
                    installFlowRule(link.src().deviceId(),TableId,srcIp,(int)connectPoint.port().toLong(),10);
//                    installFlowRule(link.dst().deviceId(),TableId,dstIp,(int)link.dst().port().toLong(),10);
                }
                if (link.dst().deviceId().equals(dstHost.location().deviceId())) {
                    int TableId = TableIDMap.get(link.dst().deviceId());
//                    installFlowRule(link.src().deviceId(),TableId,srcIp,(int)link.src().port().toLong(),10);
                    installFlowRule(link.dst().deviceId(), TableId, dstIp, (int) dstHost.location().port().toLong(), 10);
                }
                int srctableId=TableIDMap.get(link.src().deviceId());
                installFlowRule(link.src().deviceId(),srctableId,dstIp,(int)link.src().port().toLong(),10);
                int dsttableId=TableIDMap.get(link.dst().deviceId());
                installFlowRule(link.dst().deviceId(),dsttableId,srcIp,(int)link.dst().port().toLong(),10);
            }
            log.info("=====================path========={}",path.toString());
        }else if(srcVport.equals(HCPVport.OUT_PORT)){
//            log.info("=================in the out port here");
            ConnectPoint connectPoint=hcpDomainTopoServie.getLocationByVport(PortNumber.portNumber(dstVPort.getPortNumber()));
            Set<Host> hostsSet=hostService.getHostsByIp(srcAddress);
            Host srcHost=(Host)hostsSet.toArray()[0];
            Path path=null;
            if (!drl_flag){
                path=getvportPath(srcAddress,dstVPort);
            }else{
                path=getDRLPath(srcHost.location().deviceId(),connectPoint.deviceId(),srcIp,dstIp);
            }

//            log.info("=====================path========={}",path.toString());
            if (path==null){
                int tableId=TableIDMap.get(srcHost.location().deviceId());
                installFlowRule(srcHost.location().deviceId(),tableId,dstIp,(int)connectPoint.port().toLong(),10);
                installFlowRule(srcHost.location().deviceId(),tableId,srcIp,(int)srcHost.location().port().toLong(),10);
                return ;
            }
            for (Link  link:path.links()){
                if (link.src().deviceId().equals(srcHost.location().deviceId())){
                    int TableId=TableIDMap.get(link.src().deviceId());
                    installFlowRule(link.src().deviceId(),TableId,srcIp,(int)srcHost.location().port().toLong(),10);
                }
                if (link.dst().deviceId().equals(connectPoint.deviceId())){
                    int TableId=TableIDMap.get(link.dst().deviceId());
                    installFlowRule(link.dst().deviceId(),TableId,dstIp,(int)connectPoint.port().toLong(),10);
                }
                int srcTableId=TableIDMap.get(link.src().deviceId());
                installFlowRule(link.src().deviceId(),srcTableId,dstIp,(int)link.src().port().toLong(),10);
                int dstTableId=TableIDMap.get(link.dst().deviceId());
                installFlowRule(link.dst().deviceId(),dstTableId,srcIp,(int)link.dst().port().toLong(),10);
            }
            log.info("=====================path========={}",path.toString());
        }else{
//            log.info("=================in the middle doamin here");
            ConnectPoint srcConnectPoint=hcpDomainTopoServie.getLocationByVport(PortNumber.portNumber(srcVport.getPortNumber()));
            ConnectPoint dstConnectPoint=hcpDomainTopoServie.getLocationByVport(PortNumber.portNumber(dstVPort.getPortNumber()));
            Path path=null;
            if(!drl_flag){
                path=hcpDomainTopoServie.getVportToVportPath(srcVport,dstVPort);
            }else{
                path=getDRLPath(srcConnectPoint.deviceId(),dstConnectPoint.deviceId(),srcIp,dstIp);
            }
            if (path==null){
                int tableId=TableIDMap.get(srcConnectPoint.deviceId());
                installFlowRule(srcConnectPoint.deviceId(),tableId,dstIp,(int)dstConnectPoint.port().toLong(),10);
                installFlowRule(srcConnectPoint.deviceId(),tableId,srcIp,(int)srcConnectPoint.port().toLong(),10);
                return ;
            }
            for (Link link:path.links()){
                if (link.src().deviceId().equals(srcConnectPoint.deviceId())){
                    int TableId=TableIDMap.get(link.src().deviceId());
                    installFlowRule(link.src().deviceId(),TableId,srcIp,(int)srcConnectPoint.port().toLong(),10);
                }
                if (link.dst().deviceId().equals(dstConnectPoint.deviceId())){
                    int TableId=TableIDMap.get(link.src().deviceId());
                    installFlowRule(link.dst().deviceId(),TableId,dstIp,(int)dstConnectPoint.port().toLong(),10);
                }
                int srcTableId=TableIDMap.get(link.src().deviceId());
                installFlowRule(link.src().deviceId(),srcTableId,dstIp,(int)link.src().port().toLong(),10);
                int dstTableId=TableIDMap.get(link.dst().deviceId());
                installFlowRule(link.dst().deviceId(),dstTableId,srcIp,(int)link.dst().port().toLong(),10);
            }
            log.info("=====================path========={}",path.toString());
        }
    }
    private void sendResourceFlowToSuper(IPv4Address dstIp,List<HCPVportHop> list){
        Set<HCPSbpFlags> flagsSet = new HashSet<>();
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        HCPResourceReply hcpResourceReply= HCPResourceReplyVer10.of(dstIp,list);
        HCPSbp hcpSbp=hcpfactory.buildSbp()
                .setSbpCmpType(HCPSbpCmpType.RESOURCE_REPLY)
                .setFlags(flagsSet)
                .setDataLength((short)hcpResourceReply.getData().length)
                .setSbpXid(1)
                .setSbpCmpData(hcpResourceReply)
                .build();
//        log.info("==========hcpResourceReply======{}======",hcpResourceReply.toString());
        domainController.write(hcpSbp);
    }

    /**
     * send the requst to the SuperController for get the Domain routing
     * @param srcHost  src host
     * @param srcAddress request src Ipaddress
     * @param targetAddress request dst Ipaddress
     * @param connectPoint  the device of recive the request information
     */
    private void SendFlowRequestToSuper(Host srcHost,IpAddress srcAddress,IpAddress targetAddress,ConnectPoint connectPoint) {
        Map<HCPVport,Path> pathmap=ipaddressPathMap.get(srcAddress);
        if(pathmap==null){
            pathmap=new HashMap<>();
            ipaddressPathMap.put(srcAddress,pathmap);
        }
        DefaultTopology topology=(DefaultTopology)topologyService.currentTopology();
        DeviceId srcDeviceId=srcHost.location().deviceId();
        IPv4Address src=IPv4Address.of(srcAddress.toString());
        IPv4Address dst=IPv4Address.of(targetAddress.toString());
        List<HCPVportHop> vportHops=new ArrayList<>();
        Set<ConnectPoint> connectPointSet=hcpDomainTopoServie.getVPortConnectPoint();
        for (ConnectPoint connectPoint1:connectPointSet){
            DeviceId dstDeviceId=connectPoint1.deviceId();
            if (srcDeviceId.equals(dstDeviceId)){
                HCPVport vport=HCPVport.ofShort(
                        (short) hcpDomainTopoServie.getLogicalVportNumber(connectPoint1).toLong());
                HCPVportHop hcpVportHop=HCPVportHop.of(vport,0);
                vportHops.add(hcpVportHop);
                continue;
            }
            Set<Path> paths=topology.getPaths(srcDeviceId,dstDeviceId,BANDWIDTH_WEIGHT);
            Path path=(Path)paths.toArray()[0];
            HCPVport vport=HCPVport.ofShort(
                    (short) hcpDomainTopoServie.getLogicalVportNumber(connectPoint1).toLong());
            if (paths==null){
                HCPVportHop hcpVportHop=HCPVportHop.of(vport,1000);
                vportHops.add(hcpVportHop);
            }
            else{
                HCPVportHop hcpVportHop=HCPVportHop.of(vport,path.links().size());
                vportHops.add(hcpVportHop);
                pathmap.put(vport,path);
            }
        }
//        log.info("==========IddressPathMap======={}",ipaddressPathMap.toString());
        HCPForwardingRequest hcpForwardingRequest= HCPForwardingRequestVer10.of(src,dst,(int )connectPoint.port().toLong()
                                                    ,Ethernet.TYPE_IPV4,(byte)3,vportHops);
//        log.info("======================hcpForwardingRequest============={}",hcpForwardingRequest.toString());
        Set<HCPSbpFlags> flagsSet = new HashSet<>();
        flagsSet.add(HCPSbpFlags.DATA_EXITS);
        HCPSbp hcpSbp=hcpfactory.buildSbp()
                .setSbpCmpType(HCPSbpCmpType.FLOW_FORWARDING_REQUEST)
                .setFlags(flagsSet)
                .setDataLength((short)hcpForwardingRequest.getData().length)
                .setSbpCmpData(hcpForwardingRequest)
                .setSbpXid(1)
                .build();
        domainController.write(hcpSbp);

    }
    /**
     * Process the ARP_request,ARP_Reply and IPV4 packet.
     * if the Packet is ARP_request and ARP_reply, check the target Address whether in the domain,
     * if not ,encapsulation  the arp_request and arp_reply packet into the HCPSbp(PacketIN),then
     * send to the SuperController.
     * if the packet the ipv4, if the target Address in the domain,calculate the source address and target
     * address path, construct the flow entry to the deviceId, if not,send to the SuperController and calculate
     * the hops from the source address to every vport.
     */
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
//                log.info("==============srcAddress:{},targetAddress:{}======", srcAddress.toString(), targetAddress.toString());
            }

            Set<Host> dsthost = hostService.getHostsByIp(targetAddress);
            Set<Host> srchost = hostService.getHostsByIp(srcAddress);
            if (dsthost != null && dsthost.size() > 0) {
                if (ethernet.getEtherType() == Ethernet.TYPE_IPV4) {
                    processIpv4InDomain((Host)srchost.toArray()[0], (Host) dsthost.toArray()[0], srcAddress,targetAddress);
                }
                packetContext.block();
                return;
            }
            //构建packetIn数据包发送给上层控制器
            if (ethernet.getEtherType() == Ethernet.TYPE_ARP) {
//                log.info("========arp=====",(ARP)ethernet.getPayload());
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
//                return ;
                SendFlowRequestToSuper((Host)srchost.toArray()[0],srcAddress,targetAddress,connectPoint);
                packetContext.block();
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
//                    log.info("==========PACKET_OUT======{}===", (ARP) ethernet.getPayload());
                    processPacketOut(portNumber, ethernet);
                    break;
                case RESOURCE_REQUEST:
                    HCPResourceRequest hcpResourceRequest=(HCPResourceRequest)hcpSbp.getSbpCmpData();
//                    log.info("==================HCPResourceRequest==============");
                    IPv4Address srcIpv4Address=hcpResourceRequest.getSrcIpAddress();
                    IPv4Address dstIpv4Address=hcpResourceRequest.getDstIpAddress();
                    Set<HCPConfigFlags> flagsSet=hcpResourceRequest.getFlags();
                    processResourceRequest(dstIpv4Address,flagsSet);
                    break;
                case FLOW_FORWARDING_REPLY:
                    HCPForwardingReply hcpForwardingReply=(HCPForwardingReply)hcpSbp.getSbpCmpData();
                    IPv4Address srcIpv4=hcpForwardingReply.getSrcIpAddress();
                    IPv4Address dstIpv4=hcpForwardingReply.getDstIpAddress();
                    HCPVport srcVport=hcpForwardingReply.getSrcVport();
                    HCPVport dstVport=hcpForwardingReply.getDstVport();
                    short type=hcpForwardingReply.getEthType();
                    byte qos=hcpForwardingReply.getQos();
                    processFlowForwardingReply(srcIpv4,dstIpv4,srcVport,dstVport,type,qos);
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
//            setUp();
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
    private class InternalHostListener implements HostListener{

        @Override
        public void event(HostEvent hostEvent) {
            Host updatedHost = null;
            Host removedHost = null;
            List<HCPHost> hcpHosts = new ArrayList<>();
            switch (hostEvent.type()) {
                case HOST_ADDED:
                    updatedHost = hostEvent.subject();
                    break;
                case HOST_REMOVED:
                    removedHost = hostEvent.subject();
                    break;
                case HOST_UPDATED:
                    updatedHost = hostEvent.subject();
                    removedHost = hostEvent.prevSubject();
                    break;
                default:
            }
            if (null!=updatedHost){
                IpAddress ipAddress=(IpAddress) updatedHost.ipAddresses().toArray()[0];
                IPv4Address hostIpaddress= IPv4Address.of(ipAddress.toOctets());
                Set<HCPConfigFlags> flags=new HashSet<>();
                flags.add(HCPConfigFlags.CAPABILITIES_HOP);
                processResourceRequest(hostIpaddress,flags);
            }

        }
    }
    class graphBanwidthWeigth extends DefaultEdgeWeigher<TopologyVertex,TopologyEdge> implements LinkWeigher {

        @Override
        public Weight weight(TopologyEdge topologyEdge) {
//            if (hcpDomainTopoServie.getResetVportCapability(topologyEdge.link().dst())<hcpDomainTopoServie.getVportMaxCapability(topologyEdge.link().dst())*0.2){
//                return ScalarWeight.NON_VIABLE_WEIGHT;
//            }
//            else{
//                return new ScalarWeight(1);
//            }
            return new ScalarWeight(1);
//
        }

    }
    class hostToEdgeHopTask implements Runnable {
        @Override
        public void run() {
           HostToEdgeHop();
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
