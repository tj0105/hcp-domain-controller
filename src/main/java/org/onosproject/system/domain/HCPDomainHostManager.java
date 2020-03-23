package org.onosproject.system.domain;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.IpAddress;
import org.onlab.util.Tools;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.HCPHost;
import org.onosproject.hcp.types.IPv4Address;
import org.onosproject.hcp.types.MacAddress;
import org.onosproject.net.Host;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
public class HCPDomainHostManager {
    private static final Logger log= LoggerFactory.getLogger(HCPDomainHostManager.class);

    private HCPVersion hcpVersion;
    private HCPFactory hcpFactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HCPDomainController domainController;

    private HostListener hostListener=new InternalHostListener();
    private HCPSuperMessageListener hcpSuperMessageListener=new InternalHCPSuperMessageListener();
    private HCPSuperControllerListener hcpSuperControllerListener=new InternalHCPSuperControllerListener();

    private ScheduledExecutorService executorService;
    //判断是否 connect SuperController
    private boolean flag=false;

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
        log.info("============Domain HostManager stoped===========");
    }

    public void init(){
        flag=true;
        hcpVersion=domainController.getHCPVersion();
        hcpFactory=HCPFactories.getFactory(hcpVersion);
        executorService= Executors.newSingleThreadScheduledExecutor
                (Tools.groupedThreads("hcp/hostupdate", "hcp-hostupdate-%d", log));
        executorService.scheduleAtFixedRate(new HostUpdateTesk(),domainController.getPeriod(),domainController.getPeriod(), TimeUnit.SECONDS);
        domainController.addMessageListener(hcpSuperMessageListener);
        hostService.addListener(hostListener);
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

    private class InternalHostListener implements HostListener {
        @Override
        public void event(HostEvent event) {
            Host updatedHost = null;
            Host removedHost = null;
            List<HCPHost> oxpHosts = new ArrayList<>();
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
                oxpHosts.addAll(toHCPHosts(removedHost,HCPHostState.INACTIVE));
            }
            if (null != updatedHost) {
                oxpHosts.addAll(toHCPHosts(updatedHost,HCPHostState.ACTIVE));
            }
            sendHostChangeMessage(oxpHosts,null);
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

    class HostUpdateTesk implements Runnable{

        @Override
        public void run() {
            updateExisHosts(null);
        }
    }
}
