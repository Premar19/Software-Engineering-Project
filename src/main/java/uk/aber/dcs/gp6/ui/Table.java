/*
 * Table.java
 * User Stories: #1, #2, #3, #4, #6, #12, #15
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Deck;
import uk.aber.dcs.gp6.tabletop.TableObject;
import uk.aber.dcs.gp6.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A class designed to display the main Table that allows interaction with the {@link Card}s and {@link Deck}s
 *
 * @author Natalia Spence, Prem Sharma, Reema Fraser, William Shipp, Francy Sasso
 * @version 1
 * @see Screen
 */

public class Table extends Screen {
    private static final Table Instance = new Table();
    private Group dialogBox;
    private EventHandler<Event> saveDialogEventFilter;
    private Group menuButton;
    private Group menuOptions;
    private boolean menuOpen = false;
    private boolean isReplayMode = false;
    private Rectangle selectionBox;
    private double selectionAnchorX;
    private double selectionAnchorY;
    private Group actionBar;
    private boolean isDealingCards = false;
    private EventHandler<Event> dealingDeckSelectionFilter;
    private int tabSelected = -1;
    private boolean longLayout = false;

    /**
     * The Table constructor, private to ensure only one instance is ever created
     */

    private Table() {
        super();
    }

    /**
     * Initializes the Table, creating all relevant Nodes and adding them
     */

    @Override
    protected void initializeScreen() {
        // Create the initial menu button
        this.menuButton = new Group();
        Rectangle invisButton = new Rectangle();
        invisButton.setFill(Color.TRANSPARENT);
        this.menuButton.getChildren().add(invisButton);
        for (int i = 0; i < 3; i++) this.menuButton.getChildren().add(new Line(0, 0, 0, 0));
        this.getChildren().add(this.menuButton);

        this.menuOptions = createTableMenuOptions();
        this.actionBar = createActionBar();
        this.dialogBox = null;

        this.selectionBox = new Rectangle();
        Color transparentBlue = new Color(0, 0, 1, 0.15);
        this.selectionBox.setFill(transparentBlue);

        this.dealingDeckSelectionFilter = event -> {
            if (event.getEventType() != MouseEvent.MOUSE_CLICKED) {
                event.consume();
            } else if (event.getTarget() instanceof Deck deck) {
                PauseTransition pt = new PauseTransition(Duration.ZERO);
                pt.setOnFinished(e -> {
                    Table.Instance.removeEventFilter(Event.ANY, this.dealingDeckSelectionFilter);
                    Replay.startReplayFromDeck(deck);
                });
                pt.play();
            }
        };
    }

    /**
     * Displays Nodes on the screen, positions and resizes them every time the window size changes
     */

    @Override
    public void displayScreen() {

        StackPane root = new StackPane();

        try {
            // Load image from file
            Image image = GameOptions.getInstance().getTableBackgroundImage(this.getWidth(), this.getHeight());

            // Create ImageView for background
            ImageView backgroundImageView = new ImageView(image);
            backgroundImageView.setFitWidth(this.getWidth());  // Adjust based on window size
            backgroundImageView.setFitHeight(this.getHeight());
            backgroundImageView.setPreserveRatio(false);

            addBackgroundImageEventHandlers(backgroundImageView);

            // Add background and table to StackPane
            root.getChildren().addAll(backgroundImageView);
            this.getChildren().addFirst(root); // Add to the current screen

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Display all TableObjects on the table
        for (Node node : this.getChildren().filtered(n -> n instanceof TableObject)) {
            displayTableObject((TableObject) node);
        }

        // Display the menu button
        showMenuButton();

        // Resize menu options
        showTableMenu();

        // To avoid saving loads of variables for a dialog box that is not on screen for long,
        // it simply recreates it when the screen is resized rather than resizing it
        if (this.dialogBox != null) showSaveDialogBox(((Text) this.dialogBox.getChildren().get(1)).getText());
        if ((!isDealingCards && this.isReplayMode) || (isDealingCards && !Replay.isDeckSelected() &&
                !getSaveDialogText().equals("Recording too long. Continue anyway?"))) {
            displayReplayActionBar(!this.isDealingCards);
        }
    }

    /**
     * Initializes the event handlers/filters
     */

    @Override
    protected void initializeEvents() {
        // Close any popups on the table if one is opened for a TableObject
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (mouseEvent.isSecondaryButtonDown() && mouseEvent.getTarget() instanceof TableObject) closePopups();
        });

        // Close any popups if the Table is pressed
        this.setOnMousePressed(mouseEvent -> {
            closePopups();
            if (mouseEvent.isSecondaryButtonDown()) {
                List<TableObject> selected = TableObject.getSelectedObjects();
                if (!selected.isEmpty()) {
                    Popup popup = new Popup(selected);
                    this.getChildren().addAll(popup);
                    popup.setLayoutX(mouseEvent.getX() + selected.getFirst().getWidth() / 4);
                    popup.setLayoutY(mouseEvent.getY() - selected.getFirst().getHeight() / 8);
                    popup.displayOptions();
                }
            }
        });

        // Close the menu if the Table is clicked
        this.setOnMouseClicked(mouseEvent -> {
            if (this.menuOpen) {
                this.getChildren().remove(this.menuOptions);
                this.menuOpen = false;
            }
        });

        // Toggle the menu when the menu button is clicked
        this.menuButton.getChildren().forEach(n -> n.setOnMouseClicked(mouseEvent -> {
            if (!this.menuOpen) {
                this.menuOpen = true;
                showTableMenu();
                this.getChildren().add(this.menuOptions);
            } else {
                this.getChildren().remove(this.menuOptions);
                this.menuOpen = false;
            }
            mouseEvent.consume();
        }));
    }

    /**
     * Adds event handlers for when the user clicks on the background of the Table
     *
     * @param imgView the ImageView representing the background
     */

    private void addBackgroundImageEventHandlers(ImageView imgView) {
        // Start a selection box
        imgView.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getTarget() == imgView && mouseEvent.isPrimaryButtonDown()) {
                this.selectionAnchorX = mouseEvent.getX();
                this.selectionAnchorY = mouseEvent.getY();
                this.selectionBox.setX(this.selectionAnchorX);
                this.selectionBox.setY(this.selectionAnchorY);
                this.selectionBox.setWidth(0);
                this.selectionBox.setHeight(0);
                this.getChildren().add(this.selectionBox);
            }
        });

        // Adjust the selection box
        imgView.setOnMouseDragged(mouseEvent -> {
            // Smaller x/y of the mouse position and the initial position forms the top left
            this.selectionBox.setX(Math.min(this.selectionAnchorX, mouseEvent.getX()));
            this.selectionBox.setY(Math.min(this.selectionAnchorY, mouseEvent.getY()));
            // Positive difference between the two points is the size
            this.selectionBox.setWidth(Math.abs(this.selectionAnchorX - mouseEvent.getX()));
            this.selectionBox.setHeight(Math.abs(this.selectionAnchorY - mouseEvent.getY()));
        });

        // End the selection box
        imgView.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
            List<Node> overlaps = this.getChildren().stream().filter(n ->
                            n instanceof TableObject && n.getLayoutBounds().intersects(this.selectionBox.getLayoutBounds()))
                    .toList();
            overlaps.forEach(tableObject -> {
                if (!(tableObject instanceof TableObject tableObj) ||
                        TableObject.getSelectedObjects().contains(tableObj)) return;
                TableObject.getSelectedObjects().add(tableObj);
                tableObj.setStroke(Color.PURPLE);
            });
            this.getChildren().remove(this.selectionBox);
        });
    }

    /**
     * A getter to return the instance of the Table
     *
     * @return the Table instance
     */

    public static Table getInstance() {
        return Instance;
    }

    /**
     * Sets the start decks with the provided amount and positions them accordingly
     *
     * @param deckCount the number of decks to create
     */

    public void setStartDecks(int deckCount) {
        // Make the Decks and add them to the table
        if(!isReplayMode) {
            final ArrayList<Deck> decks = new ArrayList<>();
            for (int i = 0; i < deckCount; i++) decks.add(new Deck());
            decks.forEach(deck -> this.getChildren().addAll(deck, deck.getCardCountText()));
            decks.forEach(this::displayTableObject);

            // Starting deck positions
            // This could have been made less complicated, but then it wouldn't look nearly as nice so here we are
            int deckNum = 0;
            for (Deck deck : decks) {
                if (deckCount == 1) {
                    // One Deck
                    deck.setX(this.getWidth() / 2 - deck.getWidth() / 2);
                    deck.setY(this.getHeight() / 2 - deck.getHeight() / 2);
                } else if (deckCount == 2) {
                    // Two Decks
                    deck.setY(this.getHeight() / 2 - deck.getHeight() / 2);
                    if (deckNum == 0) deck.setX(this.getWidth() / 2 - deck.getWidth() * 3 / 2);
                    else deck.setX(this.getWidth() / 2 + deck.getWidth() / 2);
                } else if (deckCount == 3) {
                    // Three Decks
                    if (deckNum == 0) {
                        deck.setX(this.getWidth() / 2 - deck.getWidth() / 2);
                        deck.setY(this.getHeight() / 2 - deck.getHeight() * 3 / 2);
                    } else {
                        deck.setY(this.getHeight() / 2 + deck.getHeight() / 2);
                        if (deckNum == 1) deck.setX(this.getWidth() / 2 - deck.getWidth() * 3 / 2);
                        else deck.setX(this.getWidth() / 2 + deck.getWidth() / 2);
                    }
                } else {
                    // Four Decks
                    if (deckNum < 2) {
                        deck.setY(this.getHeight() / 2 - deck.getHeight() * 3 / 2);
                        if (deckNum == 0) deck.setX(this.getWidth() / 2 - deck.getWidth() * 3 / 2);
                        else deck.setX(this.getWidth() / 2 + deck.getWidth() / 2);
                    } else {
                        deck.setY(this.getHeight() / 2 + deck.getHeight() / 2);
                        if (deckNum == 2) deck.setX(this.getWidth() / 2 - deck.getWidth() * 3 / 2);
                        else deck.setX(this.getWidth() / 2 + deck.getWidth() / 2);
                    }
                }
                deckNum++;
            }
            try {
                Table.getInstance().showSaveDialogBox("Would you like to use a previously \n saved dealing layout?");
            } catch (IllegalStateException ignored) {}
        }
    }

    /**
     * Closes all right click {@link Popup}s
     */

    private void closePopups() {
        // Filter all Nodes on the parent to find the Popups
        Stream<Node> popupStream = this.getChildren().stream().filter(node -> node instanceof Popup);
        for (Node popup : popupStream.toList()) {
            // Remove each Popup
            this.getChildren().remove(popup);
        }
    }

    /**
     * Loads the front and back images associated with a given card.
     * <p>
     * This method attempts to retrieve user-customised images from a specific directory.
     * If a user-provided image is not found, it falls back to using a default image.
     * The front image is based on the individual card, while the back image is standard for all cards.
     *
     * @param card The {@code Card} object for which to load the images.
     * @return An array of two {@code Image} objects where:
     *         <ul>
     *             <li>Index 0 is the front image of the card.</li>
     *             <li>Index 1 is the back image of the card.</li>
     *         </ul>
     */

    private Image[] loadCardImages(Card card) {
        // stores the possible paths to images into variables
        String cardString = Card.toCardString(card);
        // front image for card
        String userFrontImagePath = "/user-images/cards/" + cardString + ".png";
        String defaultFrontImagePath = "/cards/front/" + cardString + ".png";
        // back image for card
        String defaultBackImagePath = "/cards/BACK.png";
        String userBackImagePath = "/user-images/BACK.png";

        // checks if users path for front is null if so front card path is set to default
        // otherwise it's set to the users path
        String frontPath = getClass().getResource(userFrontImagePath) != null ? userFrontImagePath : defaultFrontImagePath;
        // sets front image based on if statement above
        Image frontImage = new Image(Objects.requireNonNull(getClass().getResource(frontPath)).toExternalForm());

        // checks if users path for back is null if so back card path is set to default
        // otherwise it's set to the users path
        String backPath = getClass().getResource(userBackImagePath) != null ? userBackImagePath : defaultBackImagePath;
        //sets back image
        Image backImage = new Image(Objects.requireNonNull(getClass().getResource(backPath)).toExternalForm());

        // array of images initialised (and will only contain) with front image and back image
        return new Image[]{frontImage, backImage};
    }

    /**
     * Adds the required event handlers to a {@link TableObject} to allow the user to interact with them
     *
     * @param tableObject the {@link TableObject} to add the event handlers to
     */

    public static void addEventHandlersForTableObject(TableObject tableObject) {
        tableObject.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.isControlDown()){
                // Ctrl+click selection toggling
                List<TableObject> selectedObjects = TableObject.getSelectedObjects();
                if (selectedObjects.contains(tableObject)){
                    selectedObjects.remove(tableObject);
                    tableObject.setStroke(null);
                } else {
                    selectedObjects.add(tableObject);
                    tableObject.setStroke(Color.PURPLE);
                }
                mouseEvent.consume();
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                // Open a Popup on right click
                Popup popup;
                List<TableObject> selectedObjects = TableObject.getSelectedObjects();
                if (selectedObjects.contains(tableObject)) {
                    popup = new Popup(selectedObjects);
                } else {
                    if (tableObject instanceof Deck &&
                            selectedObjects.stream().anyMatch(tableObj -> tableObj instanceof Card))
                        popup = new Popup(tableObject, true);
                    else popup = new Popup(tableObject, false);
                }
                if (!(tableObject.getParent() instanceof Table parent)) return;
                parent.getChildren().addAll(popup);
                popup.setLayoutX(tableObject.getX() + tableObject.getWidth() + tableObject.getWidth() / 10);
                popup.setLayoutY(tableObject.getY());
                popup.displayOptions();
                mouseEvent.consume();
            }
        });
        // Find mouse position for dragging
        tableObject.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                tableObject.setAnchorX(mouseEvent.getX() - tableObject.getX());
                tableObject.setAnchorY(mouseEvent.getY() - tableObject.getY());
            } else if (mouseEvent.isSecondaryButtonDown()) mouseEvent.consume();
        });
        // Drag and drop movement
        tableObject.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                moveTableObject(mouseEvent, tableObject);
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY && TableObject.getDraggedCard() != null) {
                moveTableObject(mouseEvent, TableObject.getDraggedCard());
            }
        });
        // End drag recording
        tableObject.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (!tableObject.objectIsBeingDragged()) return;
            tableObject.setDragging(false);
            TableObject hovered = TableObject.getHoveredTableObject();
            if (hovered instanceof Deck deck) {
                Recording.createGameAction(ActionType.PLACE_ON_DECK, deck);
                Recording.createGameAction(ActionType.PLACE_ON_DECK, tableObject);

                Table.getInstance().getChildren().remove(tableObject);
                if (tableObject instanceof Deck deckObj) {
                    Table.getInstance().getChildren().remove(deckObj.getCardCountText());
                    deckObj.getCards().forEach(c -> deck.getCards().add(c));
                } else if (tableObject instanceof Card cardObj) deck.getCards().addFirst(cardObj);

                deck.getCardCountText().setText(String.valueOf(deck.getCards().size()));
                Recording.createGameAction(ActionType.PLACE_ON_DECK, deck);
                Table.getInstance().displayTableObject(deck);
                TableObject.getSelectedObjects().remove(tableObject);
                tableObject.setStroke(null);
                TableObject.setHoveredTableObject(null);
            } else Recording.createGameAction(ActionType.END_ACTION, tableObject);
        });
        // Rotate object with scroll wheel
        tableObject.addEventHandler(ScrollEvent.SCROLL, scrollEvent -> {
           tableObject.setRotationAxis(Rotate.Z_AXIS);
            double rotationAngle = tableObject.getRotate();
            double deltaY = scrollEvent.getDeltaY();
            if (deltaY > 0) {
                rotationAngle += 15; // Rotate clockwise
            } else {
                rotationAngle -= 15; // Rotate counterclockwise
            }
            tableObject.setRotate(rotationAngle);
            Recording.createGameAction(ActionType.ROTATE, tableObject);
            tableObject.setPreviousRotation(rotationAngle);
            scrollEvent.consume();
        });
        // Highlight hovered TableObjects
        tableObject.setOnMouseEntered(mouseEvent -> {
            if (!TableObject.getSelectedObjects().contains(tableObject)) tableObject.setStroke(Color.LIGHTSKYBLUE);
        });
        // Unhighlight unhovered TableObjects
        tableObject.setOnMouseExited(mouseEvent -> {
            if (!TableObject.getSelectedObjects().contains(tableObject)) tableObject.setStroke(null);
        });
    }

    /**
     * Adds the required event handlers to a {@link TableObject} to allow the user to interact with them
     * This handles event handlers specific to {@link Deck}s or {@link Card}s, rather than ones that are needed for both
     *
     * @param tableObject the {@link TableObject} to add the event handlers to
     */

    public static void addEventHandlersForSpecificTableObject(TableObject tableObject) {
        if (tableObject instanceof Deck deck ) {
            // Draw a card from the top of the deck
            deck.setOnMouseDragged(mouseEvent -> {
                Card drawnCard = deck.getDrawnCard();
                // Check for right click dragged off the Deck
                if (mouseEvent.isSecondaryButtonDown() &&
                        ((mouseEvent.getX() < deck.getX() || mouseEvent.getX() > deck.getX() + deck.getWidth()) ||
                          mouseEvent.getY() < deck.getY() || mouseEvent.getY() > deck.getY() + deck.getHeight())) {
                    if (drawnCard == null) {
                        // Record the card draw
                        Recording.createGameAction(ActionType.DRAW_CARD, deck);
                        // Draw a Card
                        drawnCard = deck.drawCard();
                        Table.getInstance().getChildren().add(drawnCard);
                        Table.getInstance().displayTableObject(drawnCard);
                    }
                    // Move the drawn Card around
                    drawnCard.setX(mouseEvent.getSceneX() - drawnCard.getWidth()/2);
                    drawnCard.setY(mouseEvent.getSceneY()- drawnCard.getHeight()/2);
                }
            });
            // Reset the above method when right click is released
            deck.setOnMouseReleased(mouseEvent -> {
                Card drawnCard;
                if (deck.getDrawnCard() != null) drawnCard = deck.getDrawnCard();
                else drawnCard = TableObject.getDraggedCard();

                if (mouseEvent.getButton() == MouseButton.SECONDARY && drawnCard != null) {
                    Recording.createGameAction(ActionType.CARD_DRAWN, drawnCard);
                    // Reset the previous event handler so a new card can be drawn
                    deck.setDrawnCard(null);
                    TableObject.setDraggedCard(null);
                    if (deck.getCards().isEmpty()) {
                        Table.getInstance().getChildren().removeAll(deck, deck.getCardCountText());
                    }
                }
            });
        }
    }

    /**
     * Move a {@link TableObject} to follow the mouse
     *
     * @param mouseEvent the MouseEvent for the {@link TableObject} to follow
     * @param tableObject the {@link TableObject} to move
     */

    private static void moveTableObject(MouseEvent mouseEvent, TableObject tableObject) {
        if (!tableObject.objectIsBeingDragged()) tableObject.setDragging(true);

        List<Node> overlaps = Table.getInstance().getChildren().stream().filter(n ->
                n instanceof TableObject && !n.equals(tableObject) &&
                        n.getLayoutBounds().intersects(tableObject.getLayoutBounds())).toList();
        if (!overlaps.isEmpty()) {
            TableObject.setHoveredTableObject((TableObject) overlaps.getFirst());
            List<Node> children = Table.getInstance().getChildren();
            overlaps.forEach(second -> {
                if (children.indexOf(tableObject) < children.indexOf(second)) {
                    children.remove(second);
                    children.add(children.indexOf(tableObject), second);
                    if (second instanceof Deck deck) {
                        children.remove(deck.getCardCountText());
                        children.add(children.indexOf(tableObject), deck.getCardCountText());
                    }
                }
            });
        } else if (TableObject.getHoveredTableObject() != null) TableObject.setHoveredTableObject(null);

        double dx = mouseEvent.getSceneX() - tableObject.getAnchorX() - tableObject.getX();
        double dy = mouseEvent.getSceneY() - tableObject.getAnchorY() - tableObject.getY();

        if (TableObject.getSelectedObjects().contains(tableObject)) {
            for (TableObject obj : TableObject.getSelectedObjects()) {
                obj.setPrevX(obj.getX());
                obj.setPrevY(obj.getY());
                obj.setX(obj.getX() + dx);
                obj.setY(obj.getY() + dy);
                if (obj instanceof Deck deck) {
                    deck.setCardCountLocation();
                }
            }
        } else {
            tableObject.setPrevX(tableObject.getX());
            tableObject.setPrevY(tableObject.getY());
            tableObject.setX(tableObject.getX() + dx);
            tableObject.setY(tableObject.getY() + dy);
            if (tableObject instanceof Deck deck) {
                deck.setCardCountLocation();
            }
        }

        tableObject.setAnchorX(mouseEvent.getSceneX() - tableObject.getX());
        tableObject.setAnchorY(mouseEvent.getSceneY() - tableObject.getY());

        Recording.createGameAction(ActionType.CARD_DRAG, tableObject);
    }

    /**
     * Displays a given {@code TableObject} (either a {@code Card} or a {@code Deck})
     * by sizing it appropriately and assigning the correct image(s) based on the selected card(s) and state.
     * <p>
     * The method calculates the appropriate width and height for the object relative to the
     * current layout bounds. It then sets images based on whether the object is a {@code Card}
     * or a {@code Deck}:
     * <ul>
     *     <li>If the object is a {@code Card}, it loads front and back images and sets the fill
     *     depending on whether the card is face up. It also sets the card's front and back image fields.</li>
     *     <li>If the object is a {@code Deck}, it loads images from the top card, sets the deck's fill (top card will display only)
     *     based on the top card's face-up status, and sets the card count position.</li>
     * </ul>
     *
     * @param tableObject The {@code TableObject} to be displayed and initialised with appropriate images and dimensions.
     */

    public void displayTableObject(TableObject tableObject) {
        double smallSide = Math.min(this.getLayoutBounds().getHeight(), this.getLayoutBounds().getWidth());

        tableObject.setHeight(smallSide / 8);
        tableObject.setWidth(smallSide * 2 / 24);

        // if tableObject being set up is a card
        if (tableObject instanceof Card card) {
            // initialises current cards images by loadCardImages
            Image[] images = loadCardImages(card);
            // sets the front images from image array
            Image frontImage = images[0];
            // sets the back image from image array
            Image backImage = images[1];

            // depending on whether the card is face up or not corresponding image is set to fill card
            card.setFill(new ImagePattern(card.isFaceUp() ? frontImage : backImage));
            // setting front and back images to cards attributes of front and back
            card.setFrontImage(frontImage);
            card.setBackImage(backImage);

            // if tableObject being set up is a deck, correct images are displayed
        } else if (tableObject instanceof Deck deck) {
            // only matters what tp card looks like
            // finds top card
            Card topCard = deck.getTopCard();
            // loads images for top card
            Image[] images = loadCardImages(topCard);
            // sets front and back image
            Image frontImage = images[0];
            Image backImage = images[1];

            // depending on whether the card is face up or not corresponding image is set to fill card
            Image imageToShow = topCard.isFaceUp() ? frontImage : backImage;
            // decks image is set to top card's image
            deck.setFill(new ImagePattern(imageToShow));
            // sets number of cards next to deck
            deck.setCardCountLocation();

        }
    }

    /**
     * Handles key presses made while on the Table
     *
     * @param event the KeyEvent to handle
     */

    @Override
    public void keyPressed(KeyEvent event) {
        // If this is updated, remember to also update the copy in USSixTests
        List<TableObject> selected = TableObject.getSelectedObjects();
        if (selected.isEmpty() && !event.getCode().equals(KeyCode.ESCAPE) &&
            !event.getCode().equals(KeyCode.TAB) && !event.getCode().equals(KeyCode.ENTER)) return;
        System.out.println(event.getCode());

        double step = 10; // how far a card moves with each key press
        double rotationStep = 15; // Rotation Angle increment with each key press
        switch (event.getCode()) {
            case W, UP-> selected.forEach(obj -> {
                obj.setY(obj.getY() - step);
                if (obj instanceof Deck deck) { // Update Deck Card Count Location
                    deck.setCardCountLocation();}
            });
            case A, LEFT-> selected.forEach(obj -> {
                obj.setX(obj.getX() - step);
                if (obj instanceof Deck deck) {
                    deck.setCardCountLocation();}
            });
            case S, DOWN -> selected.forEach(obj -> {
                obj.setY(obj.getY() + step);
                if (obj instanceof Deck deck) {
                    deck.setCardCountLocation();}
            });
            case D, RIGHT -> selected.forEach(obj -> {
                obj.setX(obj.getX() + step);
                if (obj instanceof Deck deck) {
                    deck.setCardCountLocation();}
            });
            case F -> selected.forEach(obj -> {
                if (obj instanceof Card card) card.flipCardOver();
            });
            case R -> selected.forEach(obj -> {
                if (obj instanceof Card card && !card.isFaceUp()) card.revealCard();
            });
            case C -> {
                selected.forEach(obj -> obj.setStroke(null));
                selected.clear();
            }
            case Z -> selected.forEach(obj -> { // Rotate Card Counterclockwise
                obj.setRotationAxis(Rotate.Z_AXIS);
                obj.setRotate(obj.getRotate() - rotationStep);
            });
            case X -> selected.forEach(obj -> { // Rotate Card Clockwise
                obj.setRotationAxis(Rotate.Z_AXIS);
                obj.setRotate(obj.getRotate() + rotationStep);
            });
            case G -> selected.forEach(obj -> {
                if (obj instanceof Deck deck) deck.shuffleDeck();
            });
            case ESCAPE -> {
                Group endReplayPromptBox = Replay.getPromptBox();
                if (this.isDealingCards || this.isReplayMode) {
                    ((Rectangle) this.actionBar.getChildren().get(this.tabSelected)).setStroke(Color.BLACK);
                    this.tabSelected = -1;
                } else if (endReplayPromptBox != null) {
                    ((Rectangle) endReplayPromptBox.getChildren().get(this.tabSelected + 2)).setStroke(Color.BLACK);
                    this.tabSelected = -1;
                } else if (this.dialogBox != null) {
                    ((Rectangle) ((Group) this.dialogBox.getChildren().get(this.tabSelected + 2)).getChildren()
                            .getFirst()).setStroke(Color.CYAN);
                    this.tabSelected = -1;
                } else {
                    if (!this.menuOpen) {
                        this.menuOpen = true;
                        showTableMenu();
                        this.getChildren().add(this.menuOptions);
                    } else {
                        this.getChildren().remove(this.menuOptions);
                        this.menuOpen = false;
                        if (this.tabSelected != -1) {
                            ((Rectangle) this.menuOptions.getChildren().get(this.tabSelected)).setStroke(Color.BLACK);
                        }
                    }
                }
            }
            case TAB -> {
                Group endReplayPromptBox = Replay.getPromptBox();
                int prev = this.tabSelected;
                if (this.isDealingCards || this.isReplayMode || endReplayPromptBox != null) {
                    if (this.tabSelected == -1) {
                        this.tabSelected = 1;
                    } else if (this.tabSelected == 15 && !event.isShiftDown() && endReplayPromptBox == null) {
                        this.tabSelected = 1;
                    } else if (this.tabSelected == 1 && event.isShiftDown()) {
                        if (endReplayPromptBox == null) this.tabSelected = 15;
                        else this.tabSelected = 19;
                    } else if (this.tabSelected == 19 && !event.isShiftDown()) {
                        this.tabSelected = 1;
                    } else this.tabSelected += event.isShiftDown() ? -2 : 2;

                    if (prev != -1) {
                        if (prev <= 15) ((Rectangle) this.actionBar.getChildren().get(prev)).setStroke(Color.BLACK);
                        else ((Rectangle) ((Group) endReplayPromptBox.getChildren().get(prev == 17 ? 2 : 3))
                                .getChildren().getFirst()).setStroke(Color.BLACK);
                    }

                    if (this.tabSelected <= 15) ((Rectangle) this.actionBar.getChildren().get(this.tabSelected))
                            .setStroke(Color.LIGHTSKYBLUE);
                    else if (endReplayPromptBox != null)
                        ((Rectangle) ((Group) endReplayPromptBox.getChildren().get(this.tabSelected == 17 ? 2 : 3))
                            .getChildren().getFirst()).setStroke(Color.LIGHTSKYBLUE);
                } else if (this.dialogBox != null) {
                    if (this.tabSelected == 0) this.tabSelected = 1;
                    else this.tabSelected = 0;

                    if (prev != -1) {
                        ((Rectangle) ((Group) this.dialogBox.getChildren().get(prev + 2)).getChildren().getFirst())
                                .setStroke(Color.CYAN);
                    }

                    ((Rectangle) ((Group) this.dialogBox.getChildren().get(this.tabSelected + 2)).getChildren().getFirst())
                            .setStroke(Color.GREEN);
                } else if (menuOpen) {
                    int modifier = event.isShiftDown() ? -2 : 2;
                    if (this.tabSelected == -1) {
                        this.tabSelected = 0;
                    } else if (this.tabSelected + modifier > -1 && this.tabSelected + modifier < 10) {
                        this.tabSelected = this.tabSelected + modifier;
                    } else if (modifier > 0) {
                        this.tabSelected = 0;
                    } else {
                        this.tabSelected = 8;
                    }

                    if (prev != -1) {
                        ((Rectangle) this.menuOptions.getChildren().get(prev)).setStroke(Color.BLACK);
                    }

                    ((Rectangle) this.menuOptions.getChildren().get(this.tabSelected)).setStroke(Color.LIGHTSKYBLUE);
                } else {

                }
            }
            case ENTER -> {
                if (this.tabSelected != -1) {
                    Group endReplayPromptBox = Replay.getPromptBox();
                    if (this.isDealingCards || this.isReplayMode || endReplayPromptBox != null) {
                        if (this.tabSelected <= 15) {
                            Event.fireEvent(this.actionBar.getChildren().get(this.tabSelected + 2),
                                    new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                                            MouseButton.PRIMARY, 1, false, false, false, false,
                                            true, false, false, false, false, false,
                                            null));
                        } else {
                            Event.fireEvent(endReplayPromptBox.getChildren().get(this.tabSelected == 17 ? 2 : 3),
                                    new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                                            MouseButton.PRIMARY, 1, false, false, false, false,
                                            true, false, false, false, false, false,
                                            null));
                        }
                    } else if (this.dialogBox != null) {
                        Event.fireEvent(((Group) this.dialogBox.getChildren().get(this.tabSelected + 2)).getChildren()
                                        .getFirst(), new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0,
                                0, MouseButton.PRIMARY, 1, false, false, false, false, true,
                                        false, false, false, false, false, null));
                    } else if (menuOpen) {
                        Event.fireEvent(this.menuOptions.getChildren().get(this.tabSelected),
                                new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                                MouseButton.PRIMARY, 1, false, false, false, false, true,
                                        false, false, false, false, false, null));
                    } else {

                    }
                }
            }
        }
        event.consume();
    }

    /**
     * Create a dialog box, often used for saving replay recordings
     *
     * @param promptText the prompt for the dialog box to display
     */

    public void showSaveDialogBox(String promptText) {
        // Display the save dialog and set event handlers
        ArrayList<Node> buttons = displaySaveDialogBox(promptText);
        addSaveDialogEventHandlers(buttons);
        if (this.tabSelected != -1) {
            ((Rectangle) this.menuOptions.getChildren().get(this.tabSelected)).setStroke(Color.BLACK);
            this.tabSelected = 0;
        }
    }

    /**
     * Display a dialog box with the given prompt
     *
     * @param promptText the prompt for the dialog box to display
     * @return an ArrayList of Nodes to add event handlers to so the user can interact with the dialog box
     */

    public ArrayList<Node> displaySaveDialogBox(String promptText) {
        // Create the dialog box Group
        if (this.dialogBox != null) this.getChildren().remove(this.dialogBox);
        this.dialogBox = new Group();

        // The main elements of the dialog
        Rectangle box = new Rectangle();
        Text prompt = new Text(promptText);

        // The yes button
        Group yesButton = new Group();
        Rectangle yesRect = new Rectangle();
        Text yesText = new Text("Yes");

        // The no button
        Group noButton = new Group();
        Rectangle noRect = new Rectangle();
        Text noText = new Text("No");

        // Add everything to the Table Screen
        yesButton.getChildren().addAll(yesRect, yesText);
        noButton.getChildren().addAll(noRect, noText);
        this.dialogBox.getChildren().addAll(box, prompt, yesButton, noButton);
        this.getChildren().add(this.dialogBox);

        // Display the box
        box.setWidth(this.getWidth()/3);
        box.setHeight(this.getHeight()/3);
        box.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        box.applyCss();
        box.setArcWidth(0.01*this.getWidth());
        box.setArcHeight(0.01*this.getWidth());

        // Display the prompt text
        prompt.setFont(new Font(26));
        prompt.setLayoutX(box.getWidth()/2 - prompt.getLayoutBounds().getWidth()/2);
        prompt.setLayoutY(box.getHeight()/4 + prompt.getLayoutBounds().getHeight()/2);

        // Display the yes button box
        yesRect.setWidth(this.getWidth()/7);
        yesRect.setHeight(this.getHeight()/10);
        yesRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        yesRect.applyCss();
        if (this.tabSelected == 0) yesRect.setStroke(Color.GREEN);
        yesRect.setArcWidth(0.01*this.getWidth());
        yesRect.setArcHeight(0.01*this.getWidth());

        // Display the word "Yes"
        yesText.setFont(new Font(26));
        yesText.setLayoutX(yesRect.getWidth()/2 - yesText.getLayoutBounds().getWidth()/2);
        yesText.setLayoutY(yesRect.getHeight()/2 + yesText.getLayoutBounds().getHeight()/4);

        // Display the Yes button
        yesButton.setLayoutX(box.getWidth()/20);
        yesButton.setLayoutY(box.getHeight()*11/12 - yesRect.getHeight());

        // Display the no button box
        noRect.setWidth(this.getWidth()/7);
        noRect.setHeight(this.getHeight()/10);
        noRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        noRect.applyCss();

        noRect.setArcWidth(0.01*this.getWidth());
        noRect.setArcHeight(0.01*this.getWidth());

        // Display the word "No"
        noText.setFont(new Font(26));
        noText.setLayoutX(noRect.getWidth()/2 - noText.getLayoutBounds().getWidth()/2);
        noText.setLayoutY(noRect.getHeight()/2 + noText.getLayoutBounds().getHeight()/4);

        // Display the no button
        noButton.setLayoutX(box.getWidth() - box.getWidth()/20 - noRect.getWidth());
        noButton.setLayoutY(box.getHeight()*11/12 - yesRect.getHeight());

        // Position the dialog box
        this.dialogBox.setLayoutX(this.getWidth()/3);
        this.dialogBox.setLayoutY(this.getHeight()/3);

        // Return the buttons for event registration
        ArrayList<Node> buttons = new ArrayList<>();
        buttons.add(yesRect);
        buttons.add(yesText);
        buttons.add(noRect);
        buttons.add(noText);
        return buttons;
    }

    /**
     * Gets the text of the current dialog box
     *
     * @return the String being displayed on the current dialog box
     */

    private String getSaveDialogText() {
        if (this.dialogBox == null) return "";
        return ((Text) this.dialogBox.getChildren().get(1)).getText();
    }

    /**
     * Adds event handlers to the provided buttons for a dialog box
     *
     * @param buttons the Nodes to add event handlers to
     */

    public void addSaveDialogEventHandlers(ArrayList<Node> buttons) {
        // Determine which recording type this is
        ActionType recordingType;
        String dialogText = getSaveDialogText();
        switch (dialogText) {
            case "Would you like to save this recording?" -> recordingType = ActionType.RECORD_GAME;
            case "Would you like to save this dealing layout?" -> recordingType = ActionType.RECORD_LAYOUT;
            case "Would you like to use a previously \n saved dealing layout?" ->
                    recordingType = ActionType.LOAD_LAYOUT_RECORDING;
            case "Recording too long. Continue anyway?" -> recordingType = ActionType.LAYOUT_TOO_LONG;
            default -> {
                return;
            }
        }
        // to add stuff to this
        // else if (dialogText.equals("Your text here")) {
        //     recordingType = ActionType.LOAD_LAYOUT_RECORDING;
        //     recordingType = ActionType.LOAD_GAME_RECORDING;
        //     etc.
        // }


        // Event handler to save the recording
        EventHandler<MouseEvent> saveButtonHandler = mouseEvent -> {
            // Create a file chooser
            FileChooser fileChooser = new FileChooser();

            if (recordingType == ActionType.RECORD_GAME || recordingType == ActionType.RECORD_LAYOUT) {
                // Set the file type
                if (recordingType == ActionType.RECORD_GAME) {
                    // CSGR = cloud six game recording
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                            "Game Recording File", "*.csgr"));
                    fileChooser.setInitialDirectory(GameOptions.getInstance().getRecordingDirectory(ActionType.RECORD_GAME));
                } else {
                    // CSGRDL = cloud six game recording dealing layout
                    // I would have done CSDL but that is an actual thing already:
                    // https://learn.microsoft.com/en-us/ef/ef6/modeling/designer/advanced/edmx/csdl-spec
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                            "Dealing Layout Recording File", "*.csgrdl"));
                    fileChooser.setInitialDirectory(GameOptions.getInstance().getRecordingDirectory(ActionType.RECORD_LAYOUT));
                }

                // Show the OS specific file chooser dialog box
                File file = fileChooser.showSaveDialog(this.getScene().getWindow());

                // If the user selected a file, save to it and close the dialog box
                 if (file != null) {
                    Recording.saveRecordingFile(file, recordingType);
                    this.closeSaveRecordingDialogBox(recordingType);
                }
            } else if (recordingType == ActionType.LOAD_LAYOUT_RECORDING) {
                isDealingCards = true;
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Layout Recording File",
                        "*.csgrdl"));
                fileChooser.setInitialDirectory(GameOptions.getInstance()
                        .getRecordingDirectory(ActionType.RECORD_LAYOUT));
                File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
                if (selectedFile != null) {
                    boolean success = Replay.loadReplayFromFile(selectedFile);
                    if (!success) {
                        mouseEvent.consume();
                        return;
                    }
                    if (!this.longLayout) this.addEventFilter(Event.ANY, this.dealingDeckSelectionFilter);
                    this.closeSaveRecordingDialogBox(recordingType); // Close dialog
                    Table.getInstance().displayScreen();
                }
            } else {
                this.closeSaveRecordingDialogBox(recordingType);
                Table.getInstance().displayScreen();
                this.addEventFilter(Event.ANY, this.dealingDeckSelectionFilter);
            }
            // to add stuff to this
            // else if (recordingType == ActionType.WHATEVER) {
            //     // yes was clicked, do stuff here with fileChooser
            // }

            mouseEvent.consume();
        };
        // Register save button event handlers
        buttons.get(0).setOnMouseClicked(saveButtonHandler);
        buttons.get(1).setOnMouseClicked(saveButtonHandler);

        // Cancel the recording and close the dialog box
        buttons.get(2).setOnMouseClicked(mouseEvent -> {
            this.closeSaveRecordingDialogBox(recordingType);
            mouseEvent.consume();
        });
        buttons.get(3).setOnMouseClicked(mouseEvent -> {
            this.closeSaveRecordingDialogBox(recordingType);
            mouseEvent.consume();
        });

        // Prevent access to table whilst the save dialog box is open
        this.saveDialogEventFilter = event -> {
            if (!buttons.contains((Node) event.getTarget())) event.consume();
        };
    }

    /**
     * Close the open dialog box
     *
     * @param recordingType the type of dialog box
     */

    private void closeSaveRecordingDialogBox(ActionType recordingType) {
        // Clear the recorded actions of the specified type
        Recording.clearRecordedActions(recordingType);
        if (!this.longLayout) {
            // Close the dialog box
            this.getChildren().remove(this.dialogBox);
            this.dialogBox = null;
            this.tabSelected = -1;
            // Remove the event filter preventing access to the table
            this.removeEventFilter(Event.ANY, this.saveDialogEventFilter);
        } else {
            this.longLayout = false;
            if (this.tabSelected != -1) this.tabSelected = 0;
        }
    }

    /**
     * Show the menu button (3 lines) in the corner of the table
     */

    private void showMenuButton() {
        // Find the desired size of menu buttons
        double buttonSize = Math.min(this.getHeight()/12, this.getWidth()/12);

        // Resize the invisible rectangle
        Rectangle invisButton = (Rectangle) this.menuButton.getChildren().filtered(n -> n instanceof Rectangle).getFirst();
        invisButton.setWidth(buttonSize*1.3);
        invisButton.setHeight(buttonSize);

        // Resize and reposition each line
        int pos = 1;
        for (Node node : this.menuButton.getChildren()) {
            if (!(node instanceof Line line)) continue;
            line.setEndX(buttonSize*1.3);
            line.setStartY(buttonSize*pos/12);
            line.setEndY(buttonSize*pos/12);
            line.setStrokeWidth(buttonSize/12);
            pos += 5;
        }

        // Position the menu button
        this.menuButton.setLayoutX(this.getWidth() - buttonSize*1.5);
        this.menuButton.setLayoutY(buttonSize*0.1);
    }

    /**
     * Create the options for the table menu
     *
     * @return a Group containing the menu options
     */

    private Group createTableMenuOptions() {
        // Create the menu options
        Group menuOptions = new Group();

        // Set up each button
        for (int buttonNumber = 0; buttonNumber < 5; buttonNumber++) {
            Rectangle button = new Rectangle();
            button.setFill(Color.WHITE);
            button.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
            button.applyCss();
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setImage(getMenuButtonImage(buttonNumber));
            menuOptions.getChildren().addAll(button, imageView);
            setupMenuButton(button, imageView, buttonNumber);
        }

        return menuOptions;
    }

    /**
     * Get the image for any given table menu button
     *
     * @param buttonNumber the number of the button
     * @return the image corresponding to that number
     */

    private Image getMenuButtonImage(int buttonNumber) {
        switch (buttonNumber) {
            case 0 -> {
                return new Image("table-menu-buttons/dealing.png");
            }
            case 1 -> {
                return new Image("table-menu-buttons/recording.png");
            }
            case 2 -> {
                return new Image("table-menu-buttons/grid.png");
            }
            case 3 -> {
                return new Image("table-menu-buttons/sound.png");
            }
            case 4 -> {
                return new Image("table-menu-buttons/exit.png");
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Give a menu button its event handlers to allow the user to click them
     *
     * @param button the button itself
     * @param imageView the ImageView displayed on the button
     * @param buttonNumber the number of the button
     */

    private void setupMenuButton(Rectangle button, ImageView imageView, int buttonNumber) {
        // Load each button's image and set their onClick event
        switch (buttonNumber) {
            case 0 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    boolean enabled = Recording.toggleRecordingLayout();
                    button.setFill(enabled ? Color.MEDIUMPURPLE : Color.WHITE);
                    mouseEvent.consume();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 1 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    boolean enabled = Recording.toggleRecordingGame();
                    button.setFill(enabled ? Color.MEDIUMPURPLE : Color.WHITE);
                    mouseEvent.consume();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 2 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    // toggle grid mode
                    mouseEvent.consume();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 3 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    // toggle sound effects
                    mouseEvent.consume();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 4 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    Recording.endAllRecordings();
                    Replay.endReplayThread();
                    ((Stage) this.getScene().getWindow()).close();
                    Platform.exit();
                    mouseEvent.consume();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
        }
    }

    /**
     * Show the table menu
     */

    private void showTableMenu() {
        // Find the desired size of menu buttons
        double buttonSize = Math.min(this.getHeight()/12, this.getWidth()/12);

        // Display each menu button
        for (int i = 0, childrenSize = this.menuOptions.getChildren().size(); i < childrenSize; i += 2) {
            if (!(this.menuOptions.getChildren().get(i) instanceof Rectangle button)) continue;
            button.setArcWidth(0.01*this.getWidth());
            button.setArcHeight(0.01*this.getWidth());
            button.setWidth(buttonSize*1.4);
            button.setHeight(buttonSize);
            button.setY((i/2.0)*(buttonSize*1.1));
            if (!(this.menuOptions.getChildren().get(i+1) instanceof ImageView imageView)) continue;
            imageView.setFitWidth(buttonSize*1.4);
            imageView.setFitHeight(buttonSize);
            imageView.setY((i/2.0)*(buttonSize*1.1));
            imageView.setX((buttonSize*1.4)/2 - Math.min(imageView.getFitWidth(), imageView.getFitHeight() *
                    (imageView.getImage().getWidth() / imageView.getImage().getHeight()))/2);
        }

        // Move menu options below the menu button
        this.menuOptions.setLayoutX(this.menuButton.getLayoutX() - buttonSize*0.05);
        this.menuOptions.setLayoutY(this.menuButton.getLayoutY() + buttonSize*1.1);
    }

    /**
     * Reset the table after a replay is finished
     */

    public void resetTable() {
        this.getChildren().removeAll(this.getChildren());
        initializeScreen();
        initializeEvents();
    }

    /**
     * Get the image for an action bar button in replays
     *
     * @param buttonNumber the number of the button
     * @return the image corresponding to that button
     */

    private Image getActionBarButtonImage(int buttonNumber) {
        switch (buttonNumber) {
            case 0 -> {
                return new Image("replay-action-bar-buttons/pause.png");
            }
            case 1 -> {
                return new Image("replay-action-bar-buttons/play.png");
            }
            case 2 -> {
                return new Image("replay-action-bar-buttons/skip-backwards.png");
            }
            case 3 -> {
                return new Image("replay-action-bar-buttons/skip-forwards.png");
            }
            case 4 -> {
                return new Image("replay-action-bar-buttons/slow.png");
            }
            case 5 -> {
                return new Image("replay-action-bar-buttons/fast.png");
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Set up an action bar button
     *
     * @param button the button itself
     * @param imageView the ImageView displayed on the button
     * @param buttonNumber the number of the button
     */

    private void setupActionBarButton(Rectangle button, ImageView imageView, int buttonNumber) {
        // Load each button's image and set their onClick event
        switch (buttonNumber) {
            case 0 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    if (button.isVisible()) Replay.pauseReplay();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 1 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    if (button.isVisible()) Replay.playReplay();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 2 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    if (button.isVisible()) Replay.moveToPrev();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 3 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    if (button.isVisible()) Replay.moveToNext();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 4 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    if (button.isVisible()) Replay.slowReplay();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
            case 5 -> {
                EventHandler<MouseEvent> eventHandler = (MouseEvent mouseEvent) -> {
                    if (button.isVisible()) Replay.fastReplay();
                };
                button.setOnMouseClicked(eventHandler);
                imageView.setOnMouseClicked(eventHandler);
            }
        }
    }

    /**
     * Display the action bar for replays
     * If the buttons are not included, it instead displays the select a deck prompt for US13
     *
     * @param withButtons whether it should include the buttons
     */

    private void displayReplayActionBar(boolean withButtons) {
        // Display the box
        Rectangle box = (Rectangle) this.actionBar.getChildren().getFirst();
        box.setWidth(this.getWidth());
        box.setHeight(this.getHeight()/10);
        box.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        box.applyCss();

        // Add everything to the Table Screen
        this.getChildren().add(this.actionBar);

        if (!withButtons) {
            // Display the prompt text
            Text prompt = (Text) this.actionBar.getChildren().getLast();
            prompt.setLayoutX(box.getWidth() / 2 - prompt.getLayoutBounds().getWidth() / 2);
            prompt.setLayoutY(box.getHeight() / 2 + prompt.getLayoutBounds().getHeight() / 2);
            this.actionBar.getChildren().forEach(n -> n.setVisible(false));
            box.setVisible(true);
            prompt.setVisible(true);
        } else {
            this.actionBar.getChildren().getLast().setVisible(false);
            // Display each action bar button
            for (int i = 1, childrenSize = this.actionBar.getChildren().size(); i < childrenSize; i += 2) {
                if (!(this.actionBar.getChildren().get(i) instanceof Rectangle button)) continue;
                button.setArcWidth(0.01*this.getWidth());
                button.setArcHeight(0.01*this.getWidth());
                button.setWidth(this.getWidth()/20);
                button.setHeight(box.getLayoutBounds().getHeight()*4/5);
                button.setY(box.getLayoutBounds().getHeight()/2 - button.getHeight()/2);
                button.setX(box.getLayoutBounds().getWidth()*i/16 - button.getWidth()/2);
                button.setVisible(true);
                if (!(this.actionBar.getChildren().get(i+1) instanceof ImageView imageView)) continue;
                imageView.setFitWidth(this.getWidth()/20);
                imageView.setFitHeight(box.getLayoutBounds().getHeight()*4/5 - 2);
                imageView.setY(box.getLayoutBounds().getHeight()/2 - button.getHeight()/2 + 1);
                imageView.setX(box.getLayoutBounds().getWidth()*i/16 - button.getWidth()/2 +
                        button.getWidth()/2 - imageView.getLayoutBounds().getWidth()/2);
                imageView.setVisible(true);
            }
            Text speedText = (Text) this.actionBar.getChildren().get(this.actionBar.getChildren().size() - 2);
            speedText.setLayoutX(box.getLayoutBounds().getWidth()*11/16 - box.getLayoutBounds().getWidth()*1/16 -
                    speedText.getLayoutBounds().getWidth()/2);
            speedText.setLayoutY(box.getHeight() / 2 + speedText.getFont().getSize() / 2);
            speedText.setVisible(true);
        }
    }

    /**
     * Create the action bar for replays
     *
     * @return a Group containing the action bar elements
     */

    private Group createActionBar() {
        Group actionBar = new Group();

        // The bar
        Rectangle box = new Rectangle();
        actionBar.getChildren().add(box);

        // Set up each button
        for (int buttonNumber = 0; buttonNumber < 6; buttonNumber++) {
            Rectangle button = new Rectangle();
            button.setFill(Color.WHITE);
            button.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
            button.applyCss();
            button.setVisible(false);
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setImage(getActionBarButtonImage(buttonNumber));
            imageView.setVisible(false);
            actionBar.getChildren().addAll(button, imageView);
            setupActionBarButton(button, imageView, buttonNumber);
        }

        for (int buttonNumber = 3; buttonNumber < 5; buttonNumber++) {
            Rectangle button = new Rectangle();
            button.setFill(Color.WHITE);
            button.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
            button.applyCss();
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setImage(getMenuButtonImage(buttonNumber));
            actionBar.getChildren().addAll(button, imageView);
            setupMenuButton(button, imageView, buttonNumber);
        }

        Text speedText = new Text(Integer.toString(Replay.getSpeed()));
        speedText.setFont(new Font(40));
        speedText.setVisible(false);

        Text prompt = new Text("Select a deck to deal from");
        prompt.setFont(new Font(26));
        prompt.setVisible(false);

        actionBar.getChildren().addAll(speedText, prompt);

        return actionBar;
    }

    /**
     * A getter to return the action bar
     *
     * @return the action bar Group
     */

    public Group getActionBar() {
        return actionBar;
    }

    /**
     * End a dealing layout replay
     *
     * @return true if there was a dealing layout replay to end, otherwise false
     */

    public boolean endDealingReplay() {
        if (this.isDealingCards) {
            this.isDealingCards = false;
            return true;
        } else return false;
    }

    /**
     * Check if a dealing layout is being played
     *
     * @return true if a dealing layout is being played, otherwise false
     */

    public boolean isDealing() {
        return this.isDealingCards;
    }

    /**
     * Remove the replay action bar
     */

    public void removeActionBar() {
        Table.getInstance().getChildren().remove(this.actionBar);
        this.actionBar = null;
    }

    /**
     * Update the text displaying the current speed on the replay action bar
     *
     * @param speed the new speed
     */

    public void updateActionBarSpeedText(int speed) {
        ((Text) this.actionBar.getChildren().get(this.actionBar.getChildren().size() - 2))
                .setText(Integer.toString(speed));
    }

    /**
     * Set whether the current dealing layout is longer than the current deck size
     *
     * @param isLong true if the dealing layout is too long, otherwise false
     */

    public void setLongLayout(boolean isLong) {
        this.longLayout = isLong;
    }

    /**
     * Check if the current dealing layout is too long for the current deck size
     *
     * @return true if the dealing layout is too long, otherwise false
     */

    public boolean isLongLayout() {
        return this.longLayout;
    }

    /**
     * Set whether a replay is occurring on the Table
     *
     * @param replayMode a boolean indicating if a replay is occurring
     */

    public void setReplayMode(boolean replayMode) {
        isReplayMode = replayMode;
    }
}
