package handler.pipelineHandlers.codec;

import config.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import pojo.protocal.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        System.out.println("get : " + channelHandlerContext.channel());
        if (byteBuf.readableBytes() < 2)
            return;
        Message msg = new Message();
        byte[] dataBytes = new byte[byteBuf.readableBytes() - Constants.MESSAGE_HEAD1_LENGTH - Constants.MESSAGE_HEAD2_LENGTH];
        msg.setHead1(byteBuf.readByte());
        msg.setHead2(byteBuf.readByte());
        byteBuf.readBytes(dataBytes);
        msg.setData(dataBytes);
        log.info(msg.toString());
        list.add(msg);
    }
}
