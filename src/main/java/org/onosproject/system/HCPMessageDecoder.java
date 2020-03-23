package org.onosproject.system;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.onosproject.hcp.protocol.HCPFactories;
import org.onosproject.hcp.protocol.HCPMessage;
import org.onosproject.hcp.protocol.HCPMessageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author ldy
 * @Date: 20-2-29 下午4:11
 * @Version 1.0
 */
public class HCPMessageDecoder extends FrameDecoder {
    private static final Logger log= LoggerFactory.getLogger(HCPMessageDecoder.class);

    @Override
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, ChannelBuffer channelBuffer) throws Exception {
        if (!channel.isConnected()){
            return null;
        }
        HCPMessageReader<HCPMessage> reader= HCPFactories.getGenericReader();
        HCPMessage message=reader.readFrom(channelBuffer);
//        log.info("===============Decode Message========={}==========",message.getType());
        return message;
    }
}
