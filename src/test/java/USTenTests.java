import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Suit;
import uk.aber.dcs.gp6.tabletop.TableObject;

import java.util.List;

public class USTenTests {



    @BeforeEach
    void setUp() {
        // Clear selected objects list before each test
        TableObject.getSelectedObjects().clear();

        // Create some cards
        Card card1 = new Card(Suit.HEART.ordinal(), 3);
        Card card2 = new Card(Suit.SPADE.ordinal(), 10);
        Card card3 = new Card(Suit.DIAMOND.ordinal(), 1); // Ace

        // Add them to the selected list
        TableObject.getSelectedObjects().add(card1);
        TableObject.getSelectedObjects().add(card2);
        TableObject.getSelectedObjects().add(card3);
    }

    @Test
    void testAllSelectedCardsStartFaceDown() {
        List<TableObject> selected = TableObject.getSelectedObjects();

        for (TableObject obj : selected) {
            if (obj instanceof Card card) {
                assertFalse(card.isFaceUp()); // All should start face down
            }
        }
    }

    @Test
    void testFlipAllSelectedCardsOnce() {
        List<TableObject> selected = TableObject.getSelectedObjects();

        // Flip all selected cards
        for (TableObject obj : selected) {
            if (obj instanceof Card card) {
                card.flipStateOnly();
            }
        }

        // Check they are all now face up
        for (TableObject obj : selected) {
            if (obj instanceof Card card) {
                assertTrue(card.isFaceUp());
            }
        }
    }
}