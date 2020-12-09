package handler.pipelineHandlers.codec;

import config.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import pojo.protocal.Message;

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) {
        System.out.println("give : " + channelHandlerContext.channel());
        log.info(message.toString());
        int len = Constants.MESSAGE_HEAD1_LENGTH + Constants.MESSAGE_HEAD2_LENGTH + message.getData().length;
        byteBuf.writeInt(len);
        byteBuf.writeByte(message.getHead1());
        byteBuf.writeByte(message.getHead2());
        byteBuf.writeBytes(message.getData());
    }
}
