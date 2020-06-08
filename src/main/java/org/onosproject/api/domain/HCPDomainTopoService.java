package org.onosproject.api.domain;

import org.onosproject.hcp.types.HCPVport;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-3-3 下午11:32
 * @Version 1.0
 */
public interface HCPDomainTopoService {
    PortNumber getLogicalVportNumber(ConnectPoint connectPoint);

    boolean isOuterPort(ConnectPoint connectPoint);

    ConnectPoint getLocationByVport(PortNumber portNumber);

    Set<ConnectPoint> getVPortConnectPoint();

    Path getVportToVportPath(HCPVport srcVport, HCPVport dstVport);

    long getVportMaxCapability(ConnectPoint connectPoint);

    long getVportLoadCapability(ConnectPoint connectPoint);

    long getResetVportCapability(ConnectPoint connectPoint);

    Map<ConnectPoint,PortNumber> getAllVport();
}
