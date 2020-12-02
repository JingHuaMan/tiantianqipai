import handler.pipelineHandlers.ChannelManager;
import handler.pipelineHandlers.EventDispatcher;
import handler.pipelineHandlers.Spliter;
import handler.pipelineHandlers.codec.MessageDecoder;
import handler.pipelineHandlers.codec.MessageEncoder;
import config.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import util.database.DatabaseUtil;

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
                    pipeline.addLast(ChannelManager.getInstance());
                    pipeline.addLast(new Spliter());
                    pipeline.addLast(new MessageEncoder());
                    pipeline.addLast(new MessageDecoder());
                    pipeline.addLast(EventDispatcher.getInstance());
                }
            });

            ChannelFuture future = serverBootstrap.bind(Constants.SERVER_PORT).sync();
            future.addListener((ChannelFutureListener) listener -> {
                if (listener.isSuccess())
                    log.info("bind port {} success!", Constants.SERVER_PORT);
                else
                    log.error("bind port {} fail!", Constants.SERVER_PORT);
            });
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Server error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            DatabaseUtil.getInstance().closeConnect();
        }
    }
}
