package protocal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {

    private byte head1;

    private byte head2;

    private byte[] data;
}
