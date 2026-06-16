import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExampleTests {
    // Useful methods for unit testing include:
    // - `assertEquals()`
    // - `assertTrue()`
    // - `assertFalse()`
    // - `assertThrows()` (for checking errors are thrown when required)
    @Test
    @Order(0)
    void testAddition() {
        // Example test ensuring that 1+2=3
        int one = 1;
        int two = 2;
        int result = one + two;
        assertEquals(3, result);
    }

    @Test
    @Order(10)
    void testSubtraction() {
        // Example test ensuring that 10-6=4
        // Order 10 means it runs after the previous test
        int ten = 10;
        int six = 6;
        int result = ten - six;
        assertEquals(4, result);
    }

    @Test
    @Order(20)
    void testMultiplication() {
        // Example test ensuring that 2*3=6
        // Order 20 means it runs after the previous test
        int two = 2;
        int three = 3;
        int result = two * three;
        assertEquals(6, result);
    }
}
