/*
 * Deck.java
 *
 * User stories: #5, #7, #15
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.tabletop;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uk.aber.dcs.gp6.ui.Table;
import uk.aber.dcs.gp6.util.ActionType;
import uk.aber.dcs.gp6.util.Recording;
import uk.aber.dcs.gp6.util.GameOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A class representing a single deck to be displayed on the table.
 * This contains all methods and data relating exclusively to a deck of cards.
 *
 * @author William Shipp, Natalia Spence, Prem Sharma
 * @version 1
 * @see TableObject
 * @see Card
 */

public class Deck extends TableObject {
    Card drawnCard = null;
    private final List<Card> cards; // Card Storage
    private Text cardCountText;
    private static int deckCount = 0;
    public SequentialTransition shuffleAnimation = new SequentialTransition();
    private Card lastCard;

    /**
     * The main Deck constructor
     * Creates a deck and initializes it to a random state with the correct number of jokers
     */

    public Deck() {
        // Create a Deck
        super();
        addEventHandlers();
        this.cards = new ArrayList<>();
        initializeDeck();
        this.customId = createDeckId();
    }

    /**
     * A Deck constructor to create a deck containing specific cards
     *
     * @param cards the cards the deck should contain, can also be in Deck objects
     */

    public Deck(List<TableObject> cards) {
        // Create a Deck
        super();
        addEventHandlers();
        this.cards = unpackTableObjects(cards);
        initializeCardCount();
        this.customId = createDeckId();
    }

    /**
     * A Deck constructor to create a deck with specific contents and custom ID
     * Used to recreate a deck from a replay recording
     *
     * @param cards the cards the deck should contain, can also be in Deck objects
     * @param customId the custom ID of the deck
     */

    public Deck(List<TableObject> cards, int customId) {
        // Create a Deck
        super();
        addEventHandlers();
        this.cards = unpackTableObjects(cards);
        initializeCardCount();
        this.customId = customId;
    }

    /**
     * Generates a unique ID for each deck
     *
     * @return the new custom ID for the deck
     */

    private static int createDeckId() {
        return deckCount++;
    }

    /**
     * A method to tell the table to add event handlers to the deck so the user can interact with it
     */

    private void addEventHandlers() {
        if (Platform.isFxApplicationThread()) Table.addEventHandlersForSpecificTableObject(this);
    }

    /**
     * Initializer for when a deck is created, Iterates through and adds all 52 standard cards and 0-6 potential Jokers.
     * Deck is shuffled once all cards are added.
     */

    private void initializeDeck() {
        // Set up Deck
        // Create 52 standard cards
        for (int suit = 0; suit <= 3; suit++) { // 0 = Heart, 1 = Diamond, 2 = Spade, 3 = Club
            for (int index = 1; index <= 13; index++) {
                cards.add(new Card(suit, index));
            }
        }

        // Add Joker cards
        int jokerCount = GameOptions.getInstance().getJokerCount();
        for (int i = 0; i < jokerCount; i++) {
            cards.add(new Card(4, 14)); // 4, 14 = Joker
        }

        initializeCardCount();

        // Once all cards are added the deck is shuffled
        Collections.shuffle(cards);
    }

    /**
     * Initialize a card count to be displayed in the corner of the deck
     */

    private void initializeCardCount() {
        // Set the card count text
        this.cardCountText = new Text(Integer.toString(this.cards.size()));
        this.cardCountText.setFont(Font.font(this.cardCountText.getFont().getName(), FontWeight.BOLD,
                this.cardCountText.getFont().getSize()*1.5));
    }

    /**
     * Unpack a list of {@link TableObject}s into a list of {@link Card}s
     *
     * @param tableObjects a list of {@link TableObject}s to be unpacked
     * @return a list of {@link Card}s
     */

    private List<Card> unpackTableObjects(List<TableObject> tableObjects) {
        List<Card> cards = new ArrayList<>();
        for (TableObject tableObject : tableObjects) {
            if (tableObject instanceof Card card) cards.add(card);
            else if (tableObject instanceof Deck deck) cards.addAll(deck.getCards());
        }
        return cards;
    }

    /**
     * Shuffles a deck of cards, plays an animation to signify that the deck is being shuffled.
     *
     * @return true to confirm the deck has been shuffled
     */

    public boolean shuffleDeck() {
        Recording.createGameAction(ActionType.SHUFFLE_DECK, this);
        Collections.shuffle(cards); // Shuffle the deck
        if (Platform.isFxApplicationThread())  {
            Table.getInstance().displayTableObject(this);
            // shuffle animation
            if (shuffleAnimation.getStatus() != Animation.Status.RUNNING) {
                shuffleAnimation = getAnimation(3);
                shuffleAnimation.play();
            }
        }

        Recording.createGameAction(ActionType.SHUFFLE_DECK, this);
        return true;
    }

    /**
     * The animation used for when a deck is shuffled or a card is repositioned within a deck.
     *
     * @param count the number of times the animation is played in a row
     * @return shuffleAnimation a sequentialTranslation
     */

    public SequentialTransition getAnimation(int count) {
        TranslateTransition translate = new TranslateTransition(Duration.seconds(0.5), this);
        translate.setByX(20);
        translate.setByY(-20);
        translate.setAutoReverse(true);
        translate.setCycleCount(2);
        RotateTransition rotate = new RotateTransition(Duration.seconds(0.5), this);
        rotate.setByAngle(20);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(2);
        SequentialTransition shuffleAnimation = new SequentialTransition(translate, rotate);
        shuffleAnimation.setCycleCount(count);
        shuffleAnimation.setAutoReverse(true);
        return shuffleAnimation;
    }

    /**
     * Gets the card objects assigned to  a deck, returned in a list
     *
     * @return cards, a list containing all card objects
     */

    public List<Card> getCards() {
        return cards;
    }

    /**
     * Updates the location of the card count text
     */

    public void setCardCountLocation() {
        this.cardCountText.setX(this.getX() + this.getWidth() - this.cardCountText.getLayoutBounds().getWidth() -
                this.getWidth()/40);
        this.cardCountText.setY(this.getY() + this.cardCountText.getLayoutBounds().getHeight() - this.getHeight()/20);
    }

    /**
     * A getter to return the card count text
     *
     * @return the card count {@link Text} object
     */

    public Text getCardCountText() {
        return this.cardCountText;
    }

    /**
     * Creates a custom string representation of the Deck
     * This includes:
     * - All {@link Card}s in the deck in order
     * - The Deck's custom ID
     *
     * @return a string representing the Deck
     */

    @Override
    public String toString() {
        // Custom string representation of Deck
        StringBuilder bob = new StringBuilder("Deck[");
        for (Card card : cards) {
            bob.append(card.toString());
            if (!card.toString().equals(cards.getLast().toString())) bob.append(",");
        }
        bob.append("]");
        bob.append(this.customId);
        return bob.toString();
    }

    /**
     * Creates a deck from a string representation of the deck
     * Used to create decks from loaded recording data
     *
     * @param data the deck's data
     * @return a Deck object created from the given data
     */

    public static Deck loadRecordedDeckData(String data) {
        String[] parts = data.split("[\\[\\],]");
        List<TableObject> cards = new ArrayList<>();
        int id = 0;

        for (int i = 0, partsLength = parts.length; i < partsLength; i++) {
            String part = parts[i];
            part = part.trim();
            if (part.isEmpty() || part.equalsIgnoreCase("Deck")) continue;
            if (i == parts.length - 1) {
                try {
                    id = Integer.parseInt(part);
                } catch (NumberFormatException ignored) {}
            }
            Card card = Card.toCard(part);
            if (card != null) cards.add(card);
        }

        return new Deck(cards, id);
    }

    /**
     * A getter to return the top card of the deck
     * Used to know what image to display on the deck
     *
     * @return the top {@link Card} in the deck
     */

    public Card getTopCard(){
        return cards.getFirst();
    }

    /**
     * A getter to get the {@link Card} drawn from the deck
     *
     * @return the {@link Card} drawn from the deck
     */

    public Card getDrawnCard() {
        if (draggedCard != null) return draggedCard;
        return this.drawnCard;
    }

    /**
     * A setter to set the card drawn from the deck
     *
     * @param card the {@link Card} drawn from the deck
     */

    public void setDrawnCard(Card card) {
        this.drawnCard = card;
    }

    /**
     * Draw a card from the top of the deck and return it
     * If the deck only has one card afterwards, it should be replaced with the remaining card
     *
     * @return the drawn {@link Card}
     */

    public Card drawCard() {
        if (this.cards.isEmpty()) {
            Card card = lastCard;
            lastCard = null;
            return card;
        }
        this.drawnCard = this.cards.removeFirst();
        if (this.cards.size() == 1) {
            draggedCard = this.drawnCard;
            if (this.getParent() instanceof Table) {
                this.setVisible(false);
                this.cardCountText.setVisible(false);
                lastCard = this.cards.removeFirst();
                lastCard.setX(this.getX());
                lastCard.setY(this.getY());
                if (!Table.getInstance().isDealing() || Table.getInstance().isLongLayout()) {
                    Table.getInstance().getChildren().add(lastCard);
                    Table.getInstance().displayTableObject(lastCard);
                }
            }
        } else {
            this.cardCountText.setText(Integer.toString(this.getCards().size()));
            Table.getInstance().displayTableObject(this);
        }
        return this.drawnCard;
    }


    public int getsize()
    {
        return cards.size();
    }

    /**
     * Check if two Decks are equal, ignoring object identity and UI properties
     *
     * @param object the object to compare it to
     * @return true if they represent the same deck, false otherwise
     */

    @Override
    public boolean tableObjectsAreEqual(Object object) {
        if (object == null) return false;
        if (!(object instanceof Deck deck)) return false;
        if (deck.customId != this.customId) return false;
        if (deck.cards.size() != this.cards.size()) return false;
        for (int i = 0; i < this.cards.size(); i++) {
            if (!deck.cards.get(i).tableObjectsAreEqual(this.cards.get(i))) return false;
        }
        return true;
    }

    /**
     * Clone a deck and remove the first card
     * Used for reversing actions that draw a card in replays
     *
     * @return a copy of the Deck but without the first card
     */

    public Deck cloneDeckForReverse() {
        Deck temp = new Deck(new ArrayList<>(this.cards), this.customId);
        temp.cards.removeFirst();
        return temp;
    }
}