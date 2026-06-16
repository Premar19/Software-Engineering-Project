import org.junit.jupiter.api.*;
import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Deck;
import uk.aber.dcs.gp6.tabletop.Suit;
import uk.aber.dcs.gp6.tabletop.TableObject;
import uk.aber.dcs.gp6.util.GameOptions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class USOneTests {



    @Test
    @Order(0)
    void testCardEquality() {
        Card card1 = new Card(1, 2);
        Card card1Copy = new Card(Suit.DIAMOND, 2, card1.isFaceUp(), card1.getCustomId());
        Card card1Copy1 = new Card(Suit.DIAMOND, 2, card1.isFaceUp(), card1.getCustomId());;
        Card card2 = new Card(3, 8);

        // Reflexivity
        assertTrue(card1.tableObjectsAreEqual(card1));
        assertTrue(card1Copy.tableObjectsAreEqual(card1Copy));
        assertTrue(card2.tableObjectsAreEqual(card2));

        // A deck should not equal null
        assertFalse(card1.tableObjectsAreEqual(null));

        // Two identical decks should be equal
        assertTrue(card1.tableObjectsAreEqual(card1Copy));

        // Two different decks should not be equal
        assertFalse(card1.tableObjectsAreEqual(card2));

        // Symmetricity
        assertTrue(card1Copy.tableObjectsAreEqual(card1));

        // Transitivity
        assertTrue(card1.tableObjectsAreEqual(card1Copy));
        assertTrue(card1Copy.tableObjectsAreEqual(card1Copy1));
        assertTrue(card1.tableObjectsAreEqual(card1Copy1));

        assertFalse(card1Copy.tableObjectsAreEqual(card2));
        assertFalse(card2.tableObjectsAreEqual(card1));

        // Consistency
        assertTrue(card1.tableObjectsAreEqual(card1Copy));
        assertTrue(card1.tableObjectsAreEqual(card1Copy));
        assertTrue(card1.tableObjectsAreEqual(card1Copy));
        assertFalse(card1.tableObjectsAreEqual(card2));
        assertFalse(card1.tableObjectsAreEqual(card2));
        assertFalse(card1.tableObjectsAreEqual(card2));
    }

    @Test
    @Order(10)
    void testDeckEquality() {
        Deck deck1 = new Deck();
        List<TableObject> deck1Cards = new ArrayList<>(deck1.getCards());
        Deck deck1Copy = new Deck(deck1Cards, deck1.getCustomId());
        Deck deck1Copy2 = new Deck(deck1Cards, deck1.getCustomId());
        Deck deck2 = new Deck();

        // Reflexivity
        assertTrue(deck1.tableObjectsAreEqual(deck1));
        assertTrue(deck1Copy.tableObjectsAreEqual(deck1Copy));
        assertTrue(deck2.tableObjectsAreEqual(deck2));

        // A deck should not equal null
        assertFalse(deck1.tableObjectsAreEqual(null));

        // Two identical decks should be equal
        assertTrue(deck1.tableObjectsAreEqual(deck1Copy));

        // Two different decks should not be equal
        assertFalse(deck1.tableObjectsAreEqual(deck2));

        // Symmetricity
        assertTrue(deck1Copy.tableObjectsAreEqual(deck1));

        // Transitivity
        assertTrue(deck1.tableObjectsAreEqual(deck1Copy));
        assertTrue(deck1Copy.tableObjectsAreEqual(deck1Copy2));
        assertTrue(deck1.tableObjectsAreEqual(deck1Copy2));

        assertFalse(deck1Copy.tableObjectsAreEqual(deck2));
        assertFalse(deck2.tableObjectsAreEqual(deck1));

        // Consistency
        assertTrue(deck1.tableObjectsAreEqual(deck1Copy));
        assertTrue(deck1.tableObjectsAreEqual(deck1Copy));
        assertTrue(deck1.tableObjectsAreEqual(deck1Copy));
        assertFalse(deck1.tableObjectsAreEqual(deck2));
        assertFalse(deck1.tableObjectsAreEqual(deck2));
        assertFalse(deck1.tableObjectsAreEqual(deck2));
    }

    @Test
    @Order(20)
    void testAddDeck() {
        GameOptions.getInstance().changeDeckCount(1);
        assertEquals(2, GameOptions.getInstance().getDeckCount());
    }

    @Test
    @Order(30)
    void testRemoveDeck() {
        GameOptions.getInstance().changeDeckCount(-1);
        assertEquals(1, GameOptions.getInstance().getDeckCount());
    }

    @Test
    @Order(40)
    void testTooFewDecks() {
        GameOptions.getInstance().changeDeckCount(-1);
        assertEquals(1, GameOptions.getInstance().getDeckCount());
    }

    @Test
    @Order(50)
    void testTooManyDecks() {
        // The user can only ever change the deck count by 1 or -1 at a time,
        // so this is called several times rather than once.
        GameOptions.getInstance().changeDeckCount(1);
        GameOptions.getInstance().changeDeckCount(1);
        GameOptions.getInstance().changeDeckCount(1);
        GameOptions.getInstance().changeDeckCount(1);

        assertEquals(4, GameOptions.getInstance().getDeckCount());
        GameOptions.getInstance().changeDeckCount(-3);
    }
}
