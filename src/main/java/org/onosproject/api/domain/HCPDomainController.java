package org.onosproject.api.domain;

import org.onlab.packet.Ethernet;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.Super.HCPSuperControllerListener;
import org.onosproject.api.HCPSuperMessageListener;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.DomainId;

import java.util.List;
import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-2-28 下午10:08
 * @Version 1.0
 */
public interface HCPDomainController {
    /**
     * processMessage called by HCPSuper
     * @param  message
     */
    void processMessage(HCPMessage message);

    void processDownStreamMessage(List<HCPMessage> messages);

    /**
     *
     * @param hcpSuper
     * @return
     */
    boolean connectToSuperController(HCPSuper hcpSuper);

    boolean isConnectToSuper();

    void addMessageListener(HCPSuperMessageListener listener);

    void removeMessageListener(HCPSuperMessageListener listener);

    void addHCPSuperControllerListener(HCPSuperControllerListener listener);

    void removeHCPSuperControllerListener(HCPSuperControllerListener listener);
    /**
     * send a message to HCPSuperController
     * @param message
     */
    void write(HCPMessage message);

    void processPacket(HCPMessage message);

    Set<HCPConfigFlags> getFlags();
    void setFlags(Set<HCPConfigFlags> flags);

    int getPeriod();
    void setPeriod(int period);

    long getMissSendLength();
    void setMissSendLength(long missSendLength);

    Set<HCPCapabilities> getCapabilities();
    void SetCapabilities(Set<HCPCapabilities> capabilities);

    DomainId getDomainId();
    void setDomainId(DomainId domainId);

    HCPSbpType getHCPSbpType();
    void setHCPSbpType(HCPSbpType hcpSbpType);

    HCPSbpVersion getSbpVersion();
    void setHCPSbpVersion(HCPSbpVersion hcpSbpVersion);

    int getHCPSuperPort();
    void setHCPSuperPort(int hcpSuperPort);

    String getHCPSuperIp();
    void setHCPSuperIp(String HCPSuperIp);

    HCPVersion getHCPVersion();
    void setHCPVersion(HCPVersion hcpVersion);

    Ethernet parseEthernet(byte []data);

}
