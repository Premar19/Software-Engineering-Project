import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.aber.dcs.gp6.util.GameOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class USTwoTests {

    @Test
    @Order(0)
    void testAddJoker() {
        GameOptions.getInstance().changeJokerCount(1);
        assertEquals(1, GameOptions.getInstance().getJokerCount());
    }

    @Test
    @Order(10)
    void testRemoveJoker() {
        GameOptions.getInstance().changeJokerCount(-1);
        assertEquals(0, GameOptions.getInstance().getJokerCount());
    }

    @Test
    @Order(20)
    void testJokerCountTooLow() {
        GameOptions.getInstance().changeJokerCount(-1);
        assertEquals(0, GameOptions.getInstance().getJokerCount());
    }

    @Test
    @Order(30)
    void testJokerCountTooHigh() {
        GameOptions.getInstance().changeJokerCount(1);
        GameOptions.getInstance().changeJokerCount(1);
        GameOptions.getInstance().changeJokerCount(1);
        GameOptions.getInstance().changeJokerCount(1);
        GameOptions.getInstance().changeJokerCount(1);
        GameOptions.getInstance().changeJokerCount(1);
        GameOptions.getInstance().changeJokerCount(1);
        assertEquals(6, GameOptions.getInstance().getJokerCount());
    }
}
