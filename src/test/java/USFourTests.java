import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.aber.dcs.gp6.util.GameOptions;
import javafx.scene.image.Image;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class USFourTests {

    @Test
    @Order(0)
    void testFindBackground() {
        Image background = GameOptions.getInstance().getTableBackgroundImage(1, 1);
        assertNotNull(background);
        assertEquals(background.getProgress(), 1);
        assertFalse(background.isError());
    }
}
