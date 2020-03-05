package org.onosproject.system.domain;



import org.apache.felix.scr.annotations.*;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.domain.HCPDomainTopoService;
import org.onosproject.hcp.protocol.*;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-3-3 下午11:40
 * @Version 1.0
 */
@Component(immediate = true)
public class HCPDomainRoutingManager {
    private static final Logger log= LoggerFactory.getLogger(HCPDomainRoutingManager.class);


    private HCPVersion hcpVersion;
    private HCPFactory hcpfactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostServices;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainController domainController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainTopoService hcpDomainTopoService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EdgePortService edgePortService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    private PacketProcessor packetProcessor=new ReactivePacketProcessor();
    private HCPSuperMessageListener hcpSuperMessageListener=new InternalHCPSuperMessageListener();

    @Activate
    public void activate(){
        hcpVersion=domainController.getHCPVersion();
        hcpfactory= HCPFactories.getFactory(hcpVersion);
        packetService.addProcessor(packetProcessor,PacketProcessor.director(4));
        domainController.addMessageListener(hcpSuperMessageListener);
        log.info("=======================HCP Domain Routing Manager================");
    }

    @Deactivate
    public void deactivate(){
        packetService.removeProcessor(packetProcessor);
        domainController.removeMessageListener(hcpSuperMessageListener);
        log.info("=======================HCP Domain Routing Manager Stopped");
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
}
