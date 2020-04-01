package org.onosproject.hcp.protocol;

import org.onosproject.hcp.types.HCPSbpData;
import org.onosproject.hcp.types.HCPVportHop;
import org.onosproject.hcp.types.IPv4Address;

import java.util.List;

public interface HCPForwardingRequest extends HCPSbpCmpData{
    IPv4Address getSrcIpAddress();
    IPv4Address getDstIpAddress();
    int getInport();
    IPv4Address getMask();
    short getEthType();
    byte getQos();
    List<HCPVportHop> getvportHopList();
}
