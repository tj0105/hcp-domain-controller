package org.onosproject.command;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.api.domain.HCPDomainTopoService;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.PortNumber;

import java.net.ConnectException;
import java.util.Map;

/**
 * @Author ldy
 * @Date: 20-6-7 上午1:17
 * @Version 1.0
 */

@Command(scope = "onos", name = "hcp-vport",
        description = "Sample Apache Karaf CLI command")
public class DomainVportListCommand extends AbstractShellCommand{
    private HCPDomainTopoService topoService;
    private static final String FMT="Connection=%s VportPortNumber=%s";
    @Override
    protected void execute() {
        topoService=AbstractShellCommand.get(HCPDomainTopoService.class);
        Map<ConnectPoint,PortNumber> connectPointPortNumberMap=topoService.getAllVport();
        if (connectPointPortNumberMap.isEmpty()){
            print("vport is null");
        }else{
            for (ConnectPoint connectPoint:connectPointPortNumberMap.keySet()){
                print(FMT,connectPoint.toString(),connectPointPortNumberMap.get(connectPoint).toString());
            }
        }
    }
}
