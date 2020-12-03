package util.database;

import config.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    private static DatabaseUtil instance;

    private final Connection connection;

    public DatabaseUtil() {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    public void closeConnect() {
        ConnectionManager.closeConnectionPool();
    }

    public boolean signIn(String username, String password) throws NoSuchAlgorithmException, SQLException {
        String result = encryptPassword(password);
        String sql2 = "select password from users where username = '" + username + "';";
        ResultSet resultSet;
        synchronized (connection) {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql2);
        }
        if (resultSet.next()) {
            return false;
        }
        String sql = "insert into users(username,password) values(?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, result);
        preparedStatement.executeUpdate();
        resultSet.close();
        return true;
    }
    public boolean useHalfCost(int id) throws SQLException {
        String sql1 = "select halfcost from users where id=" + id ;
        ResultSet resultSet;
        synchronized (connection) {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql1);
            int number=resultSet.getInt("halfcost");
            if (number==0) {
                return false;
            }
            String sql2 = "update users set halfcost="+(number-1)+" where id=" + id ;
            statement.executeUpdate(sql2);
            return true;
        }
    }
    public boolean useDoubleEarning(int id) throws SQLException {
        String sql1 = "select doubleearning from users where id=" + id ;
        ResultSet resultSet;
        synchronized (connection) {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql1);
            int number=resultSet.getInt("doubleearning");
            if (number==0) {
                return false;
            }
            String sql2 = "update users set doubleearning="+(number-1)+" where id=" + id ;
            statement.executeUpdate(sql2);
            return true;
        }
    }
    public void updateHalfCost(int id, int num) throws SQLException {
        String sql = "update users set halfcost= (select halfcost  from users where id=" + id + ")+(" + num + ") where id=" + id + ";";
        synchronized (connection) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        }
    }
    public void updateDoubleEarning(int id, int num) throws SQLException {//only add, no decline
        String sql = "update users set doubleearning= (select doubleearning  from users where id=" + id + ")+(" + num + ") where id=" + id + ";";
        synchronized (connection) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        }
    }
    public List<String> logIn(String username, String password) throws NoSuchAlgorithmException, SQLException {
        String sql = "select * from users where username='" + username + "'";
        ResultSet resultSet;
        synchronized (connection) {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
        }
        List<String> result = new ArrayList<>();
        if (resultSet.next()) {
            String passwordInEncryption = encryptPassword(password);
            if (!resultSet.getString(3).equals(passwordInEncryption))
                return result;
            result.add("" + resultSet.getInt(1));
            result.add(resultSet.getString(2));
            for (int i = 4; i <= Constants.DATABASE_COLUMNS; i++)
                result.add("" + resultSet.getInt(i));
        }
        return result;
    }

    private String encryptPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] bytes = messageDigest.digest(password.getBytes(Constants.CHARSET));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String tmp = Integer.toHexString(b & 0xff);
            if (tmp.length() == 1)
                tmp = "0" + tmp;
            result.append(tmp);
        }
        return result.toString();
    }

    public int getBeanNum(int id) throws SQLException {
        String sql = "select beannum from users where id='" + id + "'";
        ResultSet resultSet;
        synchronized (connection) {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
        }
        int result = 0;
        if (resultSet.next()) {
            result = resultSet.getInt("beanNum");
        }
        return result;
    }

    public void updateBeanNum(int id, int num) throws SQLException {
        String sql = "update users set beannum= (select beannum  from users where id=" + id + ")+(" + num + ") where id=" + id + ";";
        synchronized (connection) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        }
    }
}