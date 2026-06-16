import org.junit.jupiter.api.*;
import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Deck;
import uk.aber.dcs.gp6.util.ActionType;
import uk.aber.dcs.gp6.util.Recording;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class USFifteenTests {

    @AfterAll
    public static void stopRecordingThread() {
        Recording.toggleRecordingGame();
    }

    @Test
    @Order(0)
    public void testToggleRecording() {
        assertTrue(Recording.toggleRecordingGame());
        assertFalse(Recording.toggleRecordingGame());
    }

    @Test
    @Order(10)
    public void testRecordWhenRecordingDisabled() {
        Card card = new Card(1, 1);
        card.setX(100);
        card.setY(100);
        assertFalse(Recording.createGameAction(ActionType.CARD_DRAG, card));
    }

    @Test
    @Order(20)
    public void testRecordCardDrag() {
        Card card = new Card(1, 1);
        card.setX(100);
        card.setY(100);
        assertTrue(Recording.toggleRecordingGame());
        assertTrue(Recording.createGameAction(ActionType.CARD_DRAG, card));
    }

    @Test
    @Order(30)
    public void testEndDragAction() {
        Card card = new Card(1, 1);
        card.setX(100);
        card.setY(100);
        assertTrue(Recording.createGameAction(ActionType.CARD_DRAG, card));
        assertTrue(Recording.createGameAction(ActionType.END_ACTION, card));
    }

    @Test
    @Order(40)
    public void testRecordCardDraw() {
        Deck deck = new Deck();
        assertTrue(Recording.createGameAction(ActionType.DRAW_CARD, deck));
        Card card = deck.getCards().removeFirst();
        assertTrue(Recording.createGameAction(ActionType.DRAW_CARD, card));
        assertTrue(Recording.createGameAction(ActionType.DRAW_CARD, deck));
    }
}
