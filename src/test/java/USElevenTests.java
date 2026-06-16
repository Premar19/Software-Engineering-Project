
import uk.aber.dcs.gp6.tabletop.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.aber.dcs.gp6.tabletop.Suit;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class USElevenTests {
    Card card1;
    Card card2;
    Card card3;
    List<Card> cards;

    @BeforeEach
    void setUp() {
        // Create some cards
        card1 = new Card(Suit.HEART.ordinal(), 3);
        card2 = new Card(Suit.DIAMOND.ordinal(), 2);
        card3 = new Card(Suit.CLUB.ordinal(), 1);
        cards = Arrays.asList(card1, card2, card3);
    }

    @Test
    void testRevealCardsTemporarily() {
        assertFalse(card1.isFaceUp());
        assertFalse(card2.isFaceUp());
        assertFalse(card3.isFaceUp());

        for (Card card : cards) {
            card.revealCard();
        }

        assertFalse(card1.isFaceUp());
        assertFalse(card2.isFaceUp());
        assertFalse(card3.isFaceUp());

    }


    @Test
    void testRevealSingleCard() {
        Card singleCard = new Card(3, 11); // Jack of Clubs
        assertFalse(singleCard.isFaceUp());

        singleCard.revealCard();

        assertFalse(singleCard.isFaceUp());
    }
}

