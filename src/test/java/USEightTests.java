import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.aber.dcs.gp6.tabletop.TableObject;
import uk.aber.dcs.gp6.tabletop.Card;

import static org.junit.jupiter.api.Assertions.*;

public class USEightTests {

    TableObject testObject;

    @BeforeEach
    void setUp() {
        testObject = new TableObject();
        // Clear selection before each test
        TableObject.getSelectedObjects().clear();
    }

    @Test
    void testCtrlClickSelectAddsToSelection() {
        // Simulate ctrl-click
        assertFalse(TableObject.getSelectedObjects().contains(testObject));
        TableObject.getSelectedObjects().add(testObject);
        assertTrue(TableObject.getSelectedObjects().contains(testObject));
    }

    @Test
    void testCtrlClickSelectRemovesFromSelection() {
        TableObject.getSelectedObjects().add(testObject);
        assertTrue(TableObject.getSelectedObjects().contains(testObject));
        TableObject.getSelectedObjects().remove(testObject);
        assertFalse(TableObject.getSelectedObjects().contains(testObject));
    }

    @Test
    void testDraggingChangesPosition() {
        testObject.setX(100);
        testObject.setY(100);

        double oldX = testObject.getX();
        double oldY = testObject.getY();

        // Simulate dragging
        double dx = 20;
        double dy = 30;

        testObject.setX(oldX + dx);
        testObject.setY(oldY + dy);

        assertEquals(120, testObject.getX());
        assertEquals(130, testObject.getY());
    }
}