package org.onosproject.api.domain;

import org.onlab.packet.IpAddress;
import org.onosproject.hcp.types.HCPVport;
import org.onosproject.net.Path;

import java.util.Map;

/**
 * @Author ldy
 * @Date: 2021/1/20 下午5:21
 * @Version 1.0
 */
public interface HCPDomainRouteService {
    Map<HCPVport, Path> getVportPathByIP(IpAddress ipAddress);
}
