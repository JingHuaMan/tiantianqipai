import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerEntrance {

    private final static Logger logger = LoggerFactory.getLogger(ServerEntrance.class);

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

            }
        });

        serverBootstrap.bind(Constants.SERVER_PORT).addListener((ChannelFutureListener) future -> {
           if (future.isSuccess())
               logger.info("bind port {} success!", Constants.SERVER_PORT);
           else
               logger.error("bind port {} fail!", Constants.SERVER_PORT);
        });
    }


}
