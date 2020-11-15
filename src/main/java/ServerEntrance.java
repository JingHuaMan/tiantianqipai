import handlers.EventDispatcher;
import handlers.PushDecoder;
import info.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

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

        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                pipeline.addLast(new LengthFieldBasedFrameDecoder(Constants.MESSAGE_MAX_LENGTH, Constants.MESSAGE_LENGTH_FIELD_OFFSET, Constants.MESSAGE_LENGTH_FIELD_LENGTH, 0, Constants.MESSAGE_LENGTH_FIELD_LENGTH));
                pipeline.addLast(new PushDecoder());
                pipeline.addLast(EventDispatcher.INSTANCE);
            }
        });

        ChannelFuture future = serverBootstrap.bind(Constants.SERVER_PORT);
        future.addListener((ChannelFutureListener) listener -> {
            if (listener.isSuccess())
                log.info("bind port {} success!", Constants.SERVER_PORT);
            else
                log.error("bind port {} fail!", Constants.SERVER_PORT);
        });


    }


}
