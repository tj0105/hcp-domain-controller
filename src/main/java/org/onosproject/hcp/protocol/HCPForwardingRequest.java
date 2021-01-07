package org.onosproject.hcp.protocol;

import org.onosproject.hcp.types.*;

import java.util.List;

public interface HCPForwardingRequest extends HCPSbpCmpData{
    HCPFlowType getFLowType();
    HCPIOT getSrcIoT();
    HCPIOT getDstIoT();
    IPv4Address getSrcIpAddress();
    IPv4Address getDstIpAddress();
    int getInport();
    IPv4Address getMask();
    short getEthType();
    byte getQos();
    List<HCPVportHop> getvportHopList();
}
