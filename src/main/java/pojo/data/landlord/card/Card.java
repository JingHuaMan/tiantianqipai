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
}