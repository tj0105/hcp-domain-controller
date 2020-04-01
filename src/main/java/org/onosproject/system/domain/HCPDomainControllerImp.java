package org.onosproject.system.domain;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Ethernet;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.core.CoreService;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.DomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * @Author ldy
 * @Date: 20-2-29 下午4:38
 * @Version 1.0
 */
@Component(immediate =true)
@Service
public class HCPDomainControllerImp implements HCPDomainController{

   private static final Logger log= LoggerFactory.getLogger(HCPDomainControllerImp.class);

   private static final String APP_ID="org.onosproject.domain.system";

   protected String hcpSuperIp="127.0.0.1";
   protected int hcpSuperPort=8890;
   private Set<HCPConfigFlags> flags;
   private int period;
   private long missSendLength;
   private Set<HCPCapabilities> capabilities;
   private HCPSbpType hcpSbpType;
   private HCPSbpVersion hcpSbpVersion;
   private DomainId domainId;
   private HCPSuper hcpSuper;
   private HCPVersion hcpVersion;

   private DomainConnector domainConnector=new DomainConnector(this);

   private Set<HCPSuperMessageListener> hcpSuperMessageListeners=new CopyOnWriteArraySet<>();

   private Set<HCPSuperControllerListener> hcpSuperControllerListeners=new CopyOnWriteArraySet<>();
   @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
   protected CoreService coreService;

  @Activate
  public void activate(){
      this.setHCPVersion(HCPVersion.HCP_10);
      this.setDomainId(DomainId.of(1111));
      this.setHCPSuperIp("192.168.109.112");
      this.setHCPSuperPort(8899);
      this.setHCPSbpType(HCPSbpType.POF);
      this.setHCPSbpVersion((HCPSbpVersion.of((byte)4,HCPVersion.HCP_10)));
      Set<HCPCapabilities> capabilitie=new HashSet<>();
      capabilitie.add(HCPCapabilities.GROUP_STATS);
      capabilitie.add(HCPCapabilities.IP_REASM);
      capabilitie.add(HCPCapabilities.PORT_BLOCKED);
      capabilitie.add(HCPCapabilities.PORT_STATS);
      capabilitie.add(HCPCapabilities.QUEUE_STATS);
      capabilitie.add(HCPCapabilities.FLOW_STATS);
      capabilitie.add(HCPCapabilities.TABLE_STATS);
      this.SetCapabilities(capabilitie);
      Set<HCPConfigFlags> flags=new HashSet<>();
      flags.add(HCPConfigFlags.MODE_ADVANCED);
      flags.add(HCPConfigFlags.CAPABILITIES_HOP);
      this.setFlags(flags);
      this.setPeriod(5);
      this.setMissSendLength(128);
//      try {
//          Thread.sleep(1500);
//      } catch (InterruptedException e) {
//          e.printStackTrace();
//      }
      domainConnector.start();
      log.info("domain controller started");

  }

  @Deactivate
  public void deactivate(){
      hcpSuper=null;
      hcpSuperMessageListeners.clear();
      hcpSuperControllerListeners.clear();
      domainConnector.stop();
      log.info("Domain controller stoped");
  }
   @Override
    public void processMessage(HCPMessage message) {
        for (HCPSuperMessageListener listener:hcpSuperMessageListeners){
            listener.handleIncommingMessage(message);
        }
    }

    @Override
    public void processDownStreamMessage(List<HCPMessage> messages) {
        for(HCPSuperMessageListener listener:hcpSuperMessageListeners){
            listener.handleOutGoingMessage(messages);
        }
    }

    @Override
    public boolean connectToSuperController(HCPSuper hcpSuper) {
        this.hcpSuper=hcpSuper;
        for (HCPSuperControllerListener listener:hcpSuperControllerListeners)
            listener.connectToSuperController(hcpSuper);
        return true;
    }


    @Override
    public boolean isConnectToSuper() {
        if (null!=hcpSuper&&hcpSuper.isConnected())
            return true;
        return false;
    }

    @Override
    public void addMessageListener(HCPSuperMessageListener listener) {
        hcpSuperMessageListeners.add(listener);
    }

    @Override
    public void removeMessageListener(HCPSuperMessageListener listener) {
        hcpSuperMessageListeners.remove(listener);
    }

    @Override
    public void addHCPSuperControllerListener(HCPSuperControllerListener listener) {
        hcpSuperControllerListeners.add(listener);
    }

    @Override
    public void removeHCPSuperControllerListener(HCPSuperControllerListener listener) {
        hcpSuperControllerListeners.remove(listener);
    }

    @Override
    public void write(HCPMessage message) {
        if (null!=hcpSuper&&hcpSuper.isConnected()){
            hcpSuper.sendMsg(message);
        }
    }

    @Override
    public void processPacket(HCPMessage message) {

    }

    @Override
    public Set<HCPConfigFlags> getFlags() {
        return this.flags;
    }

    @Override
    public void setFlags(Set<HCPConfigFlags> flags) {
        this.flags=flags;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    @Override
    public void setPeriod(int period) {
        this.period=period;
    }

    @Override
    public long getMissSendLength() {
        return missSendLength;
    }

    @Override
    public void setMissSendLength(long missSendLength) {
        this.missSendLength=missSendLength;
    }

    @Override
    public Set<HCPCapabilities> getCapabilities() {
        return this.capabilities;
    }

    @Override
    public void SetCapabilities(Set<HCPCapabilities> capabilities) {
        this.capabilities=capabilities;
    }

    @Override
    public DomainId getDomainId() {
        return domainId;
    }

    @Override
    public void setDomainId(DomainId domainId) {
        this.domainId=domainId;
    }

    @Override
    public HCPSbpType getHCPSbpType() {
        return hcpSbpType;
    }

    @Override
    public void setHCPSbpType(HCPSbpType hcpSbpType) {
        this.hcpSbpType=hcpSbpType;
    }

    @Override
    public HCPSbpVersion getSbpVersion() {
        return hcpSbpVersion;
    }

    @Override
    public void setHCPSbpVersion(HCPSbpVersion hcpSbpVersion) {
        this.hcpSbpVersion=hcpSbpVersion;
    }

    @Override
    public int getHCPSuperPort() {
        return hcpSuperPort;
    }

    @Override
    public void setHCPSuperPort(int hcpSuperPort) {
        this.hcpSuperPort=hcpSuperPort;
    }

    @Override
    public String getHCPSuperIp() {
        return hcpSuperIp;
    }

    @Override
    public void setHCPSuperIp(String HCPSuperIp) {
        this.hcpSuperIp=HCPSuperIp;
    }

    @Override
    public HCPVersion getHCPVersion() {
        return hcpVersion;
    }

    @Override
    public void setHCPVersion(HCPVersion hcpVersion) {
        this.hcpVersion=hcpVersion;
    }

    @Override
    public Ethernet parseEthernet(byte[] data) {
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data);
        Ethernet eth = null;
        try {
            eth = Ethernet.deserializer().deserialize(buffer.array(), 0, buffer.readableBytes());
        } catch (DeserializationException e) {
            return null;
        }
        return eth;
    }
}
