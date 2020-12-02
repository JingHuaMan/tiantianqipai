package handler.pipelineHandlers;

import config.Constants;
import handler.eventHandler.landlord.GameManager;
import handler.eventHandler.landlord.RoomManager;
import handler.eventHandler.system.UserManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pojo.data.landlord.Game;
import pojo.data.landlord.Room;
import pojo.data.landlord.card.HandCard;
import pojo.data.landlord.card.PlayCard;
import pojo.data.system.User;
import pojo.protocal.Message;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import util.database.DatabaseUtil;

@Slf4j
@ChannelHandler.Sharable
public class EventDispatcher extends SimpleChannelInboundHandler<Message> {

    private static EventDispatcher instance;

    public synchronized static EventDispatcher getInstance() {
        if (instance == null) {
            instance = new EventDispatcher();
        }
        return instance;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        byte head1 = msg.getHead1(), head2 = msg.getHead2();
        byte[] data = msg.getData();
        switch (head1) {
            case 0: // system operations
                switch (head2) {
                    case 0: // sign in
//                        log.info("User sign in");
                        ctx.writeAndFlush(signIn(data));
                        break;
                    case 1: // log in
//                        log.info("User log in");
                        ctx.writeAndFlush(logIn(data, ctx.channel()));
                }
                break;
            case 1:
                switch (head2) { // landlord operations
                    case 0: // enter room
                        ctx.writeAndFlush(enterRoom(data));
                        break;
                    case 1: // wait for game
                        waitGame(data);
                        break;
                    case 2: // call for landlord
                        callForLandlord(data);
                        break;
                    case 3: // play a hand
                        playCards(data);
                        break;
                    case 4: // continue game or leave
                        userEndGame(data);
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
        } catch (NoSuchAlgorithmException e) {
            log.error("Error", e);
        }
        return new Message((byte) 0, (byte) 0, new byte[]{(byte) (result ? 1 : 0)});
    }

    private Message logIn(byte[] data, Channel channel) {
        Message response = new Message();
        try {
            //divide the message and the password by '\0'
            String[] user_info = bytesToString(data).split("\0");
            response.setHead1((byte) 0);
            response.setHead2((byte) 1);
            List<String> responseInfo = DatabaseUtil.getInstance().logIn(user_info[0], user_info[1]);
            if (responseInfo.size() == 0) {
                response.setData(new byte[0]);
            } else {
                User logInUser = new User(Integer.parseInt(responseInfo.get(0)), responseInfo.get(1));
                if (UserManager.getInstance().userLogin(logInUser, channel)) {
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
        } catch (NoSuchAlgorithmException e) {
            log.error("Error:", e);
        }
        return response;
    }

    private Message enterRoom(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        Message response = new Message();
        response.setHead1((byte) 1);
        response.setHead2((byte) 0);
        Room room = null;
        try {
            room = RoomManager.getInstance().getAvailableRoom(user, bytesToInt(data, 4));
        } catch (SQLException e) {
            log.error("SQLException when query bean num");
        }
        if (room == null) {
            response.setData(new byte[]{0});
            return response;
        }
        List<Byte> dataList = new ArrayList<>();
        byte[] usernameInBytes = stringToBytes(user.getUsername());
        byte[] userIdInBytes = intToBytes(user.getId());
        Message roommateMsg = new Message();
        roommateMsg.setHead1((byte) 1);
        roommateMsg.setHead2((byte) 1);
        byte[] roommateData = new byte[4 + usernameInBytes.length];
        System.arraycopy(userIdInBytes, 0, roommateData, 0, 4);
        System.arraycopy(usernameInBytes, 0, roommateData, 4, usernameInBytes.length);
        roommateMsg.setData(roommateData);
        boolean first = true;
        for (User userInRoom : room.getUsers()) {
            if (userInRoom != user) {
                if (first) {
                    first = false;
                } else {
                    dataList.add((byte) '\0');
                }
                for (byte i : intToBytes(userInRoom.getId()))
                    dataList.add(i);
                dataList.add((byte) '\0');
                for (byte i : stringToBytes(userInRoom.getUsername()))
                    dataList.add(i);
                Channel roommateChannel = UserManager.getInstance().getChannelByUser(userInRoom);
                roommateChannel.writeAndFlush(roommateMsg.copyMessage());
            }
        }
        int index = 0;
        byte[] responseData = new byte[dataList.size()];
        for (byte i : dataList)
            responseData[index++] = i;
        response.setData(responseData);
        return response;
    }

    private void waitGame(byte[] data) {
        int userId = bytesToInt(data, 0);
        User user = UserManager.getInstance().getUserById(userId);
        boolean result = false;
        try {
            result = RoomManager.getInstance().userWaitForGame(user);
        } catch (SQLException e) {
            log.error("SQLException when updating bean num in waiting game");
        }
        if (result) { // if a game is generated
            Game game = GameManager.getInstance().getGameByUser(user);
            dealCardsToUsers(game, (byte) 3);
        } else {
            for (User roommate : RoomManager.getInstance().getRoomByUser(user).getUsers()) {
                if (roommate != user) {
                    Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                    Message roommateMsg = new Message();
                    roommateMsg.setHead1((byte) 1);
                    roommateMsg.setHead2((byte) 2);
                    byte[] roommateData = new byte[data.length];
                    System.arraycopy(data, 0, roommateData, 0, data.length);
                    roommateMsg.setData(roommateData);
                    channel.writeAndFlush(roommateMsg);
                }
            }
        }
    }

    private void callForLandlord(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        Game game = GameManager.getInstance().getGameByUser(user);
        Room room = RoomManager.getInstance().getRoomByUser(user);
        int result = game.callForLandlord(user, data[4]);
        Message responseMsg = new Message();
        if (result == 0) { // nothing happened
            responseMsg.setHead1((byte) 1);
            responseMsg.setHead2((byte) 4);
            responseMsg.setData(data);
            for (User roommate : room.getUsers()) {
                if (roommate != user) {
                    Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                    channel.writeAndFlush(responseMsg);
                    responseMsg = responseMsg.copyMessage();
                }
            }
        } else if (result == 1) { // landlord is set, ready for play cards
            responseMsg.setHead1((byte) 1);
            responseMsg.setHead2((byte) 5);
            byte[] responseData = new byte[7];
            System.arraycopy(intToBytes(user.getId()), 0, responseData, 0, 4);
            byte[] restCardsInBytes = game.getRestCards().toByteArray();
            System.arraycopy(restCardsInBytes, 0, responseData, 4, 3);
            responseMsg.setData(responseData);
            for (User roommate : room.getUsers()) {
                Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                channel.writeAndFlush(responseMsg);
                responseMsg = responseMsg.copyMessage();
            }
        } else { // no one call for landlord, a new round
            dealCardsToUsers(game, (byte) 6);
        }
    }

    private void dealCardsToUsers(Game game, byte head2) { // head1 should always be 1
        List<User> userOrder = game.getUserOrder();
        for (Map.Entry<User, HandCard> entry : game.getUserHandCardMap().entrySet()) {
            User roommate = entry.getKey();
            Channel channel = UserManager.getInstance().getChannelByUser(roommate);
            Message responseMsg = new Message();
            responseMsg.setHead1((byte) 1);
            responseMsg.setHead2(head2);
            byte[] responseData = new byte[29];
            for (int i = 0; i < 3; i++) {
                byte[] tempUserIdInBytes = intToBytes(userOrder.get(i).getId());
                System.arraycopy(tempUserIdInBytes, 0, responseData, 4 * i, 4);
            }
            System.arraycopy(entry.getValue().toByteArray(), 0, responseData, 12, 17);
            responseMsg.setData(responseData);
            channel.writeAndFlush(responseMsg);
        }
    }

    private void playCards(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        Room room = RoomManager.getInstance().getRoomByUser(user);
        Game game = GameManager.getInstance().getGameByUser(user);
        PlayCard tempCards = new PlayCard(Arrays.copyOfRange(data, 4, data.length));
        int result = GameManager.getInstance().playCardByUser(user, tempCards);
        if (result == 0) { // illegal cards
            Channel channel = UserManager.getInstance().getChannelByUser(user);
            Message responseMsg = new Message();
            responseMsg.setHead1((byte)1);
            responseMsg.setHead2((byte)7);
            responseMsg.setData(new byte[0]);
            channel.writeAndFlush(responseMsg);
        } else if (result == 1) { // legal cards
            for (User roommate: room.getUsers()) {
                if (roommate != user) {
                    Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                    HandCard roommateHandCard = game.getUserHandCardMap().get(roommate);
                    Message responseMsg = new Message();
                    responseMsg.setHead1((byte)1);
                    responseMsg.setHead2((byte)8);
                    PlayCard availablePlayCard = roommateHandCard.getAvailablePlayCard(tempCards);
                    byte[] responseData = new byte[availablePlayCard.getSize() + data.length + 1];
                    System.arraycopy(availablePlayCard.toByteArray(), 0, responseData, 0, availablePlayCard.getSize());
                    System.arraycopy(data, 0, responseData, availablePlayCard.getSize() + 1, data.length);
                    responseMsg.setData(responseData);
                    channel.writeAndFlush(responseMsg);
                }
            }
        } else { // game finished
            Message responseMsg = new Message();
            responseMsg.setHead1((byte)1);
            responseMsg.setHead2((byte)9);
            byte[] responseData = new byte[data.length + 25];
            System.arraycopy(data, 0, responseData, 0, data.length);
            Map<User, Integer> gameResult = GameManager.getInstance().getGameResult(room);
            int index = data.length + 1;
            for (Map.Entry<User, Integer> userResult: gameResult.entrySet()) {
                System.arraycopy(intToBytes(userResult.getKey().getId()), 0, responseData, index, 4);
                System.arraycopy(intToBytes(userResult.getValue()), 0, responseData, index + 4, 4);
                index += 8;
            }
            responseMsg.setData(responseData);
            for (User roommate: room.getUsers()) {
                Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                channel.writeAndFlush(responseMsg);
                responseMsg = responseMsg.copyMessage();
            }
        }
    }

    private void userEndGame(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        Room room = RoomManager.getInstance().getRoomByUser(user);
        boolean stayOrLeave = data[4] != 0;
        int result = RoomManager.getInstance().userEndGame(user, stayOrLeave);
        if (result == 0) {
            if (!stayOrLeave) {
                for (User roommate: room.getUsers()) {
                    Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                    Message responseMsg = new Message();
                    responseMsg.setHead1((byte)1);
                    responseMsg.setHead2((byte)10);
                    responseMsg.setData(intToBytes(user.getId()));
                    channel.writeAndFlush(responseMsg);
                }
            }
        } else if (result == 1) {
            int responseDataInInt = stayOrLeave ? 0 : user.getId();
            for (User roommate: room.getUsers()) {
                Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                Message responseMsg = new Message();
                responseMsg.setHead1((byte)1);
                responseMsg.setHead2((byte)11);
                responseMsg.setData(intToBytes(responseDataInInt));
                channel.writeAndFlush(responseMsg);
            }
        } else if (result == 2) {
            for (User roommate: room.getUsers()) {
                Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                Message responseMsg = new Message();
                responseMsg.setHead1((byte)1);
                responseMsg.setHead2((byte)12);
                responseMsg.setData(new byte[0]);
                channel.writeAndFlush(responseMsg);
            }
        }
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
            bytes[i] = (byte) (input & 0xFF);
            input = input >> 8;
        }
        return bytes;
    }

    private int bytesToInt(byte[] input, int start) {
        int result = 0;
        for (int i = 0; i < 4; i++)
            result += ((int) input[start + i]) << (8 * i);
        return result;
    }
}
