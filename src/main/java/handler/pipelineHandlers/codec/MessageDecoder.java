package handler.pipelineHandlers.codec;

import config.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import pojo.protocal.Message;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        Message msg = new Message();
        msg.setHead1(byteBuf.readByte());
        msg.setHead2(byteBuf.readByte());
        byte[] dataBytes = new byte[byteBuf.readableBytes() - Constants.MESSAGE_HEAD1_LENGTH - Constants.MESSAGE_HEAD2_LENGTH];
        byteBuf.readBytes(dataBytes);
        msg.setData(dataBytes);
        list.add(msg);
    }
}
