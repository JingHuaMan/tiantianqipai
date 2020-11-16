package landlord.data;

import lombok.Data;

@Data
public class Room {

    private String id;

    private final Object[] USERS = new Object[3];
}
