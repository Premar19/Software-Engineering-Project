/*
 * CardFrontCustomization.java
 * User Stories: #3, #8
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.ui;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import uk.aber.dcs.gp6.util.GameOptions;
import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A class designed to manage the UI screen which allows players to customize the front of their playing cards.
 * This screen supports selecting card number, suit, and uploading custom images.
 *
 * @author Francesca Sasso
 * @version 1
 * @see Screen
 */

public class  CardFrontCustomization extends Screen {

    /**
     * Creating a singleton instance of the CardFrontCustomization class, so that only one instance
     *  of a class is ever created, ensuring screen responsible for customizing the card front
     */
    private static final CardFrontCustomization Instance = new CardFrontCustomization();

    // Text buttons for navigation
    private Text confirm;
    private Text back;
    // Card preview group and shape
    private Group cardFrontGroup;
    private Rectangle cardFrontPreview;
    // Card number navigation and display
    private Text cardNumber;
    private Text cardNumberDisplayed;
    private Text cardNumberNext;
    private Text cardNumberPrev;
    private Group cardNumberGroup;
    // Suit selection display
    private Text suit;
    private Group suitGroup;
    private Rectangle hearts;
    private Rectangle diamonds;
    private Rectangle spades;
    private Rectangle clubs;
    private final int rectangleSize = 120;
    // Variable to track which element is currently selected
    private int tabSelected = -1;


    /** The constructor enforces the singleton pattern of the class */

    private CardFrontCustomization() {
        super();
    }

    /**
     * The method initializes the screen by setting up the layout and various variables needed for the screen.
     */

    @Override
    protected void initializeScreen() {
        // Initialize UI elements for card customization

        // For the navigation buttons display
        this.confirm = new Text("Confirm");
        this.back = new Text("Back");

        // For the Card display
        this.cardFrontGroup = new Group();
        this.cardFrontPreview = new Rectangle();
        this.cardFrontPreview.setStrokeWidth(3);
        this.cardFrontGroup.getChildren().addAll(this.cardFrontPreview);

        // For the Card number & navigation  displayed
        this.cardNumberGroup = new Group();
        this.cardNumberPrev = new Text("<--");
        this.cardNumberDisplayed = new Text();
        this.cardNumberNext = new Text("-->");
        this.cardNumber = new Text();
        this.cardNumberGroup.getChildren().addAll(this.cardNumberNext, this.cardNumberDisplayed, this.cardNumberPrev);

        // For the suits change display
        this.suitGroup = new Group();
        this.suit = new Text();
        this.hearts = new Rectangle();
        this.hearts.setStrokeWidth(3);
        this.diamonds = new Rectangle();
        this.diamonds.setStrokeWidth(3);
        this.spades = new Rectangle();
        this.spades.setStrokeWidth(3);
        this.clubs = new Rectangle();
        this.clubs.setStrokeWidth(3);
        this.suitGroup.getChildren().addAll(this.hearts, this.clubs, this.diamonds, this.spades);

        // Adding all UI elements to the screen
        this.getChildren().addAll(this.confirm, this.back, this.cardFrontGroup, this.cardNumberGroup, this.suitGroup);
    }

    /**
     * The method displays the customization screen and refreshes UI elements
     * to reflect the current card state, like suit, number, border color.
     */

    @Override
    public void displayScreen() {
        // Set background image
        String bgUrl = Objects.requireNonNull(getClass().getResource("/cardfrontcustomization-background.png")).toExternalForm();
        this.setStyle(
                "-fx-background-image: url('" + bgUrl + "'); " +
                        "-fx-background-size: cover;"
        );


        // Default starting settings for card customization, also used to reset values when screen is accessed
        this.cardNumberDisplayed.setText("Ace");
        this.cardNumber.setText("1");
        this.suit.setText("C");
        allowCurrentBorderOnly(this.clubs);
        updateCardNumberPosition();

        // Load images for different card suits and backgrounds
        Image cardImage = loadImage("/user-images/cards/C1.png", "/cards/front/C1.png");
        Image c_image = loadImage("/suits/club-image.png");
        Image h_image = loadImage("/suits/heart-image.png");
        Image s_image = loadImage("/suits/spade-image.png");
        Image d_image = loadImage("/suits/diamond-image.png");

        // Setting up the images for the suit shapes
        setUpSuitImage(c_image, this.clubs, 0);
        setUpSuitImage(d_image, this.diamonds, rectangleSize * 2);
        setUpSuitImage(h_image, this.hearts, rectangleSize * 4);
        setUpSuitImage(s_image, this.spades, rectangleSize * 6);

        // Set the size and style of the card preview
        double smallSide = Math.min(this.getLayoutBounds().getHeight(), this.getLayoutBounds().getWidth());
        this.cardFrontPreview.setHeight(smallSide / 2);
        this.cardFrontPreview.setWidth(smallSide * 2 / 6);
        this.cardFrontPreview.setArcHeight(0.01 * this.getWidth());
        this.cardFrontPreview.setArcWidth(0.01 * this.getWidth());
        if (cardImage != null) this.cardFrontPreview.setFill(new ImagePattern(cardImage));

        // Setting fonts and layout for card number elements
        this.cardNumberPrev.setFont(new Font(48));
        this.cardNumberPrev.setLayoutX(0);
        this.cardNumberNext.setFont(new Font(48));
        this.cardNumberNext.setLayoutX(400);
        this.cardNumberDisplayed.setFont(new Font(48));

        // Sets up the layout positions for the various group clusters and screen buttons
        setUpLayoutClusters();

        // Sets display data for text and buttons
        this.cardNumberDisplayed.setFill(Color.BLACK);
        this.cardNumberPrev.setFill(Color.WHITE);
        this.cardNumberNext.setFill(Color.WHITE);
        this.confirm.setFill(Color.WHITE);
        this.back.setFill(Color.WHITE);

        // Updates positions based on card number logic
        updateCardNumberPosition();
    }

    // Helper functions for the displayScreen() method found below :)

    /**
     * The method attempts to load an image from a specified path. Returns null if the image is not found.
     *
     * @param path the path to the image resource (relative to the classpath)
     * @return the loaded Image; (or null)
     */

    private Image loadImage(String path) {
        // Try to find the resource at the specified path
        URL imageUrl = getClass().getResource(path);

        if (imageUrl == null) {
            // Log a warning if the resource wasn't found
            System.err.println("Warning- Could not load image from: " + path);
            return null;
        }
        // Convert the URL to an external form and create the Image
        return new Image(imageUrl.toExternalForm());
    }

    /**
     * The method follows from the first loadImage methods but if the image isn't found in the primary path,
     * it falls back to a secondary path. Returns null if neither path resolves to a valid image.
     *
     * @param primaryPath   the first path to attempt to load the image from
     * @param fallbackPath  the second path it tries
     * @return the loaded Image; (or null)
     */

    private Image loadImage(String primaryPath, String fallbackPath) {
        // Attempts to load from primary path
        URL imageUrl = getClass().getResource(primaryPath);
        // If it isn't found, tries the fallback path
        if (imageUrl == null) {
            imageUrl = getClass().getResource(fallbackPath);
        }

        // If neither work, log a warning and return null
        if (imageUrl == null) {
            System.err.println("Warning- Could not load image from given paths: " + primaryPath + " or " + fallbackPath);
            return null;
        }
        // Convert the URL to an external form and create the Image
        return new Image(imageUrl.toExternalForm());
    }

    /**
     * The helper method sets up the suit images.
     *
     * @param suitImage The image to be displayed for the suit.
     * @param suitShape The shape representing the suit on the card.
     * @param xPos The X position where the suit shape will be placed.
     */

    private void setUpSuitImage(Image suitImage, Rectangle suitShape, double xPos) {
        suitShape.setHeight(rectangleSize);
        suitShape.setWidth(rectangleSize);
        suitShape.setFill(new ImagePattern(suitImage));
        suitShape.setLayoutX(xPos);
    }

    /**
     * Binds layout properties of various UI elements to the screen's width and height.
     */

    private void setUpLayoutClusters() {
        // Bind layout for suit group to center
        this.suitGroup.layoutXProperty().bind(this.widthProperty().
                subtract(this.suitGroup.boundsInLocalProperty().get().getWidth()).divide(2));
        this.suitGroup.layoutYProperty().bind(this.heightProperty().multiply(1.0 / 12));

        // Layout binding for card front group
        this.cardFrontGroup.layoutXProperty().bind(this.widthProperty().
                subtract(this.cardFrontPreview.widthProperty()).divide(2));
        this.cardFrontGroup.layoutYProperty().bind(this.heightProperty().multiply(12.0 / 44));

        // Bind layout for card number group
        this.cardNumberGroup.layoutXProperty().bind(this.widthProperty().
                subtract(this.cardNumberGroup.getBoundsInLocal().getWidth()).divide(2));
        this.cardNumberGroup.layoutYProperty().bind(this.cardFrontGroup.layoutYProperty().multiply(3).add(60));

        this.back.setFont(new Font(48));
        this.back.setLayoutX(this.getWidth() * 2 / 20 - this.back.getLayoutBounds().getWidth());
        this.back.setLayoutY(this.getHeight() * 19 / 20);

        this.confirm.setFont(new Font(48));
        this.confirm.setLayoutX(this.getWidth() * 17 / 20);
        this.confirm.setLayoutY(this.getHeight() * 19 / 20);
    }

    /**
     * The method updates the position of the card number, ensuring they're centered between the navigation arrows.
     */

    private void updateCardNumberPosition() {
        // Calculating the center between arrows
        double prevX = this.cardNumberPrev.getLayoutX();
        double nextX = this.cardNumberNext.getLayoutX() + cardNumberPrev.getLayoutBounds().getWidth();
        double midPoint = (prevX + nextX) / 2;

        // Use a temporary Text object to measure actual width
        Text tempText = new Text(this.cardNumberDisplayed.getText());
        tempText.setFont(this.cardNumberDisplayed.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();

        // Centering text properly
        this.cardNumberDisplayed.setLayoutX(midPoint - (textWidth / 2));
    }

    // End of displayScreen() method helper functions

    /**
     * The method sets up mouse click handlers for card navigation, suit selection,
     * and other interactive elements like the back and confirm buttons.
     */

    @Override
    protected void initializeEvents() {
        // Set default card number and suit
        this.suit.setText("C");
        this.cardNumber.setText("1");
        this.cardNumberDisplayed.setText("Ace");

        // Updates the card number display when the card number changes
        this.cardNumberDisplayed.textProperty().addListener((obs, oldText, newText) -> updateCardNumberPosition());

        // Sets up the event which handles the  selection of a custom card image
        this.cardFrontPreview.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            // Opens a file explorer for image selection
            Image image = options.loadImageFromExplorer(this.getScene().getWindow());
            if (image != null) {
                // If an image is selected, update the card front preview
                this.cardFrontPreview.setFill(new ImagePattern(image));
                String newName = "cards/" + this.suit.getText() + this.cardNumber.getText() + ".png";
                GameOptions.copyFileToDirectory(options.getCardDir(image), newName);
            }
        });

        // Event handling for suit selection

        this.clubs.setOnMouseClicked(mouseEvent -> {
            this.suit.setText("C");
            allowCurrentBorderOnly(this.clubs);
            final GameOptions options = GameOptions.getInstance();
            updateCurrentFront(options);
        });
        this.diamonds.setOnMouseClicked(mouseEvent -> {
            this.suit.setText("D");
            allowCurrentBorderOnly(this.diamonds);
            final GameOptions options = GameOptions.getInstance();
            updateCurrentFront(options);
        });
        this.hearts.setOnMouseClicked(mouseEvent -> {
            this.suit.setText("H");
            allowCurrentBorderOnly(this.hearts);
            final GameOptions options = GameOptions.getInstance();
            updateCurrentFront(options);
        });
        this.spades.setOnMouseClicked(mouseEvent -> {
            this.suit.setText("S");
            allowCurrentBorderOnly(this.spades);
            final GameOptions options = GameOptions.getInstance();
            updateCurrentFront(options);
        });

        // Event handling for navigating through card numbers
        this.cardNumberNext.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            updateCardNumberDisplay(1, options);
            updateCurrentFront(options);
        });
        this.cardNumberPrev.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            updateCardNumberDisplay(-1, options);
            updateCurrentFront(options);
        });

        // Event handling for the back button
        this.back.setOnMouseClicked(mouseEvent -> this.changeScreen(this, Customization.getInstance()));
        // Event handling for the confirm button
        this.confirm.setOnMouseClicked(mouseEvent -> this.changeScreen(this, Table.getInstance()));
    }

    // Helper functions to the initializeEvents() method found below :)

    /**
     * The method updates the current card showing, depending on the selected card number and suit.
     *
     * @param options The current game options.
     */

    public void updateCurrentFront(GameOptions options) {
        // Constructing the card's overall ID and Image path
        String cardName = suit.getText() + cardNumber.getText();
        // If it's a joker, the ID won't include a suit
        if(options.getCardNumber() == 14){
            cardName = this.cardNumber.getText();
        }

        String cardIDPath = "/user-images/cards/" + cardName + ".png";
        File userImageFile = new File("target/classes/" + cardIDPath); // Adjust path if needed

        if (!userImageFile.exists()) {
            cardIDPath = "/cards/front/" + cardName + ".png";
        }

        // Loads image correctly
        URL imageUrl = getClass().getResource(cardIDPath);
        if (imageUrl == null) {
            System.err.println("Error: Image not found at " + cardIDPath);
        } else {
            this.cardFrontPreview.setFill(new ImagePattern(new Image(imageUrl.toExternalForm())));
        }
    }

    /**
     * The method updates the display of the card number based on the current game options.
     *
     * @param difference The difference to modify the current card number by.
     * @param options    The current game options.
     */

    public void updateCardNumberDisplay(int difference, GameOptions options){
        options.changeCardNumber(difference);
        // Mapping a card's numerical integer to its significant ID and value
        switch (options.getCardNumber()) {
            case 1 -> { this.cardNumber.setText("1"); this.cardNumberDisplayed.setText("Ace");}
            case 10 -> {this.cardNumber.setText("T"); this.cardNumberDisplayed.setText("10");}
            case 11 -> {this.cardNumber.setText("J"); this.cardNumberDisplayed.setText("Jack");}
            case 12 -> {this.cardNumber.setText("Q"); this.cardNumberDisplayed.setText("Queen");}
            case 13 -> {this.cardNumber.setText("K"); this.cardNumberDisplayed.setText("King");}
            case 14 -> {this.cardNumber.setText("JJ"); this.cardNumberDisplayed.setText("Joker");}
            default -> {this.cardNumber.setText(String.valueOf(options.getCardNumber()));
                this.cardNumberDisplayed.setText(String.valueOf(options.getCardNumber()));}
        }
    }

    /**
     * The method highlights the current selected suit by changing its border color.
     *
     * @param obj The suit button to be highlighted.
     */

    public void allowCurrentBorderOnly (Rectangle obj) {
        for (Node suit : suitGroup.getChildren()){
            if (suit instanceof Rectangle rectangle){
                if (rectangle == obj) {
                    rectangle.setStroke(Color.PURPLE);
                } else { rectangle.setStroke(null); }
            }
        }
    }

    // End of initializeEvents() method helper functions

    /**
     * Handles keyboard navigation for the CardFrontCustomization screen.
     *
     * @param keyEvent the keyEvent to handle
     */

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        // Order for tab navigation of ui elements
        List<Node> tabOrder = Arrays.asList(this.clubs, this.diamonds, this.hearts, this.spades,
            this.cardFrontPreview, this.cardNumberPrev, this.cardNumberNext, this.back, this.confirm);
        List<Node> suits = Arrays.asList(this.clubs, this.diamonds, this.hearts, this.spades, this.cardFrontPreview);

        if (keyEvent.getCode() == KeyCode.TAB) {
            // Tab navigation
            Node prev = null;
            Node next;

            if (keyEvent.isShiftDown()) {
                // Reverse if holding shift
                if (this.tabSelected != -1) {
                    prev = tabOrder.get(this.tabSelected);
                }
                if (this.tabSelected == 0) this.tabSelected = tabOrder.size() - 1;
                else this.tabSelected--;
                next = tabOrder.get(this.tabSelected);
            } else {
                // Forwards if not holding shift
                if (this.tabSelected != -1) {
                    prev = tabOrder.get(this.tabSelected);
                }
                if (this.tabSelected == tabOrder.size() - 1) this.tabSelected = 0;
                else this.tabSelected++;
                next = tabOrder.get(this.tabSelected);
            }

            // Unhighlight the element being deselected
            if (prev != null) {
                if (suits.contains(prev)) {
                    if (!((prev.equals(this.clubs) && Objects.equals(this.suit.getText(), "C")) ||
                            prev.equals(this.diamonds) && Objects.equals(this.suit.getText(), "D") ||
                            prev.equals(this.hearts) && Objects.equals(this.suit.getText(), "H") ||
                            prev.equals(this.spades) && Objects.equals(this.suit.getText(), "S")))
                        ((Rectangle) prev).setStroke(null);
                } else if (prev.equals(this.cardFrontPreview)) ((Rectangle) prev).setStroke(null);
                else ((Shape) prev).setFill(Color.WHITE);
            }

            // Highlight the element being navigated onto
            if (suits.contains(next)) {
                if (!((this.tabSelected == 0 && Objects.equals(this.suit.getText(), "C")) ||
                        this.tabSelected == 1 && Objects.equals(this.suit.getText(), "D") ||
                        this.tabSelected == 2 && Objects.equals(this.suit.getText(), "H") ||
                        this.tabSelected == 3 && Objects.equals(this.suit.getText(), "S")))
                    ((Rectangle) next).setStroke(Color.LIGHTSKYBLUE);
            } else ((Shape) next).setFill(Color.LIGHTSKYBLUE);

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            // Cancel keyboard navigation on escape key press

            if (this.tabSelected == -1) return;
            Node prev = tabOrder.get(this.tabSelected);
            if (suits.contains(prev)) {
                if (!((prev.equals(this.clubs) && Objects.equals(this.suit.getText(), "C")) ||
                        prev.equals(this.diamonds) && Objects.equals(this.suit.getText(), "D") ||
                        prev.equals(this.hearts) && Objects.equals(this.suit.getText(), "H") ||
                        prev.equals(this.spades) && Objects.equals(this.suit.getText(), "S")))
                    ((Rectangle) prev).setStroke(Color.LIGHTSKYBLUE);
            } else ((Shape) prev).setFill(Color.WHITE);
            this.tabSelected = -1;

        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            // Click on a UI element on enter key press

            if (this.tabSelected >= 0 && this.tabSelected < tabOrder.size())
                Event.fireEvent(tabOrder.get(tabSelected), new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                        MouseButton.PRIMARY, 1, false, false, false, false, true, false,
                        false, false, false, false, null));
        }
    }

    // Getters and Setters below :)

    /**
     * Setter method sets the card number text.
     *
     * @param text The Text object containing the card number to set.
     */

    public void setCardNumber(Text text) {
        this.cardNumber = text;
    }

    /**
     * Setter method sets the suit text.
     *
     * @param text The Text object containing the suit to set.
     */

    public void setSuit (Text text) {
        this.suit = text;
    }

    /**
     * Getter method returns the card number text.
     *
     * @return The Text object representing the card number.
     */

    public Text getCardNumber() {
        return this.cardNumber;
    }

    /**
     * Getter method returns the suit text.
     *
     * @return The Text object representing the suit.
     */

    public Text getSuit() {
        return this.suit;
    }

    /**
     * Getter method returns the displayed card number text.
     *
     * @return The Text object that displays the card number.
     */

    public Text getCardNumberDisplayed () {
        return cardNumberDisplayed;
    }

    /**
     * Getter method returns the singleton instance of CardFrontCustomization.
     *
     * @return The instance of CardFrontCustomization.
     */

    public static CardFrontCustomization getInstance() {
        return Instance;
    }
}
