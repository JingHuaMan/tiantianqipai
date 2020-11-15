package handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import protocal.Message;

@ChannelHandler.Sharable
public class EventDispatcher extends SimpleChannelInboundHandler<Message> {

    public static final EventDispatcher INSTANCE = new EventDispatcher();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) {
        byte head1 = msg.getHead1(), head2 = msg.getHead2();
        byte[] data = msg.getData();
        switch (head1) {
            case 0: // system operations
                switch (head2) {
                    case 0: // register in

                        break;
                    case 1: // log in

                }
                break;
            case 1:
                switch (head2) { // landlord operations
                    case 0: // enter game

                        break;
                    case 1: // call for landlord

                        break;
                    case 2: // play a hand

                        break;
                    case 3: // end a game

                        break;
                    case 4: // dialogue

                }
                break;
        }
    }
}
