package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPIoTReplyVer10;
import org.onosproject.hcp.protocol.ver10.HCPIoTRequestVer10;
import org.onosproject.hcp.protocol.ver10.HCPIoTUpdateVer10;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.hcp.types.HCPIOT;
import org.onosproject.hcp.types.HCPIOTID;
import org.onosproject.hcp.types.IPv4Address;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
/**
 * @Author ldy
 * @Date: 2021/1/6 下午12:16
 * @Version 1.0
 */
public class HCPIoTMsgTest {
    @Test
    public void HCPIoTRequestTest() throws Exception{
        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
        HCPIoTRequest hcpIoTRequest = TestBaseVer10.getMessageFactry()
                .buildIoTRequest().build();
        hcpIoTRequest.writeTo(channelBuffer);
        assertThat(hcpIoTRequest,instanceOf(HCPIoTRequestVer10.class));

        HCPMessage message = TestBaseVer10.getMessageReader().readFrom(channelBuffer);
        assertThat(message, instanceOf(hcpIoTRequest.getClass()));

        HCPIoTRequest messageRev = (HCPIoTRequest) message;
        System.out.println(messageRev.getType());
        System.out.println(messageRev.getVersion());
        System.out.println(messageRev.getXid());
        assertThat(messageRev, is(hcpIoTRequest));

    }

    @Test
    public void HCPIoTReply() throws Exception{
        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
        IPv4Address iPv4Address = IPv4Address.of("10.0.0.1");
        HCPIoTType hcpIoTType = HCPIoTType.IOT_EPC;
        HCPIOTID hcpiotid = HCPIOTID.of("ABCDEFG1234567");
        HCPIoTState hcpIoTState = HCPIoTState.ACTIVE;
        IPv4Address iPv4Address1 = IPv4Address.of("10.0.0.1");
        HCPIoTType hcpIoTType1 = HCPIoTType.IOT_ECODE;
        HCPIOTID hcpiotid1 = HCPIOTID.of("234567ABCDEFG");
        HCPIoTState hcpIoTState1 = HCPIoTState.INACTIVE;
        List<HCPIOT> iots = new ArrayList<>();
        iots.add(HCPIOT.of(iPv4Address,hcpIoTType,hcpiotid,hcpIoTState));
        iots.add(HCPIOT.of(iPv4Address1,hcpIoTType1,hcpiotid1,hcpIoTState1));
        HCPIoTReply hcpIoTReply = TestBaseVer10.getMessageFactry()
                .buildIoTReply()
                .setDomainId(DomainId.of(1111))
                .setIoTs(iots)
                .build();
        hcpIoTReply.writeTo(channelBuffer);

        assertThat(hcpIoTReply,instanceOf(HCPIoTReplyVer10.class));

        HCPMessage hcpMessage = TestBaseVer10.getMessageReader().readFrom(channelBuffer);
        System.out.println(hcpMessage.getType());
        System.out.println(hcpMessage.getXid());
        assertThat(hcpMessage,instanceOf(hcpIoTReply.getClass()));

        HCPIoTReply hcpIoTReply1 = (HCPIoTReply) hcpMessage;
        System.out.println(hcpIoTReply1.getDomainId());
        System.out.println("====================this is first iot=====================");
        System.out.println(hcpIoTReply1.getIoTs().get(0).getiPv4Address());
        System.out.println(hcpIoTReply1.getIoTs().get(0).getIoTType());
        System.out.println(hcpIoTReply1.getIoTs().get(0).getHcpiotid());
        System.out.println(hcpIoTReply1.getIoTs().get(0).getHCPIoTState());
        System.out.println("=====================this is second iot=====================");
        System.out.println(hcpIoTReply1.getIoTs().get(1).getiPv4Address());
        System.out.println(hcpIoTReply1.getIoTs().get(1).getIoTType());
        System.out.println(hcpIoTReply1.getIoTs().get(1).getHcpiotid());
        System.out.println(hcpIoTReply1.getIoTs().get(1).getHCPIoTState());
        assertThat(hcpIoTReply1,is(hcpIoTReply));
    }

    @Test
    public void HCPIoTUpdate() throws Exception{
        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
        IPv4Address iPv4Address = IPv4Address.of("10.0.0.1");
        HCPIoTType hcpIoTType = HCPIoTType.IOT_EPC;
        HCPIOTID hcpiotid = HCPIOTID.of("ABCDEFG1234567");
        IPv4Address iPv4Address1 = IPv4Address.of("10.0.0.1");
        HCPIoTState hcpIoTState = HCPIoTState.ACTIVE;
        HCPIoTType hcpIoTType1 = HCPIoTType.IOT_ECODE;
        HCPIOTID hcpiotid1 = HCPIOTID.of("234567ABCDEFG");
        HCPIoTState hcpIoTState1 = HCPIoTState.INACTIVE;
        List<HCPIOT> iots = new ArrayList<>();
        iots.add(HCPIOT.of(iPv4Address,hcpIoTType,hcpiotid,hcpIoTState));
        iots.add(HCPIOT.of(iPv4Address1,hcpIoTType1,hcpiotid1,hcpIoTState1));
        HCPIoTUpdate hcpIoTUpdate = TestBaseVer10.getMessageFactry()
                .buildIoTUpdate()
                .setDomainId(DomainId.of(1111))
                .setIoTs(iots)
                .build();
        hcpIoTUpdate.writeTo(channelBuffer);

        assertThat(hcpIoTUpdate,instanceOf(HCPIoTUpdateVer10.class));

        HCPMessage hcpMessage = TestBaseVer10.getMessageReader().readFrom(channelBuffer);
        System.out.println(hcpMessage.getType());
        System.out.println(hcpMessage.getXid());
        assertThat(hcpMessage,instanceOf(hcpIoTUpdate.getClass()));

        HCPIoTUpdate hcpIoTReply1 = (HCPIoTUpdate) hcpMessage;
        System.out.println(hcpIoTReply1.getDomainId());
        System.out.println("====================this is first iot=====================");
        System.out.println(hcpIoTReply1.getIoTs().get(0).getiPv4Address());
        System.out.println(hcpIoTReply1.getIoTs().get(0).getIoTType());
        System.out.println(hcpIoTReply1.getIoTs().get(0).getHcpiotid());
        System.out.println(hcpIoTReply1.getIoTs().get(0).getHCPIoTState());
        System.out.println("=====================this is second iot=====================");
        System.out.println(hcpIoTReply1.getIoTs().get(1).getiPv4Address());
        System.out.println(hcpIoTReply1.getIoTs().get(1).getIoTType());
        System.out.println(hcpIoTReply1.getIoTs().get(1).getHcpiotid());
        System.out.println(hcpIoTReply1.getIoTs().get(1).getHCPIoTState());
        assertThat(hcpIoTReply1,is(hcpIoTUpdate));
    }
    @Test
    public void HCPIotTest() throws Exception{
        HCPIOTID hcpiotid = HCPIOTID.of("ABCDEFG1234567");
        HCPIOTID hcpiotid1 = HCPIOTID.of("ABCDEFG1234567");
        System.out.println(hcpiotid1 == hcpiotid);
        System.out.println(hcpiotid1.equals(hcpiotid));
    }
}
