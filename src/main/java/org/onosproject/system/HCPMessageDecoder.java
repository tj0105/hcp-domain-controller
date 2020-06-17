package org.onosproject.system;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.CompositeChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.onosproject.hcp.protocol.HCPFactories;
import org.onosproject.hcp.protocol.HCPMessage;
import org.onosproject.hcp.protocol.HCPMessageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-29 下午4:11
 * @Version 1.0
 */
public class HCPMessageDecoder extends FrameDecoder {
    private static final Logger log= LoggerFactory.getLogger(HCPMessageDecoder.class);

    @Override
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, ChannelBuffer channelBuffer) throws Exception {
        if (!channel.isConnected()) {
            return null;
        }
//        log.info("====================channelBuffer======{}====",channelBuffer.toString());
//        HCPMessageReader<HCPMessage> reader = HCPFactories.getGenericReader();
//        if (channelBuffer instanceof CompositeChannelBuffer) {
//            channelBuffer.readByte();
//        }
//        List<HCPMessage> messageList = new ArrayList<>();
//        if (channelBuffer.readableBytes() < 20) {
//            HCPMessage message = reader.readFrom(channelBuffer);
//            messageList.add(message);
//        } else {
//            while (channelBuffer.readableBytes() >= 8) {
//                int startIndex = channelBuffer.readerIndex();
//                byte version = channelBuffer.readByte();
//                if (version != 1) {
//
//                    continue;
//                }
////            startIndex=channelBuffer.readerIndex();
//                byte type = channelBuffer.readByte();
//                if (type != 15) {
//                    channelBuffer.readerIndex(startIndex + 1);
//                    continue;
//                }
////                int length=channelBuffer.readInt();
////                if (length)
//                channelBuffer.readerIndex(startIndex);
//                channelBuffer.markReaderIndex();
//                HCPMessage message = reader.readFrom(channelBuffer);
//                if (message == null) {
//                    channelBuffer.resetReaderIndex();
//                    break;
//                }
//                messageList.add(message);
//            }
//        }
////        channelBuffer.clear();
////        log.info("===============Decode Message========={}==========",message.getType());
//        return messageList.size() == 0 ? null : messageList;
        HCPMessageReader<HCPMessage> reader= HCPFactories.getGenericReader();
        HCPMessage message=reader.readFrom(channelBuffer);
//        log.info("===============Decode Message========={}==========",message.getType());
        return message;
    }
}
