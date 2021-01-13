package org.onosproject.api.domain;

import org.onosproject.hcp.types.HCPIOT;
import org.onosproject.hcp.types.HCPIOTID;
import org.onosproject.net.ConnectPoint;

/**
 * @Author ldy
 * @Date: 2021/1/12 下午3:46
 * @Version 1.0
 */
public interface HCPDomainHostService {
    ConnectPoint getConnectionByIoTId(HCPIOTID hcpiotid);
    HCPIOT getHCPIOTByIoTId(HCPIOTID hcpiotid);
}
