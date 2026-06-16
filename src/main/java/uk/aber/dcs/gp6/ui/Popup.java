/*
 * Popup.java
 * User Stories: #5, #7, #9, #10, #11
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.ui;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import uk.aber.dcs.gp6.tabletop.Card;
import uk.aber.dcs.gp6.tabletop.Deck;
import uk.aber.dcs.gp6.tabletop.TableObject;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import uk.aber.dcs.gp6.util.ActionType;
import uk.aber.dcs.gp6.util.Recording;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A class designed to manage the Popup that appears on right-clicking a {@link TableObject}.
 *
 * @author Natalia Spence, Joe Ball, Niall Perry, William Shipp
 * @version 1
 * @see TableObject
 * @see Table
 */

public class Popup extends Group {
    private Group flipButton;
    private Group revealButton;
    private Group shuffleButton;
    private Group repositionTopButton;
    private Group repositionTopToMiddleButton;
    private Group repositionTopToBottomButton;
    private Group placeOnDeckButton;
    private final List<TableObject> tableObjects;

    /**
     * The popup constructor for displaying a popup when a single {@link TableObject} is selected
     *
     * @param tableObject the selected {@link TableObject}
     */

    public Popup(TableObject tableObject, boolean cardSelectedDeckClicked) {
        // Popup on a single TableObject
        this.tableObjects = Collections.singletonList(tableObject);
        makeOptions(cardSelectedDeckClicked);
        addEventHandlers();
    }

    /**
     * The popup constructor for displaying a popup when multiple {@link TableObject}s are selected
     *
     * @param tableObjects the selected {@link TableObject}s
     */

    public Popup(List<TableObject> tableObjects) {
        // Popup on multiple selected TableObjects
        this.tableObjects = tableObjects;
        makeOptions(false);
        addEventHandlers();
    }

    /**
     * Creates the options that should be displayed in the popup
     *
     * @param cardSelectedDeckClicked whether the place on deck option should be displayed
     */

    private void makeOptions(boolean cardSelectedDeckClicked) {
        // Create flip button
        this.flipButton = new Group();
        Rectangle flipRect = new Rectangle();
        flipRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        flipRect.applyCss();
        Text flipText = new Text("Flip");
        flipText.setFont(new Font(16));
        this.flipButton.getChildren().addAll(flipRect, flipText);

        // Create reveal button
        this.revealButton = new Group();
        Rectangle revealRect = new Rectangle();
        revealRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        revealRect.applyCss();
        Text revealText = new Text("Reveal");
        revealText.setFont(new Font(16));
        this.revealButton.getChildren().addAll(revealRect, revealText);

        // Create shuffle button
        this.shuffleButton = new Group();
        Rectangle shuffleRect = new Rectangle();
        shuffleRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        shuffleRect.applyCss();
        Text shuffleText = new Text("Shuffle");
        shuffleText.setFont(new Font(16));
        this.shuffleButton.getChildren().addAll(shuffleRect, shuffleText);

        // Create reposition top button
        this.repositionTopButton = new Group();
        Rectangle repositionTopRect = new Rectangle();
        repositionTopRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        repositionTopRect.applyCss();
        Text repositionTopText = new Text("Reposition Top");
        repositionTopText.setFont(new Font(16));
        this.repositionTopButton.getChildren().addAll(repositionTopRect, repositionTopText);

        // Create place on deck button
        if (cardSelectedDeckClicked ||
                (!TableObject.getSelectedObjects().isEmpty() && tableObjects.size() == 1 &&
                        tableObjects.getFirst() instanceof Card &&
                        !TableObject.getSelectedObjects().contains(tableObjects.getFirst()))) {
            this.placeOnDeckButton = new Group();
            Rectangle placeOnDeckRect = new Rectangle();
            placeOnDeckRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
            placeOnDeckRect.applyCss();
            Text placeOnDeckText = new Text("Place On Deck");
            placeOnDeckText.setFont(new Font(16));
            this.placeOnDeckButton.getChildren().addAll(placeOnDeckRect, placeOnDeckText);
        }

        // Add the buttons to the popup
        if (this.tableObjects.stream().anyMatch(tableObject -> tableObject instanceof Card))
            this.getChildren().addAll(this.flipButton, this.revealButton);
        if (cardSelectedDeckClicked ||
                (!TableObject.getSelectedObjects().isEmpty() && tableObjects.size() == 1 &&
                        tableObjects.getFirst() instanceof Card) &&
                        !TableObject.getSelectedObjects().contains(tableObjects.getFirst()))
            this.getChildren().add(this.placeOnDeckButton);
        if (this.tableObjects.stream().anyMatch(tableObject -> tableObject instanceof Deck))
            this.getChildren().addAll(this.shuffleButton, repositionTopButton);
    }

    /**
     * Create the sub-menu options for the reposition top menu option
     */

    public void makeSubMenuOptions() {
        // Create reposition top to middle button
        this.repositionTopToMiddleButton = new Group();
        Rectangle repositionTopToMiddleRect = new Rectangle();
        repositionTopToMiddleRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        repositionTopToMiddleRect.applyCss();
        Text repositionTopToMiddleText = new Text("To Middle");
        repositionTopToMiddleText.setFont(new Font(16));
        this.repositionTopToMiddleButton.getChildren().addAll(repositionTopToMiddleRect, repositionTopToMiddleText);

        // Create reposition top to bottom button
        this.repositionTopToBottomButton = new Group();
        Rectangle repositionTopToBottomRect = new Rectangle();
        repositionTopToBottomRect.setStyle("-fx-fill: white; -fx-stroke: cyan; -fx-stroke-width: 2;");
        repositionTopToBottomRect.applyCss();
        Text repositionTopToBottomText = new Text("To Bottom");
        repositionTopToBottomText.setFont(new Font(16));
        this.repositionTopToBottomButton.getChildren().addAll(repositionTopToBottomRect, repositionTopToBottomText);

        // Add the buttons to the popup
        this.getChildren().addAll(this.repositionTopToMiddleButton, this.repositionTopToBottomButton);
    }

    /**
     * Display the popup options
     * Calls the internal displayOptions method with the appropriate parameters to display the popup
     */

    public void displayOptions() {
        displayOptions(this.getChildren().stream().filter(node -> node instanceof Group).toList(), 0, 0);
    }

    /**
     * Display the popup options with a specified offset
     * The internal code to display popup options correctly
     *
     * @param groups the options to be displayed
     * @param xOffset the x offset of the options relative to the top left of the popup
     * @param yOffset the y offset of the options relative to the top left of the popup
     */

    private void displayOptions(List<Node> groups, double xOffset, double yOffset) {
        // If the popup isn't on anything, you can't position it
        if (this.getParent() == null) return;
        for (int i = 0; i < groups.size(); i++) {
            Group buttonGroup = (Group) groups.get(i);
            displayButtonGroup(buttonGroup);
            if (i != 0)
                buttonGroup.setLayoutY(this.getParent().getLayoutBounds().getWidth() * (1/(44*(1.0/i))) + yOffset);
            else buttonGroup.setLayoutY(yOffset);
            buttonGroup.setLayoutX(xOffset);
        }
    }

    /**
     * Display a button for a popup option
     *
     * @param buttonGroup the button's Group object to be displayed
     */

    private void displayButtonGroup(Group buttonGroup) {
        // Resize and position elements of the popup
        // Might be able to replace Group with StackPane to auto-centre text for us
        Rectangle rect = (Rectangle) buttonGroup.getChildren().getFirst();
        rect.setWidth(this.getParent().getLayoutBounds().getHeight()/9 - 4);
        rect.setHeight(this.getParent().getLayoutBounds().getWidth()/46 - 4);
        Text text = (Text) buttonGroup.getChildren().getLast();
        text.setLayoutY(rect.getHeight()/2 + (text.getLayoutBounds().getHeight()/4));
        text.setLayoutX(rect.getWidth()/2 - (text.getLayoutBounds().getWidth()/2));
    }

    /**
     * Adds the EventHandlers required for the user to interact with the popup
     * This determines what should happen when each option is pressed and carries out the required actions
     */

    private void addEventHandlers() {
        // Loop over this.tableObjects and do the action to everything
        // To check if something is a card, do `if (tableObject instanceof Card card) {}`
        // To check if something is a deck, do `if (tableObject instanceof Deck deck) {}`
        // Each of the above lines give you a variable (card or deck) that is the card or deck in question

        // Flip
        this.flipButton.setOnMousePressed(mouseEvent -> {
            // Flip all selected Cards
            for (TableObject obj : tableObjects) {
                if (obj instanceof Card card) {
                    card.flipCardOver();
                }
            }
            Table.getInstance().getChildren().remove(this);
            mouseEvent.consume();
        });

        // User clicked reveal
        this.revealButton.setOnMousePressed(mouseEvent -> {
            // Reveal Cards
            for (TableObject obj : tableObjects) {
                if (obj instanceof Card card) {
                    if (!card.isFaceUp()) {
                        card.revealCard();
                    }
                }
            }
            Table.getInstance().getChildren().remove(this);
            mouseEvent.consume();
        });

        if (this.placeOnDeckButton != null) {
            // User clicked move onto deck
            this.placeOnDeckButton.setOnMousePressed(mouseEvent -> {
                Deck deck;
                if (this.tableObjects.size() != 1 || this.tableObjects.getFirst() instanceof Deck) {
                    deck = ((Deck) this.tableObjects.getFirst());
                } else {
                    deck = new Deck(this.tableObjects);
                    deck.setX(this.tableObjects.getFirst().getX());
                    deck.setY(this.tableObjects.getFirst().getY());
                    Table.getInstance().displayTableObject(deck);
                    Table.getInstance().getChildren().remove(this.tableObjects.getFirst());
                    Table.getInstance().getChildren().addAll(deck, deck.getCardCountText());
                }

                Recording.createGameAction(ActionType.PLACE_ON_DECK, deck);
                List<TableObject> cards = TableObject.getSelectedObjects();

                // Move all selected TableObjects onto the top of the Deck
                cards.forEach(tableObj -> {
                    if (tableObj instanceof Card cardObj) {
                        Recording.createGameAction(ActionType.PLACE_ON_DECK, cardObj);
                        deck.getCards().addFirst(cardObj);
                        Table.getInstance().getChildren().remove(cardObj);
                    } else if (tableObj instanceof Deck deckObj) {
                        deckObj.getCards().forEach(card -> {
                            Recording.createGameAction(ActionType.PLACE_ON_DECK, card);
                            deck.getCards().addFirst(card);
                        });
                        Table.getInstance().getChildren().removeAll(deckObj, deckObj.getCardCountText());
                    }
                    tableObj.setStroke(null);
                });

                TableObject.getSelectedObjects().clear();
                Recording.createGameAction(ActionType.PLACE_ON_DECK, deck);
                deck.getCardCountText().setText(String.valueOf(deck.getCards().size()));
                Table.getInstance().getChildren().remove(this);
                Table.getInstance().displayTableObject(deck);

                mouseEvent.consume();
            });
        }

        // User clicked shuffle
        this.shuffleButton.setOnMousePressed(mouseEvent -> {
            // Shuffle Decks
            Table.getInstance().getChildren().remove(this);
            for (TableObject tableObject : this.tableObjects) {
                if (tableObject instanceof Deck deck) {
                    deck.shuffleDeck();
                }
            }
            mouseEvent.consume();
        });

        // User clicked reposition top
        this.repositionTopButton.setOnMousePressed(mouseEvent -> {
            // Open Reposition Top Sub-Menu
            makeSubMenuOptions();
            addSubMenuEventHandlers();
            List<Node> groups = new ArrayList<>();
            groups.add(this.repositionTopToMiddleButton);
            groups.add(this.repositionTopToBottomButton);
            displayOptions(groups, this.repositionTopButton.getLayoutBounds().getWidth() +
                    this.tableObjects.getFirst().getWidth()/10, this.repositionTopButton.getLayoutY());
            mouseEvent.consume();
        });

        // Don't close the popup if the user clicks between the buttons
        this.setOnMousePressed(Event::consume);
    }

    /**
     * Adds the EventHandlers for the sub-menu used for the reposition top option
     */

    private void addSubMenuEventHandlers() {
        // Loop over this.tableObjects and do the action to everything
        // To check if something is a card, do `if (tableObject instanceof Card card) {}`
        // To check if something is a deck, do `if (tableObject instanceof Deck deck) {}`
        // Each of the above lines give you a variable (card or deck) that is the card or deck in question

        // User clicked reposition to middle
        this.repositionTopToMiddleButton.setOnMousePressed(mouseEvent -> {
            // Reposition Top Cards Of Decks To Middle
            Random random = new Random();
            for (TableObject tableObject : this.tableObjects) {
                if (tableObject instanceof Deck deck) {
                    Recording.createGameAction(ActionType.REPOSITION_TOP, deck);
                    SequentialTransition shuffleAnimation = deck.getAnimation(1);
                    shuffleAnimation.play(); // Play animation For Repositioning A card
                    Card card = deck.getCards().removeFirst();
                    deck.getCards().add(random.nextInt(0, deck.getCards().size() + 1), card);
                    Recording.createGameAction(ActionType.REPOSITION_TOP, deck);
                    deck.getCardCountText().setText(String.valueOf(deck.getCards().size()));
                    Table.getInstance().displayTableObject(deck);
                }
            }
            Table.getInstance().getChildren().remove(this);
            mouseEvent.consume();
        });

        // User clicked reposition to bottom
        this.repositionTopToBottomButton.setOnMousePressed(mouseEvent -> {
            // Reposition Top Cards Of Decks To Bottom
            for (TableObject tableObject : this.tableObjects) {
                if (tableObject instanceof Deck deck) {
                    Recording.createGameAction(ActionType.REPOSITION_TOP, deck);
                    SequentialTransition shuffleAnimation = deck.getAnimation(1);
                    shuffleAnimation.play(); // Play animation For Repositioning A card
                    Card card = deck.getCards().removeFirst();
                    deck.getCards().add(card);
                    Recording.createGameAction(ActionType.REPOSITION_TOP, deck);
                    deck.getCardCountText().setText(String.valueOf(deck.getCards().size()));
                    Table.getInstance().displayTableObject(deck);
                }
            }
            Table.getInstance().getChildren().remove(this);
            mouseEvent.consume();
        });
    }
}
