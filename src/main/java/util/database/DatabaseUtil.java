package util.database;

import config.Constants;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DatabaseUtil {

    private static DatabaseUtil instance;

    private Connection connection;

    public DatabaseUtil() {
    }

    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    private void getConnection() {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public void closeConnect() {
        ConnectionManager.closeConnectionPool();
    }

    public synchronized boolean signIn(String username, String password) {
        getConnection();
        String result = encryptPassword(password);
        String sql2 = "select password from users where username = '" + username + "';";
        ResultSet resultSet;
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql2);

            if (resultSet.next()) {
                return false;
            }
            String sql = "insert into users(username,password) values(?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, result);
            preparedStatement.executeUpdate();
            resultSet.close();
        } catch (SQLException e) {
            log.error("Sign In Error");
        }
        return true;
    }

    public synchronized boolean useHalfCost(int id) {
        getConnection();
        String sql1 = "select halfcost from users where id=" + id;
        ResultSet resultSet;
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql1);
            int number = resultSet.getInt("halfcost");
            if (number == 0) {
                return false;
            }
            String sql2 = "update users set halfcost=" + (number - 1) + " where id=" + id;
            statement.executeUpdate(sql2);
        } catch (SQLException e) {
            log.error("Use HalfCost Error");
        }
        return true;

    }

    public synchronized boolean useDoubleEarning(int id) {
        String sql1 = "select doubleearning from users where id=" + id;
        ResultSet resultSet;
        try {
            getConnection();
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql1);
            int number = resultSet.getInt("doubleearning");
            if (number == 0) {
                return false;
            }
            String sql2 = "update users set doubleearning=" + (number - 1) + " where id=" + id;
            statement.executeUpdate(sql2);
        } catch (SQLException e) {
            log.error("Use Double Earning Error");
        }
        return true;
    }

    public synchronized void updateHalfCost(int id, int num) {
        getConnection();
        String sql = "update users set halfcost= (select halfcost  from users where id=" + id + ")+(" + num + ") where id=" + id + ";";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("Use Half Cost Error");
        }

    }

    public synchronized void updateDoubleEarning(int id, int num) {
        getConnection();
        String sql = "update users set doubleearning= (select doubleearning  from users where id=" + id + ")+(" + num + ") where id=" + id + ";";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("Update Double Earning Error");
        }
    }

    public synchronized List<String> logIn(String username, String password) {
        getConnection();
        String sql = "select * from users where username='" + username + "'";
        ResultSet resultSet;
        List<String> result = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                String passwordInEncryption = encryptPassword(password);
                if (!resultSet.getString(3).equals(passwordInEncryption))
                    return result;
                result.add("" + resultSet.getInt(1));
                result.add(resultSet.getString(2));
                for (int i = 4; i <= Constants.DATABASE_COLUMNS; i++)
                    result.add("" + resultSet.getInt(i));
            }
        } catch (SQLException e) {
            log.error("Fail when login");
        }
        return result;
    }

    private String encryptPassword(String password) {
        getConnection();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("Encryption error");
        }
        byte[] bytes = new byte[0];
        if (messageDigest != null) {
            bytes = messageDigest.digest(password.getBytes(Constants.CHARSET));
        }
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String tmp = Integer.toHexString(b & 0xff);
            if (tmp.length() == 1)
                tmp = "0" + tmp;
            result.append(tmp);
        }
        return result.toString();
    }

    public synchronized int getBeanNum(int id) {
        getConnection();
        String sql = "select beannum from users where id='" + id + "'";
        ResultSet resultSet;
        int result = 0;
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                result = resultSet.getInt("beanNum");
            }
        } catch (SQLException e) {
            log.error("Get Bean Number error");
        }

        return result;
    }

    public synchronized void updateBeanNum(int id, int num) {
        getConnection();
        String sql = "update users set beannum= (select beannum  from users where id=" + id + ")+(" + num + ") where id=" + id + ";";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("Update Bean Number Error");
        }
    }
}