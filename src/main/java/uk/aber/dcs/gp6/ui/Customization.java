/*
 * Customization.java
 * User Stories: #1, #2, #4
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.ui;

import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A class designed to manage the UI screen which allows players to customize everything except the front of the cards.
 * This screen supports specifying the number of decks to display, the number of jokers per deck, the
 * table background, and the card back.
 *
 * @author Natalia Spence, Prem Sharma
 * @version 1
 * @see Screen
 */

public class Customization extends Screen {
    private static final Customization Instance = new Customization();

    private Group deckGroup;
    private Text deckText;
    private Text deckCount;
    private Text deckPlus;
    private Text deckMinus;
    private Group cardBackGroup;
    private Rectangle cardBackPreview;
    private Text cardBackText;
    private Text confirm;
    private Text back;
    // Joker number, buttons and structure
    private Group jokerGroup;
    private Text jokerText;
    private Text jokerCount;
    private Text jokerPlus;
    private Text jokerMinus;

    private Rectangle tableBackground;
    private Group tableBackCustomization;
    private Text tableBackText;
    private int tabSelected = -1;

    /**
     * The Customization screen constructor, private to ensure this class remains a singleton
     */

    private Customization() {
        super();
    }

    /**
     * The method initialises the screen by setting up the layout and various variables needed for the screen.
     */

    @Override
    protected void initializeScreen() {
        this.deckGroup = new Group();
        this.deckText = new Text("Decks:");
        this.deckMinus = new Text("-");
        this.deckCount = new Text(String.valueOf(GameOptions.getInstance().getDeckCount()));
        this.deckPlus = new Text("+");
        this.deckGroup.getChildren().addAll(this.deckText, this.deckCount, this.deckPlus, this.deckMinus);

        // For the Joker count display and updating data
        this.jokerGroup = new Group();
        this.jokerText = new Text("Jokers per Deck:");
        this.jokerMinus = new Text("-");
        this.jokerCount = new Text(String.valueOf(GameOptions.getInstance().getJokerCount()));
        this.jokerPlus = new Text("+");
        this.jokerGroup.getChildren().addAll(this.jokerText, this.jokerCount, this.jokerPlus, this.jokerMinus);


        this.cardBackGroup = new Group();
        this.cardBackPreview = new Rectangle();
        this.cardBackPreview.setStrokeWidth(5);
        this.cardBackText = new Text("Card Back");
        this.cardBackGroup.getChildren().addAll(this.cardBackPreview, this.cardBackText);

        this.confirm = new Text("Confirm");
        this.back = new Text("Back");

        this.tableBackText =new Text("Table Background");
        this.tableBackground = new Rectangle(400, 400);
        this.tableBackground.setStrokeWidth(5);
        this.tableBackCustomization = new Group();
        this.tableBackCustomization.getChildren().addAll(this.tableBackground, this.tableBackText);

        this.getChildren().addAll(this.tableBackCustomization, this.deckGroup, this.cardBackGroup, this.jokerGroup, this.confirm, this.back);
    }

    /**
     * The method displays the customization screen and refreshes UI elements
     * to reflect the current state of the chosen settings
     */

    @Override
    public void displayScreen() {
        // Set background image
        String bgUrl = Objects.requireNonNull(getClass().getResource("/customization-background.png")).toExternalForm();
        this.setStyle(
                "-fx-background-image: url('" + bgUrl + "'); " +
                        "-fx-background-size: cover;"
        );

        Image cardImage;
        Image tableBackImage;

        if (getClass().getResource("/user-images/cards/BACK.png") != null)
            cardImage = new Image(Objects.requireNonNull(getClass().getResource("/user-images/cards/BACK.png"))
                    .toExternalForm());
        else
            cardImage = new Image(Objects.requireNonNull(getClass().getResource("/cards/BACK.png"))
                    .toExternalForm());

        if (getClass().getResource("/user-images/table-background.png") != null)
            tableBackImage = new Image(Objects.requireNonNull(getClass().getResource("/user-images/table-background.png"))
                    .toExternalForm());
        else
            tableBackImage = new Image(Objects.requireNonNull(getClass().getResource("/table-background.jpg"))
                    .toExternalForm());


        this.deckText.setFont(new Font(48));

        this.deckMinus.setFont(new Font(48));
        this.deckMinus.setLayoutX(this.deckText.getLayoutBounds().getWidth() + 20);

        this.deckCount.setFont(new Font(48));
        this.deckCount.setLayoutX(this.deckText.getLayoutBounds().getWidth() + 20 +
                this.deckCount.getLayoutBounds().getWidth());

        this.deckPlus.setFont(new Font(48));
        this.deckPlus.setLayoutX(this.deckText.getLayoutBounds().getWidth() + 20 +
                this.deckCount.getLayoutBounds().getWidth() + this.deckCount.getLayoutBounds().getWidth());

        this.deckGroup.setLayoutX(this.getWidth()/2 - this.deckGroup.getLayoutBounds().getWidth()/2);
        this.deckGroup.setLayoutY(this.getHeight()/9 - this.deckGroup.getLayoutBounds().getHeight()/9);

        double smallSide = Math.min(this.getLayoutBounds().getHeight(), this.getLayoutBounds().getWidth());
        this.cardBackPreview.setHeight(smallSide / 2);
        this.cardBackPreview.setWidth(smallSide * 2 / 6);
        this.cardBackPreview.setArcHeight(0.01*this.getWidth());
        this.cardBackPreview.setArcWidth(0.01*this.getWidth());
        this.cardBackPreview.setFill(new ImagePattern(cardImage));

        this.cardBackGroup.setLayoutX(this.getWidth()*4/6);
        this.cardBackGroup.setLayoutY(this.getHeight()*13/48);

        // Setting fonts and screen position layouts for joker elements
        this.jokerText.setFont(new Font(48));

        this.jokerMinus.setFont(new Font(48));
        this.jokerMinus.setLayoutX(this.jokerText.getLayoutBounds().getWidth() + 20);

        this.jokerCount.setFont(new Font(48));
        this.jokerCount.setLayoutX(this.jokerText.getLayoutBounds().getWidth() + 20 +
                this.jokerCount.getLayoutBounds().getWidth());

        this.jokerPlus.setFont(new Font(48));
        this.jokerPlus.setLayoutX(this.jokerText.getLayoutBounds().getWidth() + 20 +
                this.jokerCount.getLayoutBounds().getWidth() + this.jokerCount.getLayoutBounds().getWidth());

        this.jokerGroup.setLayoutX(this.getWidth()/2 - this.jokerGroup.getLayoutBounds().getWidth()/2);
        this.jokerGroup.setLayoutY(this.getHeight()/6);

        this.tableBackCustomization.setLayoutX(this.getWidth()/10);
        this.tableBackCustomization.setLayoutY(this.getHeight()*10/48);

        this.tableBackground.setHeight(this.getHeight()/1.7);
        this.tableBackground.setWidth(this.getWidth()/2);
        this.tableBackground.setArcHeight(0.01*this.getWidth());
        this.tableBackground.setArcWidth(0.01*this.getWidth());
        this.tableBackground.setFill(new ImagePattern(tableBackImage));

        this.cardBackText.setFont(new Font(52));
        this.cardBackText.setLayoutX(this.cardBackPreview.getWidth()/2 - this.cardBackText.getLayoutBounds().getWidth()/2);
        this.cardBackText.setLayoutY(this.cardBackPreview.getHeight() + this.cardBackText.getLayoutBounds().getHeight());

        this.tableBackText.setFont(new Font(52));
        this.tableBackText.setLayoutX(this.tableBackground.getWidth()/2 - this.tableBackText.getLayoutBounds().getWidth()/2);
        this.tableBackText.setLayoutY(this.tableBackground.getHeight() + this.tableBackText.getLayoutBounds().getHeight());

        this.back.setFont(new Font(48));
        this.back.setLayoutX(this.getWidth()*2/20 - this.back.getLayoutBounds().getWidth());
        this.back.setLayoutY(this.getHeight()*19/20);

        this.confirm.setFont(new Font(48));
        this.confirm.setLayoutX(this.getWidth()*17/20);
        this.confirm.setLayoutY(this.getHeight()*19/20);
    }

    /**
     * The method sets up mouse click handlers for all interactive UI elements to allow the user to customize settings
     */

    @Override
    protected void initializeEvents() {
        // Modify deck count
        this.deckPlus.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            options.changeDeckCount(1);
            this.deckCount.setText(String.valueOf(options.getDeckCount()));
        });
        this.deckMinus.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            options.changeDeckCount(-1);
            this.deckCount.setText(String.valueOf(options.getDeckCount()));
        });

        // Modify joker count depending on user action
        this.jokerPlus.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            // If user chooses to press '+' button
            options.changeJokerCount(1);
            this.jokerCount.setText(String.valueOf(options.getJokerCount()));
        });
        this.jokerMinus.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            // If user chooses to press '-' button
            options.changeJokerCount(-1);
            this.jokerCount.setText(String.valueOf(options.getJokerCount()));
        });


        // Select card back image
        this.cardBackPreview.setOnMouseClicked(mouseEvent -> {
            final GameOptions options = GameOptions.getInstance();
            Image image = options.loadCardBackFromExplorer(this.getScene().getWindow());
            if (image != null) this.cardBackPreview.setFill(new ImagePattern(image));
        });

        this.tableBackground.setOnMouseClicked(mouseEvent -> {
            GameOptions options = GameOptions.getInstance();
            File selectedFile = options.fileChooser.showOpenDialog(this.getScene().getWindow());
            if (selectedFile == null) return;

            Image newTableBackground = new Image(selectedFile.toURI().toString());
            GameOptions.copyFileToDirectory(selectedFile.getPath(), "table-background.png");

            options.setTableBackgroundImage(newTableBackground);
            this.tableBackground.setFill(new ImagePattern(newTableBackground));
        });

        // Back button
        this.back.setOnMouseClicked(mouseEvent -> this.changeScreen(this, Title.getInstance()));
        // Confirm button
        this.confirm.setOnMouseClicked(mouseEvent -> this.changeScreen(this, CardFrontCustomization.getInstance()));
    }

    /**
     * Handles keyboard navigation for the Customization screen.
     *
     * @param keyEvent the keyEvent to handle
     */

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        // Order for tab navigation of ui elements
        List<Node> tabOrder = Arrays.asList(this.deckMinus, this.deckPlus, this.jokerMinus, this.jokerPlus,
                this.tableBackground, this.cardBackPreview, this.back, this.confirm);

        if (keyEvent.getCode() == KeyCode.TAB) {
            // Tab navigation
            Node prev = null;
            Node next;

            if (keyEvent.isShiftDown()) {
                // Reverse if holding shift
                if (this.tabSelected != -1) prev = tabOrder.get(this.tabSelected);
                if (this.tabSelected == 0) this.tabSelected = tabOrder.size() - 1;
                else this.tabSelected--;
                next = tabOrder.get(this.tabSelected);
            } else {
                // Forwards if not holding shift
                if (this.tabSelected != -1) prev = tabOrder.get(this.tabSelected);
                if (this.tabSelected == tabOrder.size() - 1) this.tabSelected = 0;
                else this.tabSelected++;
                next = tabOrder.get(this.tabSelected);
            }

            // Unhighlight the element being deselected
            if (prev != null) {
                if (prev.equals(this.tableBackground) || prev.equals(this.cardBackPreview))
                    ((Rectangle) prev).setStroke(null);
                else ((Shape) prev).setFill(Color.BLACK);
            }

            // Highlight the element being navigated onto
            if (next.equals(this.tableBackground) || next.equals(this.cardBackPreview))
                ((Rectangle) next).setStroke(Color.LIGHTSKYBLUE);
            else ((Shape) next).setFill(Color.LIGHTSKYBLUE);

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            // Cancel keyboard navigation on escape key press

            if (this.tabSelected == -1) return;
            Node prev = tabOrder.get(this.tabSelected);
            if (prev.equals(this.tableBackground) || prev.equals(this.cardBackPreview))
                ((Rectangle) prev).setStroke(null);
            else ((Shape) prev).setFill(Color.BLACK);
            this.tabSelected = -1;

        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            // Click on a UI element on enter key press

            if (this.tabSelected >= 0 && this.tabSelected < tabOrder.size())
                Event.fireEvent(tabOrder.get(tabSelected), new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                        MouseButton.PRIMARY, 1, false, false, false, false, true, false,
                        false, false, false, false, null));
        }
    }

    /**
     * Gets the Customization instance
     *
     * @return the Customization instance
     */

    public static Customization getInstance() {
        return Instance;
    }

}
