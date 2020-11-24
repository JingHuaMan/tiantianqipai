package util.database;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import config.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConnectionManager {

    private static ConnectionManager instance;

    private static ComboPooledDataSource dataSource;

    private ConnectionManager() throws PropertyVetoException {
        dataSource = new ComboPooledDataSource();

        dataSource.setUser(Constants.DATABASE_USERNAME);		// database username
        dataSource.setPassword(Constants.DATABASE_PASSWORD);    // database password
        dataSource.setJdbcUrl(Constants.DATABASE_URL);          // database url
        dataSource.setDriverClass("org.postgresql.Driver");
        dataSource.setInitialPoolSize(5);                       // initial connection amount
        dataSource.setMinPoolSize(1);                           // minimum connection amount
        dataSource.setMaxPoolSize(10);                          // maximum connection amount
        dataSource.setMaxStatements(50);                        // maximum waiting time
        dataSource.setMaxIdleTime(60);                          // maximum idle time
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            try {
                instance = new ConnectionManager();
            } catch (PropertyVetoException e) {
                log.error("PropertyVetoException:", e);
            }
        }
        return instance;
    }

    public synchronized final Connection getConnection() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public synchronized static void closeConnectionPool() {
        dataSource.close();
    }
}