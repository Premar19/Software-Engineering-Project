import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import uk.aber.dcs.gp6.tabletop.Deck;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class USFiveTests {

    Deck testDeck;
    boolean isDeckShuffled;

    @BeforeEach
    void setUp() {
        // Create a deck
        testDeck = new Deck();
    }

    @Test
    void testShuffleDeck() {
        isDeckShuffled = testDeck.shuffleDeck();
        assertTrue(isDeckShuffled);
    }
}
