package org.onosproject.hcp.protocol;

import org.onosproject.hcp.types.IPv4Address;

import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-3-29 下午11:04
 * @Version 1.0
 */
public interface HCPResourceRequest extends HCPSbpCmpData{
    IPv4Address getSrcIpAddress();
    IPv4Address getDstIpAddress();
    Set<HCPConfigFlags> getFlags();
}
