package org.onosproject.hcp.protocol;

import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.HCPVport;
import org.onosproject.hcp.types.IPv4Address;

public interface HCPForwardingReply extends HCPSbpCmpData {
    IPv4Address getSrcIpAddress();
    IPv4Address getDstIpAddress();
    HCPVport getSrcVport();
    HCPVport getDstVport();
    short getEthType();
    byte getQos();
    DomainId getDomainId();
}
