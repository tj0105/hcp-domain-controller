package org.onosproject.hcp.protocol;

import org.onosproject.hcp.types.HCPVportHop;
import org.onosproject.hcp.types.IPv4Address;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-3-29 下午11:05
 * @Version 1.0
 */
public interface HCPResourceReply extends HCPSbpCmpData{
    IPv4Address getDstIpAddress();
    List<HCPVportHop> getvportHopList();
}
