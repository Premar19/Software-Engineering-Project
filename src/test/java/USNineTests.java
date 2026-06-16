import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Suit;

import static org.junit.jupiter.api.Assertions.*;

public class USNineTests {

    Card testCard;

    @BeforeEach
    void setUp() {
        // Create a card
        testCard = new Card(Suit.SPADE.ordinal(), 1);
    }

    @Test
    void testCardStartsFaceDown() {
        // Card should spawn face down
        assertFalse(testCard.isFaceUp());
    }

    @Test
    void testFlipTurnsCardFaceUp() {
        testCard.flipStateOnly();
        assertTrue(testCard.isFaceUp());
    }

    @Test
    void testFlipTwiceReturnsToFaceDown() {
        testCard.flipStateOnly(); // face up
        testCard.flipStateOnly(); // face down again
        assertFalse(testCard.isFaceUp());
    }
}