package pojo.protocal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {

    private byte head1;

    private byte head2;

    private byte[] data = null;

    public Message copyMessage() {
        if (data != null) {
            Message newMessage = new Message();
            newMessage.setHead1(this.head1);
            newMessage.setHead2(this.head2);
            byte[] newData = new byte[this.data.length];
            System.arraycopy(this.data, 0, newData, 0, this.data.length);
            newMessage.setData(newData);
            return newMessage;
        }
        return null;
    }
}
