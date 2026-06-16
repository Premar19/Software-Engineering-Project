import org.junit.jupiter.api.*;
import uk.aber.dcs.gp6.util.GameOptions;
import javafx.scene.text.Text;

import static org.junit.jupiter.api.Assertions.*;
import uk.aber.dcs.gp6.tabletop.TableObject;
import uk.aber.dcs.gp6.ui.CardFrontCustomization;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class USThreeTests {
    CardFrontCustomization testObject;

    @BeforeEach
    void setUp() {
        testObject = CardFrontCustomization.getInstance();
        // Clear selection before each test
        TableObject.getSelectedObjects().clear();
    }


    @Test
    @Order(10)
    void testTextWrapping() {
        final GameOptions options = GameOptions.getInstance();

        Text newText = new Text();
        //Starts at 1, so by adding 13 we get to 14
        newText.setText("13");

        testObject.setCardNumber(newText);
        testObject.updateCardNumberDisplay(0, options);;
        assertEquals(1, options.getCardNumber());

        newText.setText("1");

        testObject.setCardNumber(newText);
        testObject.updateCardNumberDisplay(-1, options);;
        assertEquals(14, options.getCardNumber());
    }

    @Test
    @Order(20)
    void testFindFile() {
        final GameOptions options = GameOptions.getInstance();

        //tring fileName = "H5";
        Text suit = new Text();
        suit.setText("H");
        Text number = new Text();
        number.setText("5");

        testObject.setSuit(suit);
        testObject.setCardNumber(number);

        // If the card file exists, we should have a non-null image
        String expectedPath = "" + testObject.getSuit().getText() + testObject.getCardNumber().getText();

        assertNotNull(getClass().getResource("/cards/front/" + expectedPath + ".png"));
    }

    @Test
    @Order(20)
    void testCarndNumberToDisplay() {
        final GameOptions options = GameOptions.getInstance();

        //Starts at 1, so by adding 9 we get to 10
        options.changeCardNumber(9);
        testObject.updateCardNumberDisplay(9, options);

        assertEquals("T", testObject.getCardNumber().getText());
        assertEquals("10", testObject.getCardNumberDisplayed().getText());

        testObject.updateCardNumberDisplay(3, options);
        assertEquals("K", testObject.getCardNumber().getText());
        assertEquals("King", testObject.getCardNumberDisplayed().getText());
    }


//    @Test
//    void testCopyFileToDirectory()  {
//    }
}

