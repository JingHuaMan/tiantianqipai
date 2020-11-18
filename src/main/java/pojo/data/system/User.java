package pojo.data.system;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id"})
public class User {

    private int id;

    private String username;
}
