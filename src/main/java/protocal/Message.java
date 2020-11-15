package protocal;

import lombok.Data;

@Data
public class Message {

    private byte head1;

    private byte head2;

    private byte[] data;
}
