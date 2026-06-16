import org.junit.jupiter.api.*;
import uk.aber.dcs.gp6.util.Recording;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class USThirteenTests {
    @Test
    @Order(0)
    public void testLoadValidLayoutFile() {
        // Sample Test File
        File file = new File("src/test/resources/test-layout-file.csgrdl");
        assertTrue(Recording.loadRecording(file));
    }

    @Test
    @Order(10)
    public void testLoadEmptyLayoutFile() {
        // Empty Test File
        File file = new File("src/test/resources/empty-test-layout-file.csgrdl");
        assertFalse(Recording.loadRecording(file));
    }

    @Test
    @Order(20)
    public void testLoadInvalidLayoutFile() {
        // Invalid Test File
        File file = new File("src/test/resources/invalid-test-layout-file.csgrdl");
        assertFalse(Recording.loadRecording(file));
    }
}