package pojo.data.system;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"id"})
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class User {
    @NonNull
    private int id;

    private String username;
}
