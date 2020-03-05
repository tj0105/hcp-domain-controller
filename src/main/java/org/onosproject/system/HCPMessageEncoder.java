package org.onosproject.system;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.onosproject.hcp.protocol.HCPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-29 下午4:33
 * @Version 1.0
 */
public class HCPMessageEncoder extends OneToOneEncoder{

    private static final Logger log= LoggerFactory.getLogger(HCPMessageEncoder.class);
    @Override
    protected Object encode(ChannelHandlerContext channelHandlerContext, Channel channel, Object message) throws Exception {
        if (!(message instanceof List)){
            return message;
        }
        List<HCPMessage> msgList=(List<HCPMessage>) message;

        ChannelBuffer buffer= ChannelBuffers.dynamicBuffer();
        for(HCPMessage hcpMessage:msgList){
            if (hcpMessage!=null){
//                log.info("===============Encode Message========={}==========",hcpMessage.getType());
                hcpMessage.writeTo(buffer);
            }
        }
        return buffer;
    }
}
