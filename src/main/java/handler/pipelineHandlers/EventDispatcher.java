package handler.pipelineHandlers;

import config.Constants;
import handler.eventHandler.landlord.GameManager;
import handler.eventHandler.landlord.RoomManager;
import handler.eventHandler.system.BeanAndPropsManager;
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
//        log.info(msg.toString());
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
                    case 2: // log out
                        logOut(data);
                    case 3: // use game props
                        ctx.writeAndFlush(useProps(data));
                    case 4: // buy game props
                        ctx.writeAndFlush(buyProps(data));
                    case 5: // get daily beans
                        ctx.writeAndFlush(getDailyBeans(data));
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
                    System.arraycopy(intToBytes(Integer.parseInt(responseInfo.get(0))), 0, responseData, 0, 4);
                    for (int i = 0; i < Constants.DATABASE_COLUMNS - 3; i++)
                        System.arraycopy(intToBytes(Integer.parseInt(responseInfo.get(2 + i))), 0, responseData, 4 * (i + 1), 4);
                    response.setData(responseData);
                } else
                    response.setData(new byte[0]);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error: the charset" + Constants.CHARSET.toString() + " is invalid!");
        }
        return response;
    }

    private void logOut(byte[] data) {
        UserManager.getInstance().userLogout(bytesToInt(data, 0));
    }

    private Message useProps(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        boolean result = BeanAndPropsManager.getInstance().useProps(user, (data[4] == 0) ?
                BeanAndPropsManager.PropType.DOUBLE_EARN : BeanAndPropsManager.PropType.HALF_COST);
        Message msg = new Message();
        msg.setHead1((byte)0);
        msg.setHead2((byte)2);
        msg.setData(new byte[] {(byte)(result ? 1 : 0), data[4]});
        return msg;
    }

    private Message buyProps(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        boolean result = BeanAndPropsManager.getInstance().buyProps(user, (data[4] == 0) ?
                BeanAndPropsManager.PropType.DOUBLE_EARN : BeanAndPropsManager.PropType.HALF_COST,
                bytesToInt(data, 5));
        Message msg = new Message();
        msg.setHead1((byte)0);
        msg.setHead2((byte)2);
        byte[] msgData = new byte[6];
        msgData[0] = (byte)(result ? 1 : 0);
        msgData[1] = data[4];
        System.arraycopy(data, 5, msgData, 2, 4);
        msg.setData(msgData);
        return msg;
    }

    private Message getDailyBeans(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
        boolean result = BeanAndPropsManager.getInstance().getBeanDaily(user);
        Message msg = new Message();
        msg.setHead1((byte)0);
        msg.setHead2((byte)2);
        msg.setData(new byte[] {(byte)(result ? 1 : 0)});
        return msg;
    }

    private Message enterRoom(byte[] data) {
        User user = UserManager.getInstance().getUserById(bytesToInt(data, 0));
//        log.info(user.toString());
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
            System.out.println(44);
            response.setData(new byte[]{});
            return response;
        }
        List<Byte> dataList = new ArrayList<>();
        byte[] usernameInBytes = stringToBytes(user.getUsername());
        byte[] userIdInBytes = intToBytes(user.getId());
        Message roommateMsg = new Message();
        roommateMsg.setHead1((byte) 1);
        roommateMsg.setHead2((byte) 1);
        byte[] roommateData = new byte[8 + usernameInBytes.length];
        System.arraycopy(userIdInBytes, 0, roommateData, 0, 4);
        System.arraycopy(intToBytes(usernameInBytes.length), 0, roommateData, 4, 4);
        System.arraycopy(usernameInBytes, 0, roommateData, 8, usernameInBytes.length);
        roommateMsg.setData(roommateData);
        boolean first = true;
        byte userNum = 0;
        for (User userInRoom : room.getUsers()) {
            if (userInRoom != user) {
                userNum++;
                if (first) {
                    first = false;
                } else {
                    dataList.add((byte) '\0');
                }
                for (byte i : intToBytes(userInRoom.getId()))
                    dataList.add(i);
                for (byte i : intToBytes(userInRoom.getUsername().length()))
                    dataList.add(i);
                for (byte i : stringToBytes(userInRoom.getUsername()))
                    dataList.add(i);
                Channel roommateChannel = UserManager.getInstance().getChannelByUser(userInRoom);
                System.out.println(roommateChannel);
                System.out.println(roommateMsg);
                roommateChannel.writeAndFlush(roommateMsg.copyMessage());
            }
        }
        int index = 1;
        byte[] responseData = new byte[1 + dataList.size()];
        responseData[0] = userNum;
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
        Channel thisChannel = UserManager.getInstance().getChannelByUser(user);
        Message thisResponseMsg = new Message();
        thisResponseMsg.setHead1((byte) 1);
        thisResponseMsg.setHead2((byte) 7);
        if (result == 0) { // illegal cards
            thisResponseMsg.setData(new byte[] {0});
            thisChannel.writeAndFlush(thisResponseMsg);
        } else if (result == 1) { // legal cards
            thisResponseMsg.setData(new byte[] {1});
            thisChannel.writeAndFlush(thisResponseMsg);
            for (User roommate : room.getUsers()) {
                if (roommate != user) {
                    Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                    HandCard roommateHandCard = game.getUserHandCardMap().get(roommate);
                    Message responseMsg = new Message();
                    responseMsg.setHead1((byte) 1);
                    responseMsg.setHead2((byte) 8);
                    if (tempCards.getSize() == 0) {
                        responseMsg.setData(data);
                    } else {
                        PlayCard availablePlayCard = roommateHandCard.getAvailablePlayCard(tempCards);
                        byte[] responseData = new byte[availablePlayCard.getSize() + data.length + 1];
                        System.arraycopy(data, 0, responseData, 0, data.length);
                        System.arraycopy(availablePlayCard.toByteArray(), 0, responseData, data.length + 1, availablePlayCard.getSize());
                        responseMsg.setData(responseData);
                    }
                    channel.writeAndFlush(responseMsg);
                }
            }
        } else { // game finished
            Message responseMsg = new Message();
            responseMsg.setHead1((byte) 1);
            responseMsg.setHead2((byte) 9);
            byte[] responseData = new byte[data.length + 25];
            System.arraycopy(data, 0, responseData, 0, data.length);
            Map<User, Integer> gameResult = GameManager.getInstance().getGameResult(room);
            int index = data.length + 1;
            for (Map.Entry<User, Integer> userResult : gameResult.entrySet()) {
                System.arraycopy(intToBytes(userResult.getKey().getId()), 0, responseData, index, 4);
                System.arraycopy(intToBytes(userResult.getValue()), 0, responseData, index + 4, 4);
                index += 8;
            }
            responseMsg.setData(responseData);
            for (User roommate : room.getUsers()) {
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
                for (User roommate : room.getUsers()) {
                    Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                    Message responseMsg = new Message();
                    responseMsg.setHead1((byte) 1);
                    responseMsg.setHead2((byte) 10);
                    responseMsg.setData(intToBytes(user.getId()));
                    channel.writeAndFlush(responseMsg);
                }
            }
        } else if (result == 1) {
            int responseDataInInt = stayOrLeave ? 0 : user.getId();
            for (User roommate : room.getUsers()) {
                Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                Message responseMsg = new Message();
                responseMsg.setHead1((byte) 1);
                responseMsg.setHead2((byte) 11);
                responseMsg.setData(intToBytes(responseDataInInt));
                channel.writeAndFlush(responseMsg);
            }
        } else if (result == 2) {
            for (User roommate : room.getUsers()) {
                Channel channel = UserManager.getInstance().getChannelByUser(roommate);
                Message responseMsg = new Message();
                responseMsg.setHead1((byte) 1);
                responseMsg.setHead2((byte) 12);
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
            bytes[3 - i] = (byte) (input & 0xFF);
            input = input >> 8;
        }
        return bytes;
    }

    private int bytesToInt(byte[] input, int start) {
        int result = 0;
        for (int i = 0; i < 4; i++)
            result += ((int) input[start + 3 - i]) << (8 * i);
        return result;
    }
}
