package org.onosproject.system.domain;

import jline.internal.Preconditions;
import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.onlab.graph.DefaultEdgeWeigher;
import org.onlab.graph.ScalarWeight;
import org.onlab.graph.Weight;
import org.onlab.packet.Ethernet;
import org.onlab.packet.HCPLLDP;
import org.onlab.packet.MacAddress;
import org.onosproject.api.HCPDomain;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.domain.HCPDomainTopoService;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.cluster.ClusterMetadataService;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPPacketInVer10;
import org.onosproject.hcp.protocol.ver10.HCPVportDescriptionVer10;
import org.onosproject.hcp.types.HCPInternalLink;
import org.onosproject.hcp.types.HCPVport;
import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.link.ProbedLinkProvider;
import org.onosproject.net.packet.*;
import org.onosproject.net.topology.LinkWeigher;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyEdge;
import org.onosproject.net.topology.TopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @Author ldy
 * @Date: 20-3-3 下午9:31
 * @Version 1.0
 */
@Component(immediate = true)
@Service
public class HCPDomainTopologyManager implements HCPDomainTopoService {
    private static final Logger log = LoggerFactory.getLogger(HCPDomainTopologyManager.class);

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceService;

    private PacketProcessor hcplldpPacketProcesser = new InternalPacketProcessor();

    private LinkListener linkListener = new InternalLinkListener();
    private HCPSuperMessageListener hcpSuperMessageListener = new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener = new InternalHCPSuperControllerListener();

    private AtomicLong vportNumber = new AtomicLong(1);
    private Map<ConnectPoint, PortNumber> vportMap = new HashMap<>();
    private Map<ConnectPoint, PortNumber> vportNumAllocateCache = new HashMap<>();
    private Map<PortNumber,Long> VportTimeMap=new HashMap<>();
    private Map<HCPVport,Map<HCPVport,Path>> vportToVportpath=new HashMap<>();
    private Map<String,ConnectPoint> devicePortName=new HashMap<>();
    private ConcurrentHashMap<ConnectPoint,Double> portBandwidth=new ConcurrentHashMap<>();


    private String allportName=null;
    private double MAX_BANDWIDTH=10;
    private double BANDWIDTH_THRESHOLD;
    private ScheduledExecutorService executor;

    private long STATE_VPORT_TIME=10000;
    private final static int LLDP_VPORT_LOCAL = 0xffff;
    private boolean flag = false;
    private LinkWeigher BANDWIDTH_WEIGHT=new graphBanwidthWeigth();
    private Socket socket=null;
    private int UpdateTopologyTimes=0;
    @Activate
    public void activate() {
//        domainController.addHCPSuperControllerListener(hcpSuperControllerListener);
        log.info("==============Domain Topology Manager Start===================");
    }

    @Deactivate
    public void deactivate() {
//        domainController.removeHCPSuperControllerListener(hcpSuperControllerListener);
//        if (!flag) {
//            return;
//        }
//        if (executor!=null){
//            executor.shutdown();
//        }
//        linkService.removeListener(linkListener);
//        domainController.removeMessageListener(hcpSuperMessageListener);
//        packetService.removeProcessor(hcplldpPacketProcesser);
//        vportMap.clear();
//        vportNumAllocateCache.clear();
//        VportTimeMap.clear();
//        vportToVportpath.clear();
//        devicePortName.clear();
//        portBandwidth.clear();
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        log.info("==============Domain Topology Manager Stopped===================");

    }

    private void init() {
        flag = true;
        hcpVersion = domainController.getHCPVersion();
        hcpFactory = HCPFactories.getFactory(hcpVersion);
        log.info("==========hcp Version ====={} ", domainController.getHCPVersion());
        domainController.addMessageListener(hcpSuperMessageListener);
        linkService.addListener(linkListener);
        packetService.addProcessor(hcplldpPacketProcesser, PacketProcessor.director(0));
//        storeDeviceIdPortName();
//        log.info("devicePortName=========={}",devicePortName.toString());
//        allportName=getAllportName();
        executor = newSingleThreadScheduledExecutor(groupedThreads("hcp/topologyupdate", "hcp-topologyupdate-%d", log));
        executor.scheduleAtFixedRate(new TopoUpdateTask(),
                domainController.getPeriod(), domainController.getPeriod(), SECONDS);
//        connectSocketGetBanwidth();
    }

    private void storeDeviceIdPortName(){
        for (Device device:deviceService.getAvailableDevices()) {
            DeviceId deviceId = device.id();
            List<Port> portList =deviceService.getPorts(deviceId);
            for (Port port:portList) {
                PortNumber portNumber=port.number();
                ConnectPoint connectPoint=new ConnectPoint(deviceId,portNumber);
                Annotations annotations=port.annotations();
                for (String key:annotations.keys()){
                    if (key.equals(AnnotationKeys.PORT_NAME)){
                        String name=annotations.value(key);
                        devicePortName.put(name,connectPoint);
                        break;
                    }
                }
            }
        }
    }
    private String getAllportName(){
        StringBuffer buffer=new StringBuffer();
        for (String s:devicePortName.keySet()){
            buffer.append(s+",");
        }
        log.info("buffer before===={}",buffer.toString());
        buffer.deleteCharAt(buffer.length()-1);
        log.info("buffer after===={}",buffer.toString());
        return buffer.toString();
    }
    private void connectSocketGetBanwidth(){
        try {
            socket=new Socket("192.168.109.208",6688);
            ObjectOutputStream outputStream=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream=new ObjectInputStream(socket.getInputStream());
            new Thread(new client_listen(socket,inputStream)).start();
            new Thread(new client_send(socket,outputStream)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    class client_listen implements Runnable{
        private Socket socket;
        private ObjectInputStream inputStream;

        client_listen(Socket socket,ObjectInputStream inputStream){
            this.socket=socket;
            this.inputStream=inputStream;
        }
        @Override
        public void run() {
            try {
                while(true) {
                    JsonObject jsonObject = (JsonObject) inputStream.readObject();
                    String meesge = jsonObject.get("msg").asString();
//                    log.info("======message===={}", jsonObject.toString());
                    if (jsonObject.get("type").asString().equals("1")) {
                        StringBuffer stringBuffer = new StringBuffer(meesge);
                        String max_bandwidth = meesge.split(",")[0];
                        MAX_BANDWIDTH = Double.valueOf(max_bandwidth.split(":")[1]);
                        BANDWIDTH_THRESHOLD = MAX_BANDWIDTH * 0.2;
                        meesge = stringBuffer.delete(0, max_bandwidth.length()+1).toString();
                    }
                    for (String s : meesge.split(",")) {
                        String name = s.split(":")[0];
                        String bandwidth = s.split(":")[1];
                        portBandwidth.put(devicePortName.get(name), Double.valueOf(bandwidth));
                    }
//                    log.info("=====================portBandwidth===={}", portBandwidth.toString());
                }
            } catch (Exception e) {
               log.debug("=============bug===={}",e.getMessage());
            }
        }
    }
    class  client_send implements Runnable{
        private Socket socket;
        private ObjectOutputStream objectOutputStream;

        client_send(Socket socket,ObjectOutputStream outputStream){
            this.socket=socket;
            this.objectOutputStream=outputStream;
        }
        @Override
        public void run() {
            boolean flag=true;
            try {
                while(true){
                    JsonObject jsonObject=new JsonObject();
                    if (flag){
                        jsonObject.add("type","1");
                        jsonObject.add("msg",allportName);
                        flag=false;
                    }
                    else {
                        jsonObject.add("type","2");
                        jsonObject.add("msg","null");

                    }
//                    log.info("=================jsonObject========={}",jsonObject.toString());
                    objectOutputStream.writeObject(jsonObject);
                    objectOutputStream.flush();
                    Thread.sleep(5000);
                }
            }catch (Exception e){
                log.debug("=============bug===={}",e.getMessage());
            }
        }
    }


    /**
     * check whether the connectPoint has already VportNumber
     *
     * @param connectPoint
     * @return the Vport number
     */
    @Override
    public PortNumber getLogicalVportNumber(ConnectPoint connectPoint) {
        return vportMap.containsKey(connectPoint) ? vportMap.get(connectPoint) : PortNumber.portNumber(HCPVport.LOCAL.getPortNumber());
    }

    @Override
    public boolean isOuterPort(ConnectPoint connectPoint) {
        return vportMap.containsKey(connectPoint);
    }

    private PortNumber getVportNum(ConnectPoint edgeConnectPoint) {
        return vportMap.get(edgeConnectPoint);
    }

    @Override
    public ConnectPoint getLocationByVport(PortNumber portNumber) {
        for (ConnectPoint connectPoint : vportMap.keySet()) {
            if (vportMap.get(connectPoint).equals(portNumber)) {
                return connectPoint;
            }
        }
        return null;
    }

    @Override
    public Set<ConnectPoint> getVPortConnectPoint() {
       return new HashSet<>(vportMap.keySet());
    }

    @Override
    public Path getVportToVportPath(HCPVport srcVport, HCPVport dstVport) {
        Map<HCPVport,Path> vportPathMap=vportToVportpath.get(srcVport);
        if (vportPathMap==null){
            return null;
        }
        for (HCPVport vport:vportPathMap.keySet()){
            if (vport.equals(dstVport)){
                return vportPathMap.get(vport);
            }
        }
        return null;
    }
    @Override
    public long getVportMaxCapability(ConnectPoint connectPoint) {
        return (long)MAX_BANDWIDTH;
    }
    @Override
    public long getVportLoadCapability(ConnectPoint connectPoint) {
        return (long)(double) portBandwidth.get(connectPoint);
    }
    @Override
    public long getResetVportCapability(ConnectPoint connectPoint){
        return getVportMaxCapability(connectPoint)-getVportLoadCapability(connectPoint);
    }

    @Override
    public Map<ConnectPoint, PortNumber> getAllVport() {
        return vportMap;
    }


    class graphBanwidthWeigth extends DefaultEdgeWeigher<TopologyVertex,TopologyEdge> implements LinkWeigher {

        @Override
        public Weight weight(TopologyEdge topologyEdge) {
            if (getResetVportCapability(topologyEdge.link().dst())<BANDWIDTH_THRESHOLD){
                return ScalarWeight.NON_VIABLE_WEIGHT;
            }
            else{
                return new ScalarWeight(1);
            }
//            return new ScalarWeight(portBandwidth.get(topologyEdge.link().dst()));
        }

    }
    private final String buildSrcMac() {
        String srcMac = ProbedLinkProvider.fingerprintMac(clusterMetadataService.getClusterMetadata());
        String defaultMac = ProbedLinkProvider.defaultMac();
        if (srcMac.equals(defaultMac)) {
            log.warn("Could not generate fringeprint,Use default value {} ", defaultMac);
            return defaultMac;
        }
        log.trace("Generate MAC Address {}", srcMac);
        return srcMac;
    }

    /**
     * if the vportNumAllcateCache do not have the vport,Assign the new Port Number to the vport;
     * @param connectPoint
     * @return
     */
    private PortNumber AllocateVPortNumber(ConnectPoint connectPoint) {
        if (vportNumAllocateCache.containsKey(connectPoint)) {
            return vportNumAllocateCache.get(connectPoint);
        } else {
            PortNumber number = PortNumber.portNumber(vportNumber.getAndIncrement());
            vportNumAllocateCache.put(connectPoint, number);
            return number;
        }
    }
    private void StoreVPortTime(PortNumber portNumber) {
        VportTimeMap.put(portNumber,System.currentTimeMillis());

    }
    private void removeVport(PortNumber portNumber){
        ConnectPoint connectPoint=getLocationByVport(portNumber);
        if (connectPoint!=null){
            addOrUpdateVport(connectPoint, HCPVportState.LINK_DOWN,HCPVportReason.DELETE);
        }
    }

    /**
     * Assign or add the Vport last time to the vportmap and vportmapTime;
     * @param connectPoint
     * @param vportState
     * @param vportReason
     */
    private void addOrUpdateVport(ConnectPoint connectPoint, HCPVportState vportState, HCPVportReason vportReason) {
        Preconditions.checkNotNull(connectPoint);
        //if vportmap have connectPoint and vportState is add return
        if (vportMap.containsKey(connectPoint) && vportReason.equals(HCPVportReason.ADD)) {
            StoreVPortTime(vportMap.get(connectPoint));
            return;
        }
        if (!vportMap.containsKey(connectPoint) && vportReason.equals(HCPVportReason.ADD)) {
            //add vport to vportmap
            //给这个Connectpoint分配一个Vport端口号，并记录下来
            PortNumber portNumber = AllocateVPortNumber(connectPoint);
            StoreVPortTime(portNumber);
            vportMap.put(connectPoint, portNumber);
        }
        PortNumber portNumber = vportMap.get(connectPoint);
        if (vportReason.equals(HCPVportReason.DELETE)) {
            ConnectPoint connect = getLocationByVport(portNumber);
            vportMap.remove(connect);
        }
        //构造VportStatus数据包，告知SuperController边界端口信息
        UpdateVPortToSuper(portNumber,vportState,vportReason);
        UpdateTopology();
    }

    /**
     * update the vport state to the superController
     * @param portNumber
     * @param Vportstate
     * @param reason
     */
    private void UpdateVPortToSuper(PortNumber portNumber,HCPVportState Vportstate,HCPVportReason reason){
        HCPVport vport = HCPVport.ofShort((short) portNumber.toLong());
        Set<HCPVportState> state = new HashSet<>();
        state.add(Vportstate);
        HCPVportDescribtion vportDesc = new HCPVportDescriptionVer10.Builder()
                .setPortNo(vport)
                .setState(state)
                .build();
        HCPVportStatus vportStatus = hcpFactory
                .buildVportStatus()
                .setReson(reason)
                .setVportDescribtion(vportDesc)
                .build();
        domainController.write(vportStatus);
    }
    private void UpdateVportsToSuper(){
        for (PortNumber portNumber:vportMap.values()){
                 UpdateVPortToSuper(portNumber,HCPVportState.LINK_UP,HCPVportReason.ADD);
        }
    }

    /**
     * computer the resource between two the vports,But now just calculate the hop.
     * Store the min hop path in the the map(vportTovportPath)
     * @param src   vports deviceId
     * @param dst   vports deviceId
     * @param srcVPort srcvport HCPVport
     * @param dstVport dstVport HCPVPort
     * @return
     */
    private HCPInternalLink VPortToVportHCPInernalink(DeviceId src,DeviceId dst,HCPVport srcVPort,HCPVport dstVport){
        Map<HCPVport,Path> srcVportMap=vportToVportpath.get(srcVPort);
        if (srcVportMap==null){
            srcVportMap=new HashMap<>();
            vportToVportpath.put(srcVPort,srcVportMap);
        }
        Set<Path> paths=pathService.getPaths(src,dst);
        //compute the path based the bandwidth
//        Set<Path> paths=pathService.getPaths(src,dst,BANDWIDTH_WEIGHT);
        if (paths==null){
            HCPInternalLink hcpInternalLink=HCPInternalLink.of(srcVPort,dstVport,0,1000);
            return hcpInternalLink;
        }
        List<Path> pathList=new ArrayList(paths);
        pathList.sort((p1, p2) -> ((ScalarWeight) p1.weight()).value() > ((ScalarWeight) p2.weight()).value()
                ? 1 : (((ScalarWeight) p1.weight()).value() < ((ScalarWeight) p2.weight()).value()) ? -1 : 0);
        int hopCapability=pathList.get(0).links().size();
        srcVportMap.put(dstVport,pathList.get(0));
//        Queue<Double> bandwidth_queuq=new LinkedList<>();
//        Path max_bandwidth_path=null;
//        for (Path path:paths) {
//            double min_bandwidth=9999;
//            for (Link link:path.links()){
//                long banwidth=getResetVportCapability(link.dst());
//                if (banwidth<min_bandwidth){
//                    min_bandwidth=banwidth;
//                }
//            }
//            if (bandwidth_queuq.isEmpty()){
//                bandwidth_queuq.add(min_bandwidth);
//            }else{
//                if (min_bandwidth>bandwidth_queuq.peek()){
//                    bandwidth_queuq.poll();
//                    bandwidth_queuq.add(min_bandwidth);
//                }
//            }
//        }
        HCPInternalLink hcpInternalLink=HCPInternalLink.of(srcVPort,dstVport,(long)(double)MAX_BANDWIDTH,hopCapability);
        return hcpInternalLink;
    }

    /**
     * send the abstract intra-domain link to the superController.
     * between the two vports we send such as information:including max bandwidth,
     * sum of the delay and hop,otherwise, we send the vport the maxBandwdith and LoadBandWidth
     * information to the superController.
     */
    private void UpdateTopology() {
        List<HCPInternalLink> internalLinks = new ArrayList<>();
        Set<PortNumber> alreadhandle = new HashSet<>();
        if (vportMap.keySet().isEmpty()){
            return ;
        }
        for (ConnectPoint srcConnection : vportMap.keySet()) {
            PortNumber srcVport = vportMap.get(srcConnection);
            HCPVport srcHCPVPort = HCPVport.ofShort((short) srcVport.toLong());
            long srcVPortMaxCapability = getVportMaxCapability(srcConnection);
//            long srcVportLoadCapability = getVportLoadCapability(srcConnection);
            long srcVportLoadCapability =0;
            for (ConnectPoint dstConnection : vportMap.keySet()) {
                PortNumber dstVPort = vportMap.get(dstConnection);
                HCPVport dstHCPVPort = HCPVport.ofShort((short) dstVPort.toLong());
                if (srcVport.equals(dstVPort) && !alreadhandle.contains(srcVport)) {
                    alreadhandle.add(srcVport);
                    internalLinks.add(HCPInternalLink.of(srcHCPVPort, dstHCPVPort, srcVPortMaxCapability));
                    internalLinks.add(HCPInternalLink.of(srcHCPVPort, HCPVport.LOCAL, srcVportLoadCapability));
                } else {
                    if (srcConnection.deviceId().equals(dstConnection.deviceId())) {
//                        long capability = 100000;
                        internalLinks.add(HCPInternalLink.of(srcHCPVPort, dstHCPVPort, (long)MAX_BANDWIDTH,0,0));
                    } else if (!pathService.getPaths(srcConnection.deviceId(), dstConnection.deviceId()).isEmpty()) {
                        internalLinks.add(VPortToVportHCPInernalink(srcConnection.deviceId(),dstConnection.deviceId(),srcHCPVPort,dstHCPVPort));
                    }
                }
            }
        }

        HCPTopologyReply topologyReply = hcpFactory.buildTopoReply()
                .setInternalLink(internalLinks)
                .build();
        domainController.write(topologyReply);

    }

    private class InternalHCPSuperMessageListener implements HCPSuperMessageListener {

        @Override
        public void handleIncommingMessage(HCPMessage message) {
            if (message.getType() != HCPType.HCP_TOPO_REQUEST)
                return;
            UpdateTopology();
        }

        @Override
        public void handleOutGoingMessage(List<HCPMessage> messages) {

        }
    }

    private class InternalHCPSuperControllerListener implements HCPSuperControllerListener {

        @Override
        public void connectToSuperController(HCPSuper hcpSuper) {
            log.info("333333333333333333333333333333");
            init();
        }


        @Override
        public void disconnectSuperController(HCPSuper hcpSuper) {

        }

    }

    private class InternalPacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext packetContext) {
            if (packetContext.isHandled()) {
                return;
            }

            Ethernet eth = packetContext.inPacket().parsed();
            if (eth == null || (eth.getEtherType() != Ethernet.TYPE_LLDP)) {
                return;
            }
            if (!domainController.isConnectToSuper()) {
                return;
            }
            HCPLLDP hcplldp = HCPLLDP.parseHCPLLDP(eth);
//            log.info("=============hcp lldp domainId:{},deviceId:{},portID:{},Vport:{}",
//                    hcplldp.getDomianId(),hcplldp.getDpid(),hcplldp.getPortNum(),hcplldp.getVportNum());
            if (hcplldp == null) {
                return;
            }
            PortNumber srcPort = PortNumber.portNumber(hcplldp.getPortNum());
            PortNumber dstPort = packetContext.inPacket().receivedFrom().port();
            DeviceId srcDeviceId = DeviceId.deviceId("pof:" + hcplldp.getDpid());
            DeviceId dstDeviceId = packetContext.inPacket().receivedFrom().deviceId();
            ConnectPoint edgeConnectPoint = new ConnectPoint(dstDeviceId, dstPort);

            //如果收到LLDP数据包中域ID和控制器的域ID相同，说明是在同一个域的设备
            if (hcplldp.getDomianId() == domainController.getDomainId().getLong()) {
                packetContext.block();
                return;
            }
//            log.info("=========================================================");
            //
            //如果Vport号是初始的oxffff，则说明对面控制器并没有发现Vport，则需要控制器重新构造
            //LLDP数据包发送给对端，让对端发现其是Vport（表示边界对外端口），并且上报给SuperController
            if (LLDP_VPORT_LOCAL == hcplldp.getVportNum()) {
                addOrUpdateVport(edgeConnectPoint, HCPVportState.LINK_UP, HCPVportReason.ADD);
                HCPLLDP replyhcplldp = HCPLLDP.hcplldp(Long.valueOf(dstDeviceId.toString().substring("pof:".length()),16),
                        Long.valueOf(dstPort.toLong()).intValue(),
                        domainController.getDomainId().getLong(),
                        Long.valueOf(getLogicalVportNumber(edgeConnectPoint).toLong()).intValue());
                Ethernet ethpacket = new Ethernet();
                ethpacket.setEtherType(Ethernet.TYPE_LLDP);
                ethpacket.setDestinationMACAddress(MacAddress.ONOS_LLDP);
                ethpacket.setPad(true);
                ethpacket.setSourceMACAddress(buildSrcMac());
                ethpacket.setPayload(replyhcplldp);
                OutboundPacket outboundPacket = new DefaultOutboundPacket(dstDeviceId
                        , builder().setOutput(dstPort).build(), ByteBuffer.wrap(ethpacket.serialize()));
                packetService.emit(outboundPacket);
                packetContext.block();
            } else {
//                log.info("=====================Sbp Message================");
                //如果lldp携带了Vport号，则说明对端控制器已经发现自己域的这个对外端口，
                // 则需要将LLDP数据包上报给SuperController，让SuperController发现域间链路
                PortNumber exitVportNumber = getVportNum(edgeConnectPoint);
                if (exitVportNumber == null) {
                    addOrUpdateVport(edgeConnectPoint, HCPVportState.LINK_UP, HCPVportReason.ADD);
                }

                //构造LLDP数据包，通过Sbp数据包中的的SbpCmpType中的PACKET_IN模式
                // 封装到Sbp数据中发送给SuperController
                HCPLLDP sbpHCPlldp = HCPLLDP.hcplldp(hcplldp.getDomianId(),
                        hcplldp.getVportNum(), hcplldp.getDomianId(),
                        hcplldp.getVportNum());
                Ethernet ethpacket = new Ethernet();
                ethpacket.setEtherType(Ethernet.TYPE_LLDP);
                ethpacket.setDestinationMACAddress(MacAddress.ONOS_LLDP);

                ethpacket.setPad(true);
                ethpacket.setSourceMACAddress(buildSrcMac());
                ethpacket.setPayload(sbpHCPlldp);
                byte[] frame = ethpacket.serialize();
                HCPPacketIn hcpPacketIn = HCPPacketInVer10.of((int) getVportNum(edgeConnectPoint).toLong(), frame);
                Set<HCPSbpFlags> flagsSet = new HashSet<>();
                flagsSet.add(HCPSbpFlags.DATA_EXITS);
                HCPSbp hcpSbp = hcpFactory.buildSbp()
                        .setSbpCmpType(HCPSbpCmpType.PACKET_IN)
                        .setFlags(flagsSet)
                        .setDataLength((short) hcpPacketIn.getData().length)
                        .setSbpXid(1)
                        .setSbpCmpData(hcpPacketIn)
                        .build();
                domainController.write(hcpSbp);
                packetContext.block();
            }
        }
    }

    private class InternalLinkListener implements LinkListener {

        @Override
        public void event(LinkEvent linkEvent) {
            //TODO
        }
    }

    class TopoUpdateTask implements Runnable {
        @Override
        public void run() {
            // update vport
            UpdateVportsToSuper();
            // update intra_links
//            StringBuffer StringBuffer=new StringBuffer();
//            for (ConnectPoint connectPoint:portBandwidth.keySet()){
//                if(portBandwidth.get(connectPoint)!=0.0){
//                    StringBuffer.append(connectPoint+"=");
//                    StringBuffer.append(portBandwidth.get(connectPoint)+",");
//                }
//            }
//            log.info("=====max_bandwidth={}======portBandwidth======{}",MAX_BANDWIDTH,StringBuffer.toString());
            UpdateTopology();
        }
    }

    class VportUpdateTask implements Runnable{

        @Override
        public void run() {
            Set<PortNumber> removePorts=new HashSet<>();
            for (ConnectPoint connectPoint:vportMap.keySet()){
                PortNumber portNumber=vportMap.get(connectPoint);
                if (isState(VportTimeMap.get(portNumber))){
                    removePorts.add(portNumber);
                }
            }
            for(PortNumber portNumber:removePorts){
                removeVport(portNumber);
            }
        }
        private boolean isState(long lastTime){
            return (System.currentTimeMillis()-lastTime)>STATE_VPORT_TIME?false:true;
        }
    }
}
