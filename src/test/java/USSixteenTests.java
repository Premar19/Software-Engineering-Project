import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.aber.dcs.gp6.util.Recording;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class USSixteenTests {
    @Test
    @Order(0)
    public void testLoadValidReplayFile() {
        // Sample Test File
        File file = new File("src/test/resources/test-replay-file.csgr");
        assertTrue(Recording.loadRecording(file));
    }

    @Test
    @Order(10)
    public void testLoadEmptyReplayFile() {
        // Empty Test File
        File file = new File("src/test/resources/empty-test-replay-file.csgr");
        assertFalse(Recording.loadRecording(file));
    }

    @Test
    @Order(20)
    public void testLoadInvalidReplayFile() {
        // Invalid Test File
        File file = new File("src/test/resources/invalid-test-replay-file.csgr");
        assertFalse(Recording.loadRecording(file));
    }
}