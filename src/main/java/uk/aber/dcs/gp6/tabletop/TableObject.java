/*
 * TableObject.java
 * User Stories: #6, #8, #14
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.tabletop;
import javafx.application.Platform;
import javafx.scene.shape.Rectangle;
import uk.aber.dcs.gp6.ui.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing any TableObject (a {@link Card} or {@link Deck}).
 * This contains all methods and data that apply to any TableObject.
 *
 * @author Joe Ball, Natalia Spence
 * @version 1
 * @see Card
 * @see Deck
 */

public class TableObject extends Rectangle {
    private double anchorX;
    private double anchorY;
    private double prevX;
    private double prevY;
    private boolean dragging = false;
    private static final List<TableObject> selectedObjects = new ArrayList<>();
    private static TableObject hoveredTableObject = null;
    static Card draggedCard = null;
    protected int customId;
    private double prevRotation;

    /**
     * The TableObject constructor
     */

    public TableObject() {
        // Create a TableObject
        super();
        this.setStrokeWidth(3);
        addEventHandlers();
    }

    /**
     * Tells the {@link Table} to add event handlers to the TableObject to allow the user to interact with it
     */

    private void addEventHandlers() {
        if (Platform.isFxApplicationThread()) Table.addEventHandlersForTableObject(this);
    }

    /**
     * Get the currently selected TableObjects
     *
     * @return a list of all selected TableObjects
     */

    public static List<TableObject> getSelectedObjects() {
        return selectedObjects;
    }

    /**
     * Creates a TableObject from a string representation of the TableObject
     * This simply directs the program to the appropriate method in {@link Card} or {@link Deck}
     *
     * @param objectData the data representing the TableObject
     * @return the TableObject represented by the data
     */

    public static TableObject toTableObject(String objectData) {
        // Converts the string into a TableObject object
        // This functions as the opposite of toString()
        if (objectData.length() == 4) return Card.toCard(objectData);
        else return Deck.loadRecordedDeckData(objectData);
    }

    /**
     * A getter to check if this TableObject is being dragged
     *
     * @return true if being dragged, false otherwise
     */

    public boolean objectIsBeingDragged() {
        return this.dragging;
    }

    /**
     * A setter to mark if this TableObject is being dragged
     *
     * @param isDragging true to indicate the TableObject being dragged, false otherwise
     */

    public void setDragging(boolean isDragging) {
        this.dragging = isDragging;
    }

    /**
     * A getter to get the currently hovered TableObject
     *
     * @return the currently hovered TableObject
     */

    public static TableObject getHoveredTableObject() {
        return hoveredTableObject;
    }

    /**
     * Sets the currently hovered TableObject
     *
     * @param tableObj the currently hovered TableObject
     */

    public static void setHoveredTableObject(TableObject tableObj) {
        hoveredTableObject = tableObj;
    }

    /**
     * A getter to get the mouse's current x anchor
     * Used to determine where the mouse started for drag and drop movement
     *
     * @return the x position of the mouse anchor
     */

    public double getAnchorX() {
        return this.anchorX;
    }

    /**
     * A setter to set the mouse's x anchor
     * Used to determine where the mouse started for drag and drop movement
     *
     * @param anchorX the x anchor of the mouse
     */

    public void setAnchorX(double anchorX) {
        this.anchorX = anchorX;
    }

    /**
     * A getter to get the mouse's current y anchor
     * Used to determine where the mouse started for drag and drop movement
     *
     * @return the y position of the mouse anchor
     */

    public double getAnchorY() {
        return this.anchorY;
    }

    /**
     * A setter to set the mouse's y anchor
     * Used to determine where the mouse started for drag and drop movement
     *
     * @param anchorY the y anchor of the mouse
     */

    public void setAnchorY(double anchorY) {
        this.anchorY = anchorY;
    }

    /**
     * A getter for the card currently being dragged
     *
     * @return the Card currently being dragged
     */

    public static Card getDraggedCard() {
        return draggedCard;
    }

    /**
     * A setter for the card currently being dragged
     *
     * @param card the card currently being dragged
     */

    public static void setDraggedCard(Card card) {
        draggedCard = card;
    }

    /**
     * Gets the custom ID of the TableObject
     * Used to identify if two TableObjects are the same object during recordings when they are otherwise identical
     *
     * @return the custom ID of the TableObject
     */

    public int getCustomId() {
        return this.customId;
    }

    /**
     * Check if two TableObjects are equal, ignoring object identity and UI properties
     *
     * @param object the object to compare it to
     * @return true if they represent the same TableObject, false otherwise
     */

    public boolean tableObjectsAreEqual(Object object) {
        if (this instanceof Card) return ((Card) this).tableObjectsAreEqual(object);
        else if (this instanceof Deck) return ((Deck) this).tableObjectsAreEqual(object);
        return false;
    }

    /**
     * Gets the previous rotation of the TableObject
     * Used for recording to allow reversing a replay
     *
     * @return the previous rotation value of the TableObject
     */

    public double getPreviousRotation() {
        return this.prevRotation;
    }

    /**
     * Sets the previous rotation of the TableObject
     * Used for recording to allow reversing a replay
     *
     * @param rotation the previous rotation value of the TableObject
     */

    public void setPreviousRotation(double rotation) {
        this.prevRotation = rotation;
    }

    /**
     * Sets the previous x position of the TableObject
     * Used for recording to allow reversing a replay
     *
     * @param xCoord the previous x position of the TableObject
     */

    public void setPrevX(double xCoord) {
        this.prevX = xCoord;
    }

    /**
     * Sets the previous y position of the TableObject
     * Used for recording to allow reversing a replay
     *
     * @param yCoord the previous y position of the TableObject
     */

    public void setPrevY(double yCoord) {
        this.prevY = yCoord;
    }

    /**
     * Gets the previous x position of the TableObject
     * USed for recording to allow reversing a replay
     *
     * @return the previous x position of the TableObject
     */

    public double getPrevX() {
        return this.prevX;
    }

    /**
     * Gets the previous y position of the TableObject
     * USed for recording to allow reversing a replay
     *
     * @return the previous y position of the TableObject
     */

    public double getPrevY() {
        return this.prevY;
    }
}
