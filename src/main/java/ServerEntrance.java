import handler.pipelineHandlers.EventDispatcher;
import handler.pipelineHandlers.codec.MessageDecoder;
import handler.pipelineHandlers.codec.MessageEncoder;
import info.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import util.database.DatabaseUtil;

import java.sql.SQLException;

@Slf4j
public class ServerEntrance {

    public static void main(String[] args) {
        ServerEntrance server = new ServerEntrance();
        server.start();
    }

    private void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) {
                    ChannelPipeline pipeline = nioSocketChannel.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Constants.MESSAGE_MAX_LENGTH, Constants.MESSAGE_LENGTH_FIELD_OFFSET, Constants.MESSAGE_LENGTH_FIELD_LENGTH, 0, Constants.MESSAGE_LENGTH_FIELD_LENGTH));
                    pipeline.addLast(new MessageEncoder());
                    pipeline.addLast(new MessageDecoder());
                    pipeline.addLast(EventDispatcher.INSTANCE);
                }
            });

            ChannelFuture future = serverBootstrap.bind(Constants.SERVER_PORT);
            future.addListener((ChannelFutureListener) listener -> {
                if (listener.isSuccess())
                    log.info("bind port {} success!", Constants.SERVER_PORT);
                else
                    log.error("Error: bind port {} fail!", Constants.SERVER_PORT);
            });
        } catch (Exception e) {
            log.error("Error: Server error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            try {
                DatabaseUtil.getInstance().closeConnect();
            } catch (SQLException | ClassNotFoundException e) {
                log.error("Failed when closing connection with database");
            }
        }
    }
}
