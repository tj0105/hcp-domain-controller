package org.onosproject.oxp.protocol.ver10;

import jdk.internal.dynalink.linker.LinkerServices;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPHostReplyVer10;
import org.onosproject.hcp.protocol.ver10.HCPHostRequestVer10;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.HCPHost;
import org.onosproject.hcp.types.IPv4Address;
import org.onosproject.hcp.types.MacAddress;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @Author ldy
 * @Date: 20-3-7 下午9:43
 * @Version 1.0
 */
public class HCPHostMsgTest {
    @Test
    public void HCPHostRequestTest() throws Exception{
        ChannelBuffer channelBuffer= ChannelBuffers.dynamicBuffer();
        HCPHostRequest hcpHostRequest=TestBaseVer10.getMessageFactry()
                .buildHostRequest()
                .build();
        hcpHostRequest.writeTo(channelBuffer);
        assertThat(hcpHostRequest,instanceOf(HCPHostRequestVer10.class));

        HCPMessage message=TestBaseVer10.getMessageReader().readFrom(channelBuffer);
        assertThat(message,instanceOf(hcpHostRequest.getClass()));

        HCPHostRequest messageRev=(HCPHostRequest) message;
        assertThat(messageRev,is(hcpHostRequest));
    }

    @Test
    public void HCPHostReply() throws Exception{
        ChannelBuffer channelBuffers=ChannelBuffers.dynamicBuffer();
        IPv4Address iPv4Address=IPv4Address.of("192.168.109.112");
        MacAddress macAddress=MacAddress.of(org.onlab.packet.MacAddress.ONOS_LLDP.toString());
        HCPHostState hcpHostState=HCPHostState.ACTIVE;
        List<HCPHost> hcpHostList=new ArrayList<>();
        hcpHostList.add(HCPHost.of(iPv4Address,macAddress,hcpHostState));
        HCPHostReply hcpHostReply=TestBaseVer10.getMessageFactry()
                .buildHostReply()
                .setDomainId(DomainId.of(1111))
                .setHosts(hcpHostList)
                .build();
        hcpHostReply.writeTo(channelBuffers);

        assertThat(hcpHostReply,instanceOf(HCPHostReplyVer10.class));

        HCPMessage hcpMessage=TestBaseVer10.getMessageReader().readFrom(channelBuffers);
        System.out.println(hcpMessage.getType());
        System.out.println(hcpMessage.getXid());
        assertThat(hcpMessage,instanceOf(hcpHostReply.getClass()));
        HCPHostReply hcpHostReply1=(HCPHostReply)hcpMessage;
        System.out.println(hcpHostReply1.getDomainId().getLong());
        System.out.println(hcpHostReply1.getHosts().get(0).getiPv4Address());
        System.out.println(hcpHostReply1.getHosts().get(0).getHostState());
        System.out.println(hcpHostReply1.getHosts().get(0).getMacAddress());
        assertThat(hcpHostReply1,is(hcpHostReply));
    }
    @Test
    public void HCPHostUpdateTest() throws Exception{
        ChannelBuffer channelBuffers=ChannelBuffers.dynamicBuffer();
        IPv4Address iPv4Address=IPv4Address.of("192.168.109.112");
        MacAddress macAddress=MacAddress.of(org.onlab.packet.MacAddress.ONOS_LLDP.toString());
        HCPHostState hcpHostState=HCPHostState.ACTIVE;
        List<HCPHost> hcpHostList=new ArrayList<>();
        hcpHostList.add(HCPHost.of(iPv4Address,macAddress,hcpHostState));
        HCPHostUpdate hcpHostUpdate=TestBaseVer10.getMessageFactry()
                .buildHostUpdate()
                .setDomainId(DomainId.of(1111))
                .setHosts(hcpHostList)
                .build();
        hcpHostUpdate.writeTo(channelBuffers);

        assertThat(hcpHostUpdate,instanceOf(HCPHostUpdate.class));

        HCPMessage hcpMessage=TestBaseVer10.getMessageReader().readFrom(channelBuffers);
        System.out.println(hcpMessage.getType());
        System.out.println(hcpMessage.getXid());
        assertThat(hcpMessage,instanceOf(hcpHostUpdate.getClass()));

        HCPHostUpdate messageRev=(HCPHostUpdate) hcpMessage;
        System.out.println(messageRev.getDomainId().getLong());
        System.out.println(messageRev.getHosts().get(0).getiPv4Address());
        System.out.println(messageRev.getHosts().get(0).getHostState());
        System.out.println(messageRev.getHosts().get(0).getMacAddress());
        assertThat(messageRev,is(hcpHostUpdate));
    }
}
