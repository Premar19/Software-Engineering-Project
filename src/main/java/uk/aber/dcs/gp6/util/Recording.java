/*
 * Recording.java
 * User Stories: #12, #15
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.util;

import uk.aber.dcs.gp6.tabletop.TableObject;
import uk.aber.dcs.gp6.ui.Table;
import javafx.application.Platform;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class designed to handle recording and loading replays and dealing layouts
 * This class inherits from runnable as it is designed to have its main execution occur in a separate thread
 * alongside the main JavaFX application
 *
 * @author Natalia Spence
 * @version 1
 * @see GameAction
 * @see ActionType
 * @see uk.aber.dcs.gp6.ui.Replay
 */

public class Recording implements Runnable {
    private static boolean recordingLayout = false;
    private static boolean recordingGame = false;
    private static final BlockingQueue<GameAction> actionQueue = new LinkedBlockingQueue<>();
    private static final List<GameAction> recordedLayoutActions = new ArrayList<>();
    private static final List<GameAction> recordedGameActions = new ArrayList<>();
    private static final List<GameAction> loadedGameActions = new ArrayList<>();

    /**
     * The Recording constructor
     * Private to ensure it is only ever created by the static {@link #startRecordingThread()} method
     */

    private Recording() {
        // Create a recording runnable
        super();
    }

    /**
     * Clears the recorded actions at the end of a recording to prepare for the next
     *
     * @param toClear the recording type to clear
     */

    public static void clearRecordedActions(ActionType toClear) {
        if (toClear == ActionType.RECORD_GAME) recordedGameActions.clear();
        if (toClear == ActionType.RECORD_LAYOUT) recordedLayoutActions.clear();
    }

    /**
     * Gets the loaded {@link GameAction}s for a replay
     *
     * @return the loaded {@link GameAction}s
     */

    public static List<GameAction> getLoadedGameActions() {
        return new ArrayList<>(loadedGameActions);
    }

    /**
     * The run method controls the recording process
     */

    @Override
    public void run() {
        // The main recording thread
        boolean endPrevious = false;
        // Runs for as long as any recording is being done
        while (recordingLayout || recordingGame) {
            try {
                // Take actions from the action queue
                // This waits for something to be added then takes it
                // Since this uses a LinkedBlockingQueue and waits for items to be added, this is thread-safe
                GameAction action = actionQueue.take();
                // If the GameAction signals to end a recording type, end it
                if (action.actionType == ActionType.END_RECORDING_GAME) {
                    recordingGame = false;
                    // Get the main UI thread to show the save dialog
                    try {
                        Platform.runLater(() -> Table.getInstance().showSaveDialogBox("Would you like to save this recording?"));
                    } catch (IllegalStateException ignored) {}
                } else if (action.actionType == ActionType.END_RECORDING_LAYOUT) {
                    recordingLayout = false;
                    // Get the main UI thread to show the save dialog
                    try {
                        Platform.runLater(() -> Table.getInstance().showSaveDialogBox("Would you like to save this dealing layout?"));
                    } catch (IllegalStateException ignored) {}
                } else {
                    // If the recording explicitly ends an action that otherwise could be merged, remember that
                    if (action.actionType == ActionType.END_ACTION) {
                        endPrevious = true;
                    } else {
                        // Record the action, ending the previous one first if required
                        recordAction(action, endPrevious);
                        endPrevious = false;
                    }
                }
            } catch (InterruptedException e) {
                // This is only ever thrown if the thread is interrupted whilst waiting for `take()`
                // If this ever occurs, something has gone horribly wrong
                System.err.println("Failed to record an action :(");
            }
        }
        // All recording is over, thread should exit neatly
    }

    /**
     * Record a {@link GameAction}
     *
     * @param action the {@link GameAction} to record
     * @param endPrevious whether this should be entirely separate from the previous GameAction
     */

    private void recordAction(GameAction action, boolean endPrevious) {
        // Record in the relevant action lists
        if (recordingGame) {
            // If the previous action exists and has not explicitly been ended, check to merge
            if (!endPrevious && !recordedGameActions.isEmpty()) {
                // Get the previous action
                GameAction previousAction = recordedGameActions.getLast();
                // If the previous action is of the same type and TableObject and should be one continuous
                // action, then merge them
                if (action.actionType.shouldMerge() &&
                        previousAction.actionType == action.actionType &&
                        previousAction.actionObject == action.actionObject &&
                        (previousAction.actionType != ActionType.ROTATE ||
                         previousAction.actionArguments.get("direction").doubleValue() ==
                                 action.actionArguments.get("direction").doubleValue())) {
                    // Remove previous action from recorded actions
                    recordedGameActions.removeLast();
                    // Add new merged action
                    recordedGameActions.add(mergeActions(previousAction, action));
                } else if (recordedGameActions.size() > 1 &&
                        recordedGameActions.get(recordedGameActions.size() - 2).actionType == ActionType.CARD_DRAG &&
                        action.actionType == ActionType.PLACE_ON_DECK &&
                        action.actionObject == recordedGameActions.get(recordedGameActions.size() - 2).actionObject) {
                    recordedGameActions.remove(recordedGameActions.size() - 2);
                    recordedGameActions.add(action);
                } else recordedGameActions.add(action);
            } else recordedGameActions.add(action);
        }
        if (recordingLayout) {
            // Ensure that the action should be recorded for this dealing layout
            if (action.actionType != ActionType.DRAW_CARD && action.actionType != ActionType.CARD_DRAG) return;
            if (recordedLayoutActions.isEmpty() && action.actionType != ActionType.DRAW_CARD) return;
            if (!recordedLayoutActions.isEmpty()) {
                // Get the previous action
                GameAction previousAction = recordedLayoutActions.getLast();
                // Ensure card drags are part of card draws
                if (action.getActionType() == ActionType.CARD_DRAG &&
                    previousAction.actionType != ActionType.DRAW_CARD) return;
                // Record the action
                recordedLayoutActions.add(action);
            } else recordedLayoutActions.add(action);
        }
    }

    /**
     * Merge two {@link GameAction}s into a single one
     * Especially useful for recording card movement to prevent there being another action for each pixel the card moved
     *
     * @param first the first {@link GameAction}
     * @param second the second {@link GameAction}
     * @return a new {@link GameAction} representing the two previous actions combined
     */

    private GameAction mergeActions(GameAction first, GameAction second) {
        // Merge two game actions together

        // Do not try to merge two unrelated actions
        if (first.actionType != second.actionType || first.actionObject != second.actionObject) {
            // If this ever occurs, something has gone horribly wrong
            throw new IllegalArgumentException("Actions must be of the same type and TableObject to be merged");
        }

        Map<String, Double> newActionArgs = new HashMap<>(first.actionArguments);

        // Merge the arguments
        switch (first.actionType) {
            case ROTATE -> {
                // Replace the destination coordinates with the final ones
                newActionArgs.replace("rotation", second.actionArguments.get("rotation"));
                newActionArgs.replace("direction", second.actionArguments.get("direction"));
            }
            case CARD_DRAG -> {
                // Replace the destination coordinates with the final ones
                newActionArgs.replace("xCoord", second.actionArguments.get("xCoord"));
                newActionArgs.replace("yCoord", second.actionArguments.get("yCoord"));
            }
        }

        // Create and return an action of the same type and TableObject but with the new arguments
        return new GameAction(first.actionType, first.actionObject, newActionArgs);
    }

    /**
     * Create a {@link GameAction} from an {@link ActionType} and a {@link TableObject}
     * This will create all required {@link GameAction}s for a specified action, even if multiple are needed
     *
     * @param actionType the {@link ActionType} of the action
     * @param tableObject the {@link TableObject} the actions refers to
     * @return a {@link GameAction} of the specified type, created from the provided object
     */

    public static boolean createGameAction(ActionType actionType, TableObject tableObject) {
        // Records a game action of the specified type from the specified TableObject
        // Call this from elsewhere to record a game action

        // If not recording, do not record anything
        if (!recordingGame && !recordingLayout) return false;

        // Create the GameAction(s)
        switch (actionType) {
            case ActionType.CARD_DRAWN -> {
                createGameActionObject(ActionType.CARD_DRAG, tableObject);
                createGameActionObject(ActionType.END_ACTION, tableObject);
            }
            case ActionType.SHUFFLE_DECK -> {
                createGameActionObject(ActionType.SHUFFLE_DECK, tableObject);
                createGameActionObject(ActionType.END_ACTION, tableObject);
            }
            default -> createGameActionObject(actionType, tableObject);
        }
        return true;
    }

    /**
     * Create a {@link GameAction} from an {@link ActionType} and a {@link TableObject} and add it to the
     * recording queue.
     *
     * @param actionType the {@link ActionType} of the action
     * @param actionObject the {@link TableObject} the actions refers to
     */

    private static void createGameActionObject(ActionType actionType, TableObject actionObject) {
        // Creates and adds a GameAction object to the recording queue

        Map<String, Double> actionArgs = new HashMap<>();

        // Create the required action arguments
        switch (actionType) {
            case ActionType.INITIAL_POS -> {
                actionArgs.put("xCoord", actionObject.getX());
                actionArgs.put("yCoord", actionObject.getY());
            }
            case ActionType.CARD_DRAG -> {
                actionArgs.put("xCoord", actionObject.getX());
                actionArgs.put("yCoord", actionObject.getY());
                actionArgs.put("startX", actionObject.getPrevX());
                actionArgs.put("startY", actionObject.getPrevY());
            }
            case ActionType.ROTATE -> {
                actionArgs.put("rotation", actionObject.getRotate());
                actionArgs.put("startRotation", actionObject.getPreviousRotation());
                actionArgs.put("direction", actionObject.getPreviousRotation() - actionObject.getRotate() > 0 ? -1.0 : 1.0);
            }
        }

        // Create the GameAction
        GameAction action = new GameAction(actionType, actionObject, actionArgs);
        // Push the action to the queue for recording
        actionQueue.add(action);
    }

    /**
     * Start recording a game
     */

    private static void startRecordingGame() {
        // Start a game recording
        if (!recordingGame && !recordingLayout) {
            recordingGame = true;
            startRecordingThread();
            if (Platform.isFxApplicationThread()) saveInitialState();
        } else recordingGame = true;
    }

    /**
     * Start recording a dealing layout
     */

    private static void startRecordingLayout() {
        // Start a layout recording
        if (!recordingGame && !recordingLayout) {
            recordingLayout = true;
            startRecordingThread();
        } else recordingLayout = true;
    }

    /**
     * Start the recording thread
     */

    private static void startRecordingThread() {
        // Create the recording thread and run it
        new Thread(new Recording()).start();
    }

    /**
     * Toggle whether the game is being recorded
     *
     * @return true if a recording is started, false if one is ended
     */

    public static boolean toggleRecordingGame() {
        // Toggle game recording on or off
        if (recordingGame) {
            createGameAction(ActionType.END_RECORDING_GAME, null);
            return false;
        } else {
            startRecordingGame();
            return true;
        }
    }

    /**
     * Toggle whether a dealing layout is being recorded
     *
     * @return true if a recording is started, false if one is ended
     */

    public static boolean toggleRecordingLayout() {
        // Toggle layout recording on or off
        if (recordingLayout) {
            createGameAction(ActionType.END_RECORDING_LAYOUT, null);
            return false;
        } else {
            startRecordingLayout();
            return true;
        }
    }

    /**
     * Save the initial state of the {@link TableObject}, including all {@link TableObject}s and their locations
     */

    private static void saveInitialState() {
        Table.getInstance().getChildren().filtered(obj -> obj instanceof TableObject).forEach(obj ->
                createGameAction(ActionType.INITIAL_POS, (TableObject) obj));
    }

    /**
     * Save the recording file
     *
     * @param file the File to save the recording to
     * @param recordingType the type of recording (game or dealing layout)
     */

    public static void saveRecordingFile(File file, ActionType recordingType) {
        // Save a recording

        // Create a string builder for the data
        StringBuilder bobTheBuilder = new StringBuilder();
        // Determine which list to save from
        // This is thread safe as the user cannot interact with the table and add to this list whilst the save
        // dialog box is open, and the save dialog box is not closed until the recording is saved or cancelled
        List<GameAction> actionList = recordingType == ActionType.RECORD_GAME ?
                recordedGameActions : recordedLayoutActions;

        // Add every action to the string to save
        while (!actionList.isEmpty()) bobTheBuilder.append(actionList.removeFirst().toString());

        // Save to the file
        try {
            // Create a new file if the selected file does not already exist
            file.createNewFile();
            // Write to the file
            Files.writeString(Paths.get(file.getAbsolutePath()), bobTheBuilder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Thrown if an I/O error occurs trying to create or write to the file
            System.err.println("Failed to save file :(");
        } catch (SecurityException e) {
            // Thrown if a SecurityManager exists and denies access to the file
            System.err.println("No access to save file :(");
        }
    }

    /**
     * Load a recording from a file
     *
     * @param file the recording file to load
     * @return true if the recording is loaded successfully, false if the file is invalid
     */

    public static boolean loadRecording(File file) {
        // This assumes the file provided exists and is a valid type, so that should be handled by the calling method

        // Read the file
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String data = br.readLine();
            if (data == null) return false;

            // Separate out each action
            String[] actions = data.split(";");

            // Load each action
            for (String s : actions) {
                // Convert the string data to a GameAction
                GameAction action = GameAction.toAction(s);
                // If the data is invalid, cancel and return
                if (action == null) {
                    loadedGameActions.clear();
                    return false;
                }
                // Add the GameAction to the loaded actions
                loadedGameActions.add(action);
            }
            return true;
        } catch (FileNotFoundException e) {
            // The calling method should prevent this
            System.err.println("File not found :(");
        } catch (IOException e) {
            // If this happens, something has gone wrong
            System.err.println("I/O Exception reading file :(");
        }
        return false;
    }

    /**
     * End all recordings
     * Used to prevent the thread continuing in the background if the program is closed while recording
     */

    public static void endAllRecordings() {
        if (recordingGame) toggleRecordingGame();
        if (recordingLayout) toggleRecordingLayout();
    }
}
