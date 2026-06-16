/*
 * Replay.java
 * User Stories: #13, #16
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.ui;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uk.aber.dcs.gp6.tabletop.*;
import uk.aber.dcs.gp6.util.ActionType;
import uk.aber.dcs.gp6.util.GameAction;
import uk.aber.dcs.gp6.util.GameOptions;
import uk.aber.dcs.gp6.util.Recording;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static uk.aber.dcs.gp6.util.ActionType.*;

/**
 * A class designed to manage the replaying previous saved games.
 * This class inherits from runnable as it is designed to have its main execution occur in a separate thread
 * alongside the main JavaFX application
 *
 * @author Prem Sharma, Natalia Spence
 * @version 1
 * @see GameAction
 * @see ActionType
 * @see Recording
 * @see Table
 */

public class Replay implements Runnable {
    private  static List<GameAction> actions;
    private static boolean isReplaying = false;
    private int currentIndex = 0;
    private final static BlockingQueue<ActionType> actionQueue = new LinkedBlockingQueue<>();
    private static int speed = 1;
    private boolean paused = false;
    private static Deck dealingDeck;
    private static Card dealtCard;
    private static boolean isRevealing = false;
    private static Group promptBox;

    /**
     * A method to tell the table to add event handlers to deal out cards
     */

    private static final EventHandler<InputEvent> handlerBlockAllInput = event -> {
        if (!Table.getInstance().isDealing() &&
                Table.getInstance().getActionBar().getChildren().contains((Node) event.getTarget())) return;
        if (event instanceof KeyEvent keyEvent && (keyEvent.getCode() == KeyCode.TAB ||
                keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.ESCAPE)) return;
        event.consume();
    };

    /**
     * This constructor calls the superclass constructor. {@link Runnable}
     */

    protected Replay() {
        super();
    }

    /**
     * This getter method returns card movement data
     * @return card data
     */

    public static Table getInstance() {
        return Table.getInstance();
    }

    /**
     * This method reads from the saved game data file in store and loads onto table
     * @param file the file to load
     * @return status of completion
     */

    public static boolean loadReplayFromFile(File file) {
        boolean success = Recording.loadRecording(file);
        if (success) {
            actions = Recording.getLoadedGameActions();
            if (!Table.getInstance().isDealing()) setupInitialState();
            else if (actions.stream().filter(action -> action.getActionType() == DRAW_CARD)
                    .count() > 52 + GameOptions.getInstance().getJokerCount()) {
                Table.getInstance().setLongLayout(true);
                Table.getInstance().showSaveDialogBox("Recording too long. Continue anyway?");
            }
        } else {
            System.err.println("Failed to load replay file.");
        }
        return success;
    }

    /**
     * This method plays recording of the saved game
     */

    public static void startReplay() {
        if (actions == null || actions.isEmpty() || isReplaying) return;
        isReplaying = true;
        Table.getInstance().setReplayMode(true);
        Table.getInstance().displayScreen();
        getInstance().addEventFilter(InputEvent.ANY, handlerBlockAllInput);
        new Thread(new Replay()).start();
    }

    /**
     * Play a dealing layout from a {@link Deck}
     *
     * @param deck the deck to deal cards from
     */

    public static void startReplayFromDeck(Deck deck) {
        dealingDeck = deck;
        Platform.runLater(() -> Table.getInstance().removeActionBar());
        fastReplay();
        fastReplay();
        startReplay();
    }

    /**
     * This method stops the replay
     */

    private static void stopReplay() {
        if (dealingDeck != null) dealingDeck = null;
        if (Table.getInstance().isDealing()) {
            slowReplay();
            slowReplay();
        }
        isReplaying = false;
        Table.getInstance().setReplayMode(false);
        endReplayThread();
        getInstance().removeEventFilter(InputEvent.ANY, handlerBlockAllInput);
        Platform.runLater(() -> {
            if (!Table.getInstance().endDealingReplay()) {
                showReplayFinishedPrompt();
            }
        });
    }

    /**
     * This method initializes the Decks
     */

    public static void setupInitialState()
    {
        for (GameAction action : actions) {
            if (action.getActionType() == ActionType.INITIAL_POS) {
                TableObject obj = (action.getActionObject());
                obj.setX(action.getActionArguments().getOrDefault("xCoord", obj.getX()));

                obj.setY(action.getActionArguments().getOrDefault("yCoord", obj.getY()));

                getInstance().getChildren().add(obj);
                if (obj instanceof Deck d) getInstance().getChildren().add(d.getCardCountText());
                getInstance().displayTableObject(obj);
            } else break;
        }
    }

    /**
     * This method pauses the replay recording
     */

    public static void pauseReplay() {
        actionQueue.add(ActionType.PAUSE_REPLAY);
    }

    /**
     * This method plays the replay recording
     */

    public static void playReplay() {
        actionQueue.add(ActionType.PLAY_REPLAY);
    }


    /**
     * This method moves the replay recording back one step
     */

    public static void moveToPrev() {
        actionQueue.add(ActionType.PREV_ACTION);
    }

    /**
     * This method moves the replay recording forward one step
     */

    public static void moveToNext() {
        actionQueue.add(ActionType.NEXT_ACTION);
    }


    /**
     * This method slows down the replay recording
     */

    public static void slowReplay() {
        speed -= speed > 1 ? 1 : 0;
        if (!Table.getInstance().isDealing()) Table.getInstance().updateActionBarSpeedText(speed);
    }


    /**
     * This method speeds up the replay recording
     */

    public static void fastReplay() {
        speed += speed < 3 ? 1 : 0;
        if (!Table.getInstance().isDealing()) Table.getInstance().updateActionBarSpeedText(speed);
    }

    /**
     * This method controls the execution of the replay
     */

    @Override
    public void run() {
        Platform.runLater(() -> createDelay(ActionType.INITIAL_POS).play());
        boolean doAction = true;
        while (isReplaying) {
            try {
                ActionType controlSignal = actionQueue.take();
                doAction = handleControlSignal(controlSignal, doAction);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Platform.runLater(Replay::stopReplay);
    }

    /**
     * This method handles the speed, pause/play and exit controls
     */

    private boolean handleControlSignal(ActionType signal, boolean doAction) {
        switch (signal) {
            case ActionType.NEXT_ACTION -> {
                if (!doAction) return true;
                if (currentIndex == actions.size()) {
                    isReplaying = false;
                }
                if (isReplaying) {
                    Platform.runLater(() -> {
                        GameAction action = actions.get(currentIndex);
                        currentIndex++;
                        handleAction(action);

                        if (!paused) {
                            if (currentIndex != actions.size()) createDelay(action.getActionType()).play();
                            else actionQueue.add(END_REPLAY);
                        }
                    });
                }
            }
            case ActionType.PREV_ACTION -> {
                if (!doAction) return true;
                Platform.runLater(() -> {
                    reverseAction();
                    currentIndex--;
                });
            }
            case PLAY_REPLAY -> createDelay(ActionType.PLAY_REPLAY).play();
            case PAUSE_REPLAY -> {
                paused = true;
                return false;
            }
            case ActionType.END_REPLAY -> isReplaying = false;
        }
        return true;
    }

    /**
     * This method reverses the previous action
     * Used for stepping backwards through a recording
     */

    private void reverseAction() {

        if(actions.get(currentIndex-1).getActionType()==ActionType.CARD_DRAG && actions.get(currentIndex-2).getActionType()==ActionType.DRAW_CARD)
        {
            Card card = (Card) Table.getInstance().getChildren().filtered(n -> actions.get(currentIndex-1).getActionObject().tableObjectsAreEqual(n)).getFirst();
            Deck deck = (Deck) Table.getInstance().getChildren().filtered(n -> ((Deck) actions.get(currentIndex-2).getActionObject()).cloneDeckForReverse().tableObjectsAreEqual(n)).getFirst();
            deck.getCards().addFirst(card);
            deck.getCardCountText().setText(String.valueOf(deck.getCards().size()));
            Table.getInstance().getChildren().remove(card);
            Table.getInstance().displayTableObject(deck);
            currentIndex--;
        }
        else if(actions.get(currentIndex-1).getActionType()==ActionType.CARD_DRAG)
        {
            Card card=null;
            Deck deck=null;
            GameAction action;

            if (actions.get(currentIndex-1).getActionObject() instanceof Card)
                 card = (Card) Table.getInstance().getChildren().filtered(n -> actions.get(currentIndex-1).getActionObject().tableObjectsAreEqual(n)).getFirst();
            else
                deck = (Deck) Table.getInstance().getChildren().filtered(n -> actions.get(currentIndex-1).getActionObject().tableObjectsAreEqual(n)).getFirst();
            if(card != null) {
                action = actions.get(currentIndex-1);

                card.setX(action.getActionArguments().get("startX"));
                card.setY(action.getActionArguments().get("startY"));
            }
            else if (deck != null){
                action = actions.get(currentIndex-1);
                deck.setX(action.getActionArguments().get("startX"));
                deck.setY(action.getActionArguments().get("startY"));
            }

        }
        else if (actions.get(currentIndex-1).getActionType() == ROTATE)
        {
            GameAction action;
            action = actions.get(currentIndex-1);
            Double degree = action.getActionArguments().get("startRotation");
            Table.getInstance().getChildren().filtered(n -> ( actions.get(currentIndex-1).getActionObject()).tableObjectsAreEqual(n)).getFirst().setRotate(degree);
            Table.getInstance().displayTableObject(((TableObject)Table.getInstance().getChildren().filtered(n -> (actions.get(currentIndex-1).getActionObject()).tableObjectsAreEqual(n)).getFirst()));
        }
        else if (actions.get(currentIndex-1).getActionType() == CARD_FLIP)
        {
            Card card = (Card)Table.getInstance().getChildren().filtered(n -> (actions.get(currentIndex-1).getActionObject()).tableObjectsAreEqual(n)).getFirst();
            card.flipCardOver();
        }
        else if (actions.get(currentIndex-1).getActionType() == REVEAL_CARD)
        {
            Card card = (Card)Table.getInstance().getChildren().filtered(n -> (actions.get(currentIndex-1).getActionObject()).tableObjectsAreEqual(n)).getFirst();
            card.revealCard();
        }

    }

    /**
     * Create a PauseTransition that waits for the required delay then plays the next action in the recording
     *
     * @param actionType the action being done before the delay
     * @return a PauseTransition to play the next action with an appropriate delay
     */

    private PauseTransition createDelay(ActionType actionType) {
        if (paused) paused = false;
        Duration duration;
        if (actionType == ActionType.DRAW_CARD) duration = Duration.millis(0);
        else if (speed == 1) duration = Duration.millis(1000);
        else if (speed == 2) duration = Duration.millis(500);
        else if (speed == 3) duration = Duration.millis(100);
        else duration = Duration.millis(500);
        if (isRevealing) {
            duration = duration.add(Duration.seconds(6));
            isRevealing = false;
        }
        PauseTransition pt = new PauseTransition(duration);
        pt.setOnFinished(event -> actionQueue.add(ActionType.NEXT_ACTION));
        return pt;
    }

    /**
     * Handles replaying one {@link GameAction}
     *
     * @param action the {@link GameAction} to be performed
     */

    private static void handleAction(GameAction action) {
        ActionType type = action.getActionType();
        TableObject obj;
        if (Table.getInstance().isDealing()) {
            if (type == DRAW_CARD) obj = dealingDeck;
            else obj = dealtCard;
        } else obj = (TableObject) Table.getInstance().getChildren().filtered(n -> action.getActionObject().tableObjectsAreEqual(n)).getFirst();


        switch (type) {
            case DRAW_CARD -> {
                if (obj instanceof Deck deck) {
                    dealtCard = deck.drawCard();
                    if (dealtCard == null) {
                        if (Table.getInstance().isDealing()) {
                            System.err.println("Could not find card, ending replay");
                            actionQueue.add(END_REPLAY);
                        } else {
                            System.err.println("⚠ Could not find card: " + obj);
                        }
                        return;
                    }
                    Table.getInstance().getChildren().add(dealtCard);

                    // Fallback fill to visualize even if images fail
                    if (dealtCard.getFill() == null) {
                        dealtCard.setFill(javafx.scene.paint.Color.LIGHTGRAY);
                    }

                    // Sync deck count
                    if (!deck.getCards().isEmpty()) {
                        deck.getCardCountText().setText(String.valueOf(deck.getCards().size()));
                        getInstance().displayTableObject(deck);
                        deck.setDrawnCard(null);
                    }
                }
            }

            case CARD_DRAG -> {
                if (obj != null) {
                    double x = action.getActionArguments().getOrDefault("xCoord", obj.getX());
                    double y = action.getActionArguments().getOrDefault("yCoord", obj.getY());
                    obj.setX(x);
                    obj.setY(y);


                    if (!getInstance().getChildren().contains(obj)) {
                        getInstance().getChildren().add(obj);
                    }

                    getInstance().displayTableObject(obj);
                }
            }

            case CARD_FLIP -> {
                if (obj instanceof Card card) {
                    card.flipCardOver();
                    getInstance().displayTableObject(card);
                }
            }
            case ROTATE -> {
                Double degree = action.getActionArguments().get("rotation");
                double direction = action.getActionArguments().get("direction");
                Table.getInstance().getChildren().filtered(n -> action.getActionObject().tableObjectsAreEqual(n)).getFirst().setRotate(degree * direction);
            }
            case REVEAL_CARD -> {
                if(obj instanceof Card card) {
                    isRevealing = true;
                    card.revealCard();
                }
            }
            default -> System.err.println("Unhandled action: " + type);
        }

    }

    /**
     * Show the dialog box for a finished replay
     */

    private static void showReplayFinishedPrompt() {
        promptBox = new Group();

        double boxWidth = getInstance().getWidth() / 3;
        double boxHeight = getInstance().getHeight() / 4;

        Rectangle box = new Rectangle(boxWidth, boxHeight);
        box.setStyle("-fx-fill: white; -fx-stroke: black; -fx-stroke-width: 2;");
        box.setArcWidth(20);
        box.setArcHeight(20);

        Text promptText = new Text("Replay complete!");
        promptText.setFont(new Font(28));
        promptText.setLayoutX(boxWidth / 2 - promptText.getLayoutBounds().getWidth() / 2);
        promptText.setLayoutY(boxHeight / 3);

        // "Continue Playing" button
        Text continueText = new Text("Continue Playing");
        continueText.setFont(new Font(20));
        Rectangle continueRect = new Rectangle(boxWidth / 2.5, 50);
        continueRect.setStyle("-fx-fill: lightgreen; -fx-stroke: black;");
        continueRect.setArcWidth(10);
        continueRect.setArcHeight(10);
        double continueTextX = (continueRect.getWidth() - continueText.getLayoutBounds().getWidth()) / 2;
        double continueTextY = (continueRect.getHeight() + continueText.getLayoutBounds().getHeight()) / 2 - 5;
        continueText.setLayoutX(continueTextX);
        continueText.setLayoutY(continueTextY);
        Group continueButton = new Group(continueRect, continueText);
        continueButton.setLayoutX((boxWidth / 2) - continueRect.getWidth() - 10);
        continueButton.setLayoutY(boxHeight * 0.65);
        continueButton.setOnMouseClicked(e -> {
            getInstance().getChildren().remove(promptBox);
            getInstance().removeActionBar();
            promptBox = null;
        });

        // "Return to Menu" button
        Text returnText = new Text("Return to Menu");
        returnText.setFont(new Font(20));
        Rectangle returnRect = new Rectangle(boxWidth / 2.5, 50);
        returnRect.setStyle("-fx-fill: lightcoral; -fx-stroke: black;");
        returnRect.setArcWidth(10);
        returnRect.setArcHeight(10);
        double returnTextX = (returnRect.getWidth() - returnText.getLayoutBounds().getWidth()) / 2;
        double returnTextY = (returnRect.getHeight() + returnText.getLayoutBounds().getHeight()) / 2 - 5;
        returnText.setLayoutX(returnTextX);
        returnText.setLayoutY(returnTextY);
        Group returnButton = new Group(returnRect, returnText);
        returnButton.setLayoutX((boxWidth / 2) + 10);
        returnButton.setLayoutY(boxHeight * 0.65);
        returnButton.setOnMouseClicked(e -> {
            getInstance().changeScreen(getInstance(), Title.getInstance());
            getInstance().resetTable();
            promptBox = null;
        });

        promptBox.getChildren().addAll(box, promptText, continueButton, returnButton);
        promptBox.setLayoutX(getInstance().getWidth() / 2 - boxWidth / 2);
        promptBox.setLayoutY(getInstance().getHeight() / 2 - boxHeight / 2);
        Platform.runLater(() -> getInstance().getChildren().add(promptBox));
        promptBox.toFront();
    }

    /**
     * Gets the prompt box for ending replays to allow for keyboard navigation
     *
     * @return the end replay prompt box
     */

    public static Group getPromptBox() {
        return promptBox;
    }

    /**
     * End the replay thread at the end of the replay or when exiting the program
     */

    public static void endReplayThread() {
        actionQueue.add(ActionType.END_REPLAY);
    }

    /**
     * Check if a deck is selected for a dealing layout replay
     *
     * @return true if a deck is selected, otherwise false
     */

    public static boolean isDeckSelected() {
        return dealingDeck != null;
    }

    /**
     * A getter to get the current playback speed
     *
     * @return the current playback speed (1-3)
     */

    public static int getSpeed() {
        return speed;
    }
}