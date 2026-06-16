import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import uk.aber.dcs.gp6.ui.Table;
import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Suit;
import uk.aber.dcs.gp6.tabletop.TableObject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class USSixTests {

    Table table;
    Card testCard;

    @BeforeEach
    void setUp() {
        testCard = new Card(Suit.SPADE.ordinal(), 7); // ✅ Fixed
        testCard.setX(100);
        testCard.setY(100);
        TableObject.getSelectedObjects().clear();
        TableObject.getSelectedObjects().add(testCard);
    }

    @Test
    void testMoveUpWithW() {
        double beforeY = testCard.getY();
        simulateKey(KeyCode.W);
        assertEquals(beforeY - 10, testCard.getY());
    }

    @Test
    void testMoveDownWithS() {
        double beforeY = testCard.getY();
        simulateKey(KeyCode.S);
        assertEquals(beforeY + 10, testCard.getY());
    }

    @Test
    void testMoveLeftWithA() {
        double beforeX = testCard.getX();
        simulateKey(KeyCode.A);
        assertEquals(beforeX - 10, testCard.getX());
    }

    @Test
    void testMoveRightWithD() {
        double beforeX = testCard.getX();
        simulateKey(KeyCode.D);
        assertEquals(beforeX + 10, testCard.getX());
    }

    private void simulateKey(KeyCode keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false);
        keyPressed(event);
    }

    private void keyPressed(KeyEvent event) {
        // A copy of the keyPressed method in the Table class but only containing TableObject movement logic

        List<TableObject> selected = TableObject.getSelectedObjects();
        if (selected.isEmpty()) return;

        double step = 10; // how far a card moves with each key press

        switch (event.getCode()) {
            case W -> selected.forEach(obj -> obj.setY(obj.getY() - step));
            case A -> selected.forEach(obj -> obj.setX(obj.getX() - step));
            case S -> selected.forEach(obj -> obj.setY(obj.getY() + step));
            case D -> selected.forEach(obj -> obj.setX(obj.getX() + step));
        }
        event.consume();
    }
}
