package handler.pipelineHandlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
@ChannelHandler.Sharable
public class ChannelManager extends ChannelInboundHandlerAdapter {

    private static ChannelManager instance = null;

    public synchronized static ChannelManager getInstance() {
        if (instance == null) {
            instance = new ChannelManager();
        }
        return instance;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info(String.format("New connection is set, ip=%s", ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info(String.format("Connection is down, ip=%s", ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress()));
    }
}
