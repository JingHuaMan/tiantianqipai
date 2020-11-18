package config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {

    public final static Charset CHARSET = StandardCharsets.US_ASCII;

    // The following are the parameters for the server settings

    public final static int SERVER_PORT = 11111;

    // The following are the parameters for the message

    public final static int MESSAGE_MAX_LENGTH = Integer.MAX_VALUE;

    public final static int MESSAGE_LENGTH_FIELD_OFFSET = 0;

    public final static int MESSAGE_LENGTH_FIELD_LENGTH = 4;

    public final static int MESSAGE_HEAD1_LENGTH = 1;

    public final static int MESSAGE_HEAD2_LENGTH = 1;

    // The following are the parameters for database

    public final static String DATABASE_USERNAME = "postgres";

    public final static String DATABASE_PASSWORD = "123456";

    public final static String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";

    public final static int DATABASE_COLUMNS = 4;
}
