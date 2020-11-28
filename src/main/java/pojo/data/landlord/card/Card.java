package pojo.data.landlord.card;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(exclude = {"val", "color"})
public class Card {
    public enum Color {
        DIAMOND(0), CLUB(1), HEART(2), SPADE(3);
        private final int value;

        Color(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public int val;
    public Color color;

    public Card(int val, Color color) {
        this.val = val;
        this.color = color;
    }

    public Card(byte msg) {
        this((byte) (msg >> 2), Card.Color.values()[0b11 & msg]);
    }

    public byte toByte() {
        byte a = (byte) (this.val);
        byte b = (byte) (this.color.getValue());
        return (byte) ((byte) (a << 2) | b);
    }
}