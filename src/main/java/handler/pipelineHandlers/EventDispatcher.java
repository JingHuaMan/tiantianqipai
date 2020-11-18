package handler.pipelineHandlers;

import config.Constants;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pojo.protocal.Message;
import util.DatabaseUtil;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
                        ctx.writeAndFlush(signIn(data));
                        break;
                    case 1: // log in
                        ctx.writeAndFlush(logIn(data));
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

    private Message signIn(byte[] data) {
        boolean result = false;
        try {
            //divide the message and the password by '\0'
            String[] user_info = bytesToString(data).split("\0");
            result = DatabaseUtil.getInstance().signIn(user_info[0], user_info[1]);
        } catch (UnsupportedEncodingException e) {
            log.error("Error: the charset " + Constants.CHARSET.toString() + " is invalid!");
        } catch (SQLException e) {
            log.error("Error: sql exception", e);
        } catch (NoSuchAlgorithmException | ClassNotFoundException e) {
            log.error("Error", e);
        }
        return new Message((byte)0, (byte)0, new byte[]{(byte)(result ? 1 : 0)});
    }

    private Message logIn(byte[] data) {
        Message response = new Message();
        try {
            //divide the message and the password by '\0'
            String[] user_info = bytesToString(data).split("\0");
            response.setHead1((byte)0);
            response.setHead2((byte)1);
            response.setData(DatabaseUtil.getInstance().logIn(user_info[0], user_info[1]));
        } catch (UnsupportedEncodingException e) {
            log.error("Error: the charset" + Constants.CHARSET.toString() + " is invalid!");
        } catch (SQLException e) {
            log.error("Error: sql exception", e);
        } catch (NoSuchAlgorithmException | ClassNotFoundException e) {
            log.error("Error:", e);
        }
        return response;
    }

    private String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, Constants.CHARSET.toString());
    }
}
