package uk.aber.dcs.gp6.ui;

import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import uk.aber.dcs.gp6.util.GameOptions;

/**
 * An abstract class designed to manage a UI screen.
 * This contains any methods required for every UI screen, as well as the changeScreen method to switch between them.
 *
 * @author Natalia Spence
 * @version 1
 * @see Title
 * @see Customization
 * @see CardFrontCustomization
 * @see Table
 */

public abstract class Screen extends Pane {

    /**
     * The screen constructor, this sets up any given screen
     */

    protected Screen() {
        // Create a Screen
        super();
        initializeScreen();
        initializeEvents();
    }

    /**
     * Changes the screen from the current screen to a new one
     *
     * @param current the currently displayed screen
     * @param next the screen to be displayed instead
     */

    protected void changeScreen(Screen current, Screen next) {
        // Switch from the current screen to the next screen
        Scene scene = current.getScene();
        scene.setRoot(next);
        if (next instanceof Table && current instanceof CardFrontCustomization)
            ((Table) next).setStartDecks(GameOptions.getInstance().getDeckCount());
        next.displayScreen();
    }

    /**
     * Initializes the Screen, creating Nodes and adding them
     */

    abstract protected void initializeScreen();

    /**
     * Displays Nodes on the screen, positions and resizes them every time the window size changes
     */

    public abstract void displayScreen();

    /**
     * Initializes the event handlers/filters
     */

    abstract protected void initializeEvents();

    /**
     * Handles key presses made while on the screen
     *
     * @param keyEvent the KeyEvent to handle
     */

    abstract public void keyPressed(KeyEvent keyEvent);
}
