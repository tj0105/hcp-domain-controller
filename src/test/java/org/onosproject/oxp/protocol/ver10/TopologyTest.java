package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.hcp.exceptions.HCPParseError;
import org.onosproject.hcp.protocol.HCPMessage;
import org.onosproject.hcp.protocol.HCPTopologyReply;
import org.onosproject.hcp.protocol.HCPTopologyRequest;
import org.onosproject.hcp.protocol.ver10.HCPTopologyReplyVer10;
import org.onosproject.hcp.protocol.ver10.HCPTopologyRequestVer10;
import org.onosproject.hcp.types.HCPInternalLink;
import org.onosproject.hcp.types.HCPVport;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.onosproject.oxp.protocol.ver10.TestBaseVer10.getMessageFactry;
import static org.onosproject.oxp.protocol.ver10.TestBaseVer10.getMessageReader;
/**
 * @Author ldy
 * @Date: 20-3-27 下午9:33
 * @Version 1.0
 */
public class TopologyTest {
    @Test
    public void TopologyRequest() throws HCPParseError {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        HCPTopologyRequest topologyRequest = getMessageFactry()
                .buildTopoRequest()
                .build();
        topologyRequest.writeTo(buffer);
        assertThat(topologyRequest, instanceOf(HCPTopologyRequestVer10.class));

        HCPMessage message = getMessageReader().readFrom(buffer);
        assertThat(message, instanceOf(topologyRequest.getClass()));

        HCPTopologyRequest messageRev = (HCPTopologyRequest) message;
        System.out.println(messageRev.getType());
        System.out.println(messageRev.getXid());
        assertThat(topologyRequest, is(messageRev));

    }
    @Test
    public void TopologyReply() throws HCPParseError{
        ChannelBuffer buffer=ChannelBuffers.dynamicBuffer();
        List<HCPInternalLink> internalLinks=new ArrayList<>();
        HCPInternalLink internalLink=HCPInternalLink.of(HCPVport.ofShort((short)1),
                                        HCPVport.ofShort((short)2)
                                        ,100,10,2);
        internalLinks.add(internalLink);
        HCPTopologyReply topologyReply=getMessageFactry().buildTopoReply()
                .setInternalLink(internalLinks)
                .build();
        topologyReply.writeTo(buffer);

        assertThat(topologyReply,instanceOf(HCPTopologyReplyVer10.class));

        HCPMessage message=getMessageReader().readFrom(buffer);
        assertThat(message,instanceOf(topologyReply.getClass()));

        HCPTopologyReply messagev=(HCPTopologyReply)message;
        List<HCPInternalLink> link=messagev.getInternalLink();
        System.out.println(link.get(0).getBandwidthCapability());
        System.out.println(link.get(0).getHopCapability());
        System.out.println(link.get(0).getDelayCapability());
        System.out.println(messagev.getType());

    }
}
