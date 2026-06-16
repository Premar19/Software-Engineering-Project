import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import uk.aber.dcs.gp6.tabletop.Deck;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class USFourteenTests {

    Deck testDeck;

    @BeforeEach
    void setUp() {
        // Create a deck
        testDeck = new Deck();
    }

    @Test
    void testRotationAngleIncrease() {
        testDeck.setRotate(15);
        assertEquals(15,testDeck.getRotate());
    }

    @Test
    void testRotationAngleDecrease() {
        testDeck.setRotate(-15);
        assertEquals(-15,testDeck.getRotate());
    }
}
