package handlers;

import info.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import protocal.Message;

import java.util.List;

public class PushDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        Object decoded = decode(byteBuf);
        list.add(decoded);
    }

    private Object decode(ByteBuf in) {
        Message msg = new Message();
        msg.setHead1(in.readByte());
        msg.setHead2(in.readByte());
        byte[] dataBytes = new byte[in.readableBytes() - Constants.MESSAGE_HEAD1_LENGTH - Constants.MESSAGE_HEAD2_LENGTH];
        in.readBytes(dataBytes);
        msg.setData(dataBytes);
        return msg;
    }
}
