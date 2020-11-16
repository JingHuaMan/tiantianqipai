package handlers;

import info.Constants;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import protocal.Message;
import util.database.DatabaseUtil;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

@ChannelHandler.Sharable
public class EventDispatcher extends SimpleChannelInboundHandler<Message> {

    public static final EventDispatcher INSTANCE = new EventDispatcher();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        byte head1 = msg.getHead1(), head2 = msg.getHead2();
        byte[] data = msg.getData();
        switch (head1) {
            case 0: // system operations
                switch (head2) {
                    case 0: // sign in
                        try {
                            String[] user_info = new String(data, Constants.CHARSET.toString()).split("\0");
                            boolean result = DatabaseUtil.getInstance().signIn(user_info[0], user_info[1]);
                            Message response = Message.builder().head1((byte)0).head2((byte)0).data(new byte[]{(byte)(result ? 1 : 0)}).build();
                            ctx.writeAndFlush(response);
                        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
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
