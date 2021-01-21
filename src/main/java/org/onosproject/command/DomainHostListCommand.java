package org.onosproject.command;

import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onosproject.api.domain.HCPDomainRouteService;
import org.onosproject.api.domain.HCPDomainTopoService;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.hcp.types.HCPVport;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.host.HostService;

import java.util.Map;
import java.util.Set;

/**
 * @Author ldy
 * @Date: 20-6-7 上午1:17
 * @Version 1.0
 */

@Command(scope = "onos", name = "hcp-host",
        description = "Sample Apache Karaf CLI command")
public class DomainHostListCommand extends AbstractShellCommand{
    private HCPDomainTopoService topoService;
    private HostService hostServices;
    private HCPDomainRouteService routeService;
    private static final String FMT=
            "Ipaddress=%s, SrcConnectPoint=%s, Vport=%s, DstConnectPoint=%s, Path=%s";
    @Override
    protected void execute() {
        topoService = AbstractShellCommand.get(HCPDomainTopoService.class);
        hostServices = AbstractShellCommand.get(HostService.class);
        routeService = AbstractShellCommand.get(HCPDomainRouteService.class);
        Iterable<Host> hosts = hostServices.getHosts();
        if (hostServices.getHostCount() > 0){
            for (Host host: hosts) {
                ConnectPoint srcConnectPoint = host.location();
                IpAddress ipAddress = (IpAddress) host.ipAddresses().toArray()[0];
                Map<ConnectPoint,PortNumber> vportMap = topoService.getAllVport();
                Map<HCPVport, Path> pathMap = routeService.getVportPathByIP(ipAddress);
                for (ConnectPoint connectPoint: vportMap.keySet()) {
                    HCPVport hcpVport = HCPVport.ofShort((short) vportMap.get(connectPoint).toLong());
                    if (pathMap.containsKey(hcpVport)){
                        print(FMT,ipAddress.toString(),srcConnectPoint.toString(),hcpVport.toString(),
                                connectPoint.toString(),pathMap.get(hcpVport).toString());
                    }else{
                        print(FMT,ipAddress.toString(),srcConnectPoint.toString(),hcpVport.toString(),connectPoint.toString(),"null");
                    }
                }

            }
        }
    }
}
