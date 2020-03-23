package org.onosproject.api.Super;

import org.onlab.packet.Ethernet;
import org.onosproject.api.HCPDomain;
import org.onosproject.api.HCPDomainMessageListener;
import org.onosproject.api.domain.HCPDomainListener;
import org.onosproject.hcp.protocol.HCPMessage;
import org.onosproject.hcp.protocol.HCPVersion;
import org.onosproject.hcp.types.DomainId;

import javax.validation.constraints.Max;
import java.util.List;

/**
 * @Author ldy
 * @Date: 20-3-4 下午9:12
 * @Version 1.0
 */
public interface HCPSuperController {
    HCPVersion getVersion();
    void setHCPVersion(HCPVersion hcpVersion);

    int getHCPSuperPort();
    void setHCPSuperPort(int hcpSuperPort);

    String getHCPSuperIp();
    void setHCPSuperIp(String hcpSuperIp);

    void addMessageListener(HCPDomainMessageListener listener);
    void removeMessageListener(HCPDomainMessageListener listener);

    void addHCPDomainListener(HCPDomainListener listener);
    void removeHCPDomainListener(HCPDomainListener listener);

    void sendHCPMessge(DomainId domainId, HCPMessage message);

    void addDomain(DomainId domainId,HCPDomain domain);
    void removeDomain(DomainId domainId);

    void processDownstremMessage(DomainId domainId,List<HCPMessage> messages);
    void processMessage(DomainId domainId,HCPMessage message);

    HCPDomain getHCPDomain(DomainId domainId);



}
