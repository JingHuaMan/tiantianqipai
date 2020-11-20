package handler.pipelineHandlers;

import config.Constants;
import handler.eventHandler.landlord.RoomManager;
import handler.eventHandler.system.UserManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pojo.data.landlord.Room;
import pojo.data.system.User;
import pojo.protocal.Message;
import util.DatabaseUtil;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                        ctx.writeAndFlush(logIn(data, ctx));
                }
                break;
            case 1:
                switch (head2) { // landlord operations
                    case 0: // enter room
                        ctx.writeAndFlush(enterRoom(data));
                        break;
                    case 1: // call for a landlord position

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

    private Message logIn(byte[] data, ChannelHandlerContext ctx) {
        Message response = new Message();
        try {
            //divide the message and the password by '\0'
            String[] user_info = bytesToString(data).split("\0");
            response.setHead1((byte)0);
            response.setHead2((byte)1);
            List<String> responseInfo = DatabaseUtil.getInstance().logIn(user_info[0], user_info[1]);
            if (responseInfo.size() == 0) {
                response.setData(new byte[0]);
            } else {
                User logInUser = new User(Integer.parseInt(responseInfo.get(0)), responseInfo.get(1));
                if (UserManager.getInstance().userLogin(logInUser, ctx)) {
                    byte[] responseData = new byte[4 * (responseInfo.size() - 1)];
                    int index = 0;
                    for (byte i : intToBytes(Integer.parseInt(responseInfo.get(0))))
                        responseData[index++] = i;
                    for (int i = 0; i < Constants.DATABASE_COLUMNS - 3; i++)
                        for (byte j : intToBytes(Integer.parseInt(responseInfo.get(2 + i))))
                            responseData[index++] = j;
                    response.setData(responseData);
                } else
                    response.setData(new byte[0]);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error: the charset" + Constants.CHARSET.toString() + " is invalid!");
        } catch (SQLException e) {
            log.error("Error: sql exception", e);
        } catch (NoSuchAlgorithmException | ClassNotFoundException e) {
            log.error("Error:", e);
        }
        return response;
    }

    private Message enterRoom(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        Room room = RoomManager.getInstance().getAvailableRoom(user);
        Message response = new Message();
        response.setHead1((byte)1);
        response.setHead2((byte)0);
        List<Byte> dataList = new ArrayList<>();
        byte[] idInBytes = intToBytes(user.getId()), usernameInBytes = stringToBytes(user.getUsername());
        for (User userInRoom : room.getUsers()) {
            if (userInRoom != user) {
                for (byte i : intToBytes(userInRoom.getId()))
                    dataList.add(i);
                dataList.add((byte)'\0');
                for (byte i : stringToBytes(userInRoom.getUsername()))
                    dataList.add(i);
                dataList.add((byte)'\0');
                ChannelHandlerContext roommateCtx = UserManager.getInstance().getCtxByUser(userInRoom);
                Message roommateMsg = new Message();
                roommateMsg.setHead1((byte)1);
                roommateMsg.setHead2((byte)1);
                int index = 0;
                byte[] roommateMsgData = new byte[1 + idInBytes.length + usernameInBytes.length];
                for (byte i : idInBytes)
                    roommateMsgData[index++] = i;
                roommateMsgData[index++] = (byte)'\0';
                for (byte i : usernameInBytes)
                    roommateMsgData[index++] = i;
                roommateMsg.setData(roommateMsgData);
                roommateCtx.writeAndFlush(roommateMsg);
            }
        }
        int index = 0;
        byte[] responseData = new byte[dataList.size()];
        for (byte i : dataList)
            responseData[index++] = i;
        response.setData(responseData);
        return response;
    }

    private String bytesToString(byte[] input) throws UnsupportedEncodingException {
        return new String(input, Constants.CHARSET.toString());
    }

    private byte[] stringToBytes(String input) {
        return input.getBytes(Constants.CHARSET);
    }

    private byte[] intToBytes(int input) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte)(input % 256);
            input /= 256;
        }
        return bytes;
    }

    private int bytesToInt(byte[] input, int start) {
        int result = 0;
        for (int i = 0; i < 4; i++)
            result += ((int)input[start + i]) << (8 * i);
        return result;
    }
}
