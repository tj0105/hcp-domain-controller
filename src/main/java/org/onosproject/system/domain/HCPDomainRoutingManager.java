package org.onosproject.system.domain;



import com.google.common.collect.Table;
import org.apache.felix.scr.annotations.*;
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
import org.onosproject.floodlightpof.protocol.table.OFFlowTable;
import org.onosproject.floodlightpof.protocol.table.OFTableType;
import org.onosproject.hcp.protocol.*;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.table.*;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected FlowTableStore tableStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableService flowTableService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    private PacketProcessor packetProcessor=new ReactivePacketProcessor();
    private HCPSuperMessageListener hcpSuperMessageListener=new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener=new InternalHCPSuperControllerListener();

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
        flowRuleService.removeFlowRulesById(applicationId);
        flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(tableId));

    }
    private class ReactivePacketProcessor implements PacketProcessor{

        @Override
        public void process(PacketContext packetContext) {

        }
    }
    private class InternalHCPSuperMessageListener implements HCPSuperMessageListener{

        @Override
        public void handleIncommingMessage(HCPMessage message) {
                if (message.getType()!= HCPType.HCP_SBP){
                    return ;
                }

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
}
