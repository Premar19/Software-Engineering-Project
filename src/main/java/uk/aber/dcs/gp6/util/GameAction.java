/*
 * GameAction.java
 * User Stories: #12, #13, #15, #16
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.util;

import uk.aber.dcs.gp6.tabletop.TableObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A class representing a single action taken in the game
 *
 * @author Natalia Spence, Prem Sharma
 * @version 1
 * @see ActionType
 * @see Recording
 * @see uk.aber.dcs.gp6.ui.Replay
 */
public class GameAction {
    ActionType actionType;
    TableObject actionObject;
    String actionObjectData;
    Map<String, Double> actionArguments;

    /**
     * The GameAction constructor
     * Creates a GameAction from a specified {@link ActionType}, {@link TableObject} and Map of arguments
     *
     * @param actionType the {@link ActionType} of the game action
     * @param actionObject the {@link TableObject} the action refers to
     * @param actionArgs the arguments of the action
     */

    protected GameAction(ActionType actionType, TableObject actionObject, Map<String, Double> actionArgs) {
        this.actionType = actionType;
        this.actionObject = actionObject;
        this.actionObjectData = actionObject == null ? "" : actionObject.toString();
        this.actionArguments = actionArgs;
    }

    /**
     * Provides a custom String representation of a GameAction for saving to a file
     *
     * @return a String representation of the GameAction
     */

    @Override
    public String toString() {
        // Custom string representation of GameAction
        // Useful for saving to a file and printing for debugging
        return this.actionType.toString() +
                "-" +
                this.actionObjectData +
                "-" +
                this.actionArguments.toString().replaceAll("[\\s{}]", "") +
                ";";
    }

    /**
     * A getter to get the {@link TableObject} the action refers to
     *
     * @return the {@link TableObject} the action refers to
     */

    public TableObject getActionObject() {
        return actionObject;
    }

    /**
     * A getter to get the arguments of the action
     *
     * @return the arguments of the action as a Map of String to Double
     */

    public Map<String, Double> getActionArguments() {
        return actionArguments;
    }

    /**
     * Converts a given string into a GameAction
     *
     * @param actionData the string to be converted
     * @return a GameAction represented by the String, or null if invalid
     */

    public static GameAction toAction(String actionData) {
        // Create a GameAction from save data for said action
        // Essentially the inverse of GameAction.toString(), but input here doesn't include the ";"

        // Split the data into the separate parts of a GameAction
        String[] parts = actionData.split("-");
        for (int i = 3; i < parts.length; i++) {
            parts[2] = parts[2] + "-" + parts[i];
        }

        ActionType actionType;

        // Get the ActionType
        try {
            actionType = ActionType.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            System.err.println("This save file contains invalid actions!");
            return null;
        }

        // Get the TableObject being referred to
        TableObject tableObject = TableObject.toTableObject(parts[1]);

        HashMap<String, Double> actionArgs;
        if (parts.length >= 3 && !Objects.equals(parts[2], "")) {
            // Convert the saved action arguments into a HashMap
            // Thanks to IntelliJ for this ungodly optimisation of a for loop
            actionArgs = Arrays.stream(parts[2].split(","))
                    .map(arg -> arg.split("="))
                    .collect(Collectors.toMap(
                            splitArg -> splitArg[0],
                            splitArg -> Double.parseDouble(splitArg[1]),
                            (a, b) -> b, HashMap::new));
        } else actionArgs = new HashMap<>();

        // Create and return a GameAction from this data
        return new GameAction(actionType, tableObject, actionArgs);
    }

    /**
     * A getter to get the {@link ActionType} of the GameAction
     *
     * @return the action's {@link ActionType}
     */

    public ActionType getActionType() {
        return actionType;
    }
}
