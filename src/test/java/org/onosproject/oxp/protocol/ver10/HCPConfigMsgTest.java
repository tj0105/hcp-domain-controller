package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.hcp.protocol.HCPConfigFlags;
import org.onosproject.hcp.protocol.HCPGetConfigReply;
import org.onosproject.hcp.protocol.HCPGetConfigRequest;
import org.onosproject.hcp.protocol.HCPMessage;
import org.onosproject.hcp.protocol.ver10.HCPGetConfigReplyVer10;

import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @Author ldy
 * @Date: 20-2-27 下午10:55
 * @Version 1.0
 */

public class HCPConfigMsgTest extends TestBaseVer10{
    @Test
    public void HCPGetConfigRequest()throws Exception{
        ChannelBuffer channelBuffer= ChannelBuffers.dynamicBuffer();
        HCPGetConfigRequest configRequest=getMessageFactry()
                .buildGetConfitRequest()
                .build();
    }


    @Test
    public void HCPGetConfigReplyMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        Set<HCPConfigFlags> flags = EnumSet.noneOf(HCPConfigFlags.class);
//        flags.add(HCPConfigFlags.MODE_ADVANCED);
        flags.add(HCPConfigFlags.CAPABILITIES_BW);
        HCPGetConfigReply configReply = getMessageFactry()
                .buildGetConfigReply()
                .setFlags(flags)
                .build();
        configReply.writeTo(buffer);
        assertThat(configReply, instanceOf(HCPGetConfigReplyVer10.class));

        HCPMessage message = getMessageReader().readFrom(buffer);
        assertThat(message, instanceOf(configReply.getClass()));

        HCPGetConfigReply messageRev = (HCPGetConfigReply) message;
        System.out.println(messageRev.getFlags().toString());
        assertThat(configReply, is(messageRev));
    }
}
