package handler.pipelineHandlers;

import config.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Spliter extends LengthFieldBasedFrameDecoder {

    public Spliter() {
        super(Constants.MESSAGE_MAX_LENGTH, Constants.MESSAGE_LENGTH_FIELD_OFFSET, Constants.MESSAGE_LENGTH_FIELD_LENGTH, 0, Constants.MESSAGE_LENGTH_FIELD_LENGTH);
    }
}
