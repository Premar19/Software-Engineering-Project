import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Deck;

import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class USSevenTests {

    Deck testDeck;
    Card testCard;

    @BeforeEach
    void setUp() {
        // Create a deck
        testDeck = new Deck();
        testCard = testDeck.getCards().getFirst();
    }

    @Test
    void testRandomPosition() {
        Random random = new Random();
        int randomIndex = random.nextInt(0, testDeck.getCards().size() + 1);
        Card card = testDeck.getCards().removeFirst();
        testDeck.getCards().add(randomIndex, card);
        Card movedCard = testDeck.getCards().get(randomIndex);
        assertEquals(testCard.getSuit(), movedCard.getSuit());
        assertEquals(testCard.getSuit(), movedCard.getSuit());
    }

    @Test
    void testBottomPosition() {
        Card card = testDeck.getCards().removeFirst();
        testDeck.getCards().add(card);
        Card movedCard = testDeck.getCards().getLast();
        assertEquals(testCard.getSuit(), movedCard.getSuit());
        assertEquals(testCard.getSuit(), movedCard.getSuit());
    }
}
