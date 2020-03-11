package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.protocol.ver10.HCPFeaturesReplyVer10;
import org.onosproject.hcp.protocol.ver10.HCPHostRequestVer10;
import org.onosproject.hcp.types.DomainId;
import org.onosproject.system.domain.HCPDomainControllerImp;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @Author ldy
 * @Date: 20-3-9 下午11:52
 * @Version 1.0
 */
public class HCPFeaturesTest {
    private HCPDomainController domainController=new HCPDomainControllerImp();

    @Test
    public void HCPFeaturesReplyTest() throws Exception{
        ChannelBuffer channelBuffer= ChannelBuffers.dynamicBuffer();
        domainController.setDomainId(DomainId.of(1111));
        domainController.setHCPSbpType(HCPSbpType.POF);
        Set<HCPCapabilities> capabilitie=new HashSet<>();
        capabilitie.add(HCPCapabilities.GROUP_STATS);
        capabilitie.add(HCPCapabilities.IP_REASM);
        capabilitie.add(HCPCapabilities.PORT_BLOCKED);
        capabilitie.add(HCPCapabilities.PORT_STATS);
        capabilitie.add(HCPCapabilities.QUEUE_STATS);
        capabilitie.add(HCPCapabilities.FLOW_STATS);
        capabilitie.add(HCPCapabilities.TABLE_STATS);
        domainController.SetCapabilities(capabilitie);
        domainController.setHCPSbpVersion((HCPSbpVersion.of((byte)4,HCPVersion.HCP_10)));
        domainController.setHCPSbpType(HCPSbpType.POF);
        HCPFeaturesReply FeaturesReplymessage = TestBaseVer10.getMessageFactry().buildFeaturesReply()
                .setCapabilities(domainController.getCapabilities())
                .setDomainId(domainController.getDomainId())
                .setSbpType(domainController.getHCPSbpType())
                .setSbpVersion(domainController.getSbpVersion())
                .setXid(2)
                .build();
        FeaturesReplymessage.writeTo(channelBuffer);

        assertThat(FeaturesReplymessage,instanceOf(HCPFeaturesReplyVer10.class));

        HCPMessage message=TestBaseVer10.getMessageReader().readFrom(channelBuffer);
        assertThat(message,instanceOf(FeaturesReplymessage.getClass()));

        HCPFeaturesReply messageRev=(HCPFeaturesReply) message;
        assertThat(messageRev,is(FeaturesReplymessage));

        System.out.println(messageRev.getDomainId().toString());
        System.out.println(messageRev.getSbpType());
        System.out.println(messageRev.getCapabilities());

    }
}
