package org.onosproject.oxp.protocol.ver10;

import org.junit.Test;
import org.onlab.packet.HCPLLDP;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.net.Device;

/**
 * @Author ldy
 * @Date: 20-3-6 下午4:58
 * @Version 1.0
 */
public class HCPLLDPTest {
    private Device device;
    @Test
    public void HCPLLDPTest() throws Exception{
        String domainId="111";
        Long portNumber=new Long(1);
        DomainId domainId1= DomainId.of(111);

        HCPLLDP hcplldp = HCPLLDP.hcplldp(Long.valueOf("00000000001"),
                portNumber.intValue(),
                Long.valueOf(domainId),
                0xffff);
        System.out.println(hcplldp.getDpid());
        System.out.println(hcplldp.getPortNum());
        System.out.println(hcplldp.getVportNum());
        System.out.println(hcplldp.getDomianId());

//        System.out.println(Long.valueOf("domainId:              6f".substring("domainId:".length()).replace(" ",""), 16));
        System.out.println(domainId1.getLong());
    }
}
