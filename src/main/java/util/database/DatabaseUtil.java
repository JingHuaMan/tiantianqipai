package util.database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import info.Constants;

public class DatabaseUtil {

    private static DatabaseUtil instance;

    private final Connection connection;

    public DatabaseUtil() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        String user = Constants.DATABASE_USERNAME;
        String password = Constants.DATABASE_PASSWORD;
        String url = Constants.DATABASE_URL;
        connection = DriverManager.getConnection(url, user, password);
    }

    public static synchronized DatabaseUtil getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    public void closeConnect() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }

    public boolean signIn(String username, String password) throws NoSuchAlgorithmException, SQLException {
        String result = encryptPassword(password);
        String sql2 = "select password from users where username = '" + username + "';";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql2);
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

    public byte[] logIn(String username, String password) throws NoSuchAlgorithmException, SQLException {
        String sql = "select * from users where username='" + username + "'";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        List<Byte> result = new ArrayList<>();
        if (resultSet.next()) {
            String passwordInEncryption = encryptPassword(password);
            if (!resultSet.getString(2).equals(passwordInEncryption))
                return new byte[]{};
            for (int i = 0; i < 4; i++) {
                result.add((byte) (resultSet.getInt(0) >> (i * 8)));
            }
            for (int i = 3; i < Constants.DATABASE_COLUMNS; i++) {
                for (int j = 0; j < 4; j++) {
                    result.add((byte) (resultSet.getInt(i) >> (j * 8)));
                }
            }
        }
        resultSet.close();
        int index = 0;
        byte[] result_array = new byte[result.size()];
        for (Byte x : result)
            result_array[index++] = x;
        return result_array;
    }

    private String encryptPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] bytes = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String tmp = Integer.toHexString(b & 0xff);
            if (tmp.length() == 1)
                tmp = "0" + tmp;
            result.append(tmp);
        }
        return result.toString();
    }
}