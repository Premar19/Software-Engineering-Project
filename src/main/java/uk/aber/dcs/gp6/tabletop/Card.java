/*
 * Card.java
 * User Stories: #3, #9, #11, #14
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.tabletop;
import javafx.animation.PauseTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import uk.aber.dcs.gp6.ui.Table;
import uk.aber.dcs.gp6.util.ActionType;
import uk.aber.dcs.gp6.util.Recording;

import java.util.HashMap;

/**
 * A class representing a single card to be displayed on the table.
 * This contains all methods and data relating exclusively to a card.
 *
 * @author William Shipp, Natalia Spence, Prem Sharma, Niall Perry
 * @version 1
 * @see TableObject
 * @see Suit
 * @see Deck
 */

public class Card extends TableObject {
    private final Suit suit;  // 0 = Heart, 1 = Diamond, 2 = Spade, 3 = Club, 4 = Joker
    private final int index; // 1 = Ace, 2-10 = Cards 2 to 10, 11 = Jack, 12 = Queen, 13 = King, 14 = Joker
    private boolean isFaceUp = false;
    private Image frontImage;
    private Image backImage;
    private static final HashMap<Suit, HashMap<Integer, Integer>> createdCards = new HashMap<>() {
        {
            put(Suit.CLUB, new HashMap<>());
            put(Suit.DIAMOND, new HashMap<>());
            put(Suit.HEART, new HashMap<>());
            put(Suit.SPADE, new HashMap<>());
            put(Suit.JOKER, new HashMap<>());
        }
    };

    /**
     * The main card constructor
     * This creates a card with the given suit and value
     *
     * @param suit the suit of the card
     * @param number the value of the card
     */

    public Card(int suit, int number) {
        // Create a Card
        super();
        this.suit = Suit.values()[suit];
        this.index = number;
        this.customId = createCardId(this.suit, this.index);
        addEventHandlers();
    }

    /**
     * The card constructor used for replay loading
     * This version specifies the id and whether the card is face up to ensure the correct card is referenced
     *
     * @param suit the suit of the card
     * @param number the value of the card
     * @param isFaceUp whether the card is face up or not
     * @param customId the custom id of the card to differentiate it from identical cards in a replay
     */

    public Card(Suit suit, int number, boolean isFaceUp, int customId) {
        // Create a Card
        super();
        this.suit = suit;
        this.index = number;
        this.isFaceUp = isFaceUp;
        this.customId = customId;
        addEventHandlers();
    }

    /**
     * A method to generate a unique id for each card with the same suit and value
     * Used to differentiate between identical cards in a replay
     *
     * @param suit the suit of the card
     * @param index the value of the card
     * @return the card's unique id
     */

    private static int createCardId(Suit suit, int index) {
        if (createdCards.get(suit).containsKey(index)) {
            createdCards.get(suit).put(index, createdCards.get(suit).get(index) + 1);
        } else createdCards.get(suit).put(index, 1);
        return createdCards.get(suit).get(index) - 1;
    }

    /**
     * Get the suit of a card object: Heart, Diamond, Spade, Club
     *
     * @return the Suit of the card
     */

    public Suit getSuit() {
        return suit;
    }

    /**
     * A method to tell the table to add event handlers to the card so the user can interact with it
     */

    private void addEventHandlers() {
        if (Platform.isFxApplicationThread()) Table.addEventHandlersForSpecificTableObject(this);
    }

    /**
     * Creates a custom string representation of the Card
     * This includes:
     * - Suit
     * - Value
     * - Face Up/Down
     * - Custom ID
     *
     * @return a string representing the Card
     */

    @Override
    public String toString() {
        // Custom string representation of Card
        // Useful for saving to a file and printing for debugging
        return toCardString(this) + (isFaceUp ? "U" : "D") + this.customId;
    }

    /**
     * A method to generate a card string from a given suit and value
     * Used to determine the string representation of a card without creating one
     *
     * @param suit the suit of the card
     * @param index the value of the card
     * @return a 2 letter string representation of the card
     */

    public static String toCardString(Suit suit, int index) {
        // String representation of a Card without isFaceUp without having to create one
        String indexString;
        if (index == 10) indexString = "T";
        else if (index == 11) indexString = "J";
        else if (index == 12) indexString = "Q";
        else if (index == 13) indexString = "K";
        else if (index == 14) indexString = "J";
        else indexString = Integer.toString(index);
        return suit.toString() + indexString;
    }

    /**
     * A method to generate a card string from a given suit and value
     * Overloads the toCardString method to work with a Card or a suit and value
     *
     * @param card the card to find a string representation of
     * @return a 2 letter string representation of the card
     */

    public static String toCardString(Card card) {
        // String representation of a Card without isFaceUp
        return toCardString(card.suit, card.index);
    }

    /**
     * Creates a card from a string representation of the card
     * Used to create cards from loaded recording data
     *
     * @param data the card's data
     * @return a Card object created from the given data
     */

    public static Card toCard(String data) {
        if (data.length() < 3) return null; // Safety check

        String suitChar = data.substring(0, 1); // e.g., "C"
        String valueChar = data.substring(1, 2); // e.g., "J", "T", "2", etc.
        String faceState = data.substring(2, 3); // "D" or "U"
        String idChar = data.substring(3, 4); // "0", "1", etc.

        int index;
        switch (valueChar) {
            case "T" -> index = 10;
            case "J" -> index = 11;
            case "Q" -> index = 12;
            case "K" -> index = 13;
            case "A" -> index = 1;
            default -> {
                try {
                    index = Integer.parseInt(valueChar);
                } catch (NumberFormatException e) {
                    index = 0;
                }
            }
        }

        boolean isFaceUp = faceState.equals("U");

        int customId;
        try {
            customId = Integer.parseInt(idChar);
        } catch (NumberFormatException e) {
            customId = 0;
        }
        return new Card(Suit.convertToSuit(suitChar), index, isFaceUp, customId);
    }

    /**
     * Flips the card using animation and updates its state.
     * It plays a rotation and image change animation at the same time,
     * and records the action for the game.
     */

    public void flipCardOver() {
        // Rotate the card
        RotateTransition rotation = rotateCard(this);
        PauseTransition imageChange = changeImage();

        ParallelTransition parallelTransition = new ParallelTransition(rotation, imageChange);
        parallelTransition.setOnFinished(e -> this.setScaleX(1));
        parallelTransition.play();

        // Update state
        Recording.createGameAction(ActionType.CARD_FLIP, this);
    }

    /**
     * A getter to check if a card is face up
     *
     * @return true if the card is face up, false if face down
     */

    public boolean isFaceUp() {
        return isFaceUp;
    }

    /**
     * Change a card's flipped state without affecting the UI
     * Used for test-only logic where there is no UI
     */

    public void flipStateOnly() {
        isFaceUp = !isFaceUp;
    }

    /**
     * Set the image for the back of the card
     *
     * @param backImage the Image to display on the back
     */

    public void setBackImage(Image backImage) {
        this.backImage = backImage;
    }

    /**
     * Set the image to display on the front of the card
     *
     * @param frontImage the Image to display on the front
     */

    public void setFrontImage(Image frontImage) {
        this.frontImage = frontImage;
    }

    /**
     * Creates a short delay, then changes the card image to the other side.
     * This helps the flip animation look smooth by swapping the image halfway through.
     *
     * @return a PauseTransition that waits and then changes the card image
     */

    private PauseTransition changeImage() {
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(e -> {
            this.setScaleX(-1);
            if (isFaceUp) {
                this.setFill(new ImagePattern(backImage));
                isFaceUp = false;
            } else {
                this.setFill(new ImagePattern(frontImage));
                isFaceUp = true;
            }
        });
        return pause;
    }

    /**
     * Creates a rotation animation that spins the card around the Y-axis.
     * This makes it look like the card is flipping.
     *
     * @param card the card to apply the rotation to
     * @return a RotateTransition that rotates the card
     */

    private RotateTransition rotateCard(Card card) {
        RotateTransition rotator = new RotateTransition(Duration.millis(500), card);
        rotator.setAxis(Rotate.Y_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(180);
        rotator.setCycleCount(1);
        rotator.setOnFinished(e -> this.setRotate(0));
        return rotator;
    }

    /**
     * Reveal a card, flipping it face up for 5 seconds before turning it back face down
     */

    public void revealCard() {
        Recording.createGameAction(ActionType.REVEAL_CARD, this);
        // Flip the card state so if it was faced up, make it face down.
        isFaceUp = !isFaceUp;

        // Check that the current thread is the application thread.
        if (Platform.isFxApplicationThread()) {
            // Rotate so that when the card is flip it will appear in correct form.
            RotateTransition rotation = rotateCard(this);

            // Change card image from back to front.
            PauseTransition imageChange = changeImage();

            // Combine transitions into a single animation.
            ParallelTransition parallelTransition = new ParallelTransition(rotation, imageChange);

            // After animation finishes, reset the card.
            parallelTransition.setOnFinished(actionEvent -> this.setRotate(0));
            parallelTransition.setOnFinished(e -> this.setScaleX(1));
            ParallelTransition pt = new ParallelTransition(rotation, imageChange);
            pt.setOnFinished(actionEvent -> this.setRotate(0));
            pt.setOnFinished(e -> this.setScaleX(1));

            // Create the 5-second delay.
            PauseTransition hideDelay = new PauseTransition(Duration.seconds(5));

            // Create the full sequence, reveal - wait 5-seconds - play th parallel animation again.
            SequentialTransition sq = new SequentialTransition(parallelTransition, hideDelay, pt);

            // Play the sequence.
            sq.play();
        }
        // Return the card back to its original state.
        isFaceUp = !isFaceUp;
    }

    /**
     * Check if two Cards are equal, ignoring object identity and UI properties
     *
     * @param object the object to compare it to
     * @return true if they represent the same card, false otherwise
     */

    @Override
    public boolean tableObjectsAreEqual(Object object) {
        if (object == null) return false;
        if (!(object instanceof Card card)) return false;
        if (card.customId != this.customId) return false;
        if (card.isFaceUp != this.isFaceUp) return false;
        if (card.suit != this.suit) return false;
        if (card.index != this.index) return false;
        return true;
    }
}
