/*
 * ActionType.java
 * User Stories: #12, #13, #15, #16
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.util;

import java.util.Arrays;

/**
 * An Enum representing the various types of actions that can occur
 * Actions could be caused by user input or used to control recordings or replays
 *
 * @author Natalia Spence
 * @version 1
 * @see GameAction
 * @see Recording
 * @see uk.aber.dcs.gp6.ui.Replay
 */
public enum ActionType {
    //
    // Control Signals
    //

    // Recording types

    RECORD_GAME,
    RECORD_LAYOUT,

    LOAD_GAME_RECORDING,
    LOAD_LAYOUT_RECORDING,
    LAYOUT_TOO_LONG,

    // Replay Control

    PLAY_REPLAY,
    PAUSE_REPLAY,

    // Playback types

    NEXT_ACTION,
    PREV_ACTION,
    END_REPLAY,

    // Initial table state
    INITIAL_POS,


    // End recording
    END_RECORDING_GAME,
    END_RECORDING_LAYOUT,


    // End an action that could be continuous
    END_ACTION,


    // End a card draw (so the recording can save the state of the deck before and the location of the card after)
    CARD_DRAWN,


    //
    // Game Actions
    //

    CARD_DRAG,
    CARD_FLIP,
    REVEAL_CARD,
    ROTATE,

    // Linked actions (these use a more actions immediately after to form the full game action)
    // Draw card gives one DRAW_CARD action for the deck, followed by one CARD_DRAG for the card and the
    // location the card should be placed
    DRAW_CARD,

    // Shuffling decks gives two actions in a row, one of the deck before and one of the deck after
    SHUFFLE_DECK,

    // Place on deck gives many actions in a row, one of the deck in its starting state, followed by
    // one for each card to be placed on the deck, followed by one for the deck in its final state
    PLACE_ON_DECK,

    // Reposition top gives one action for the deck before, and one for the deck after
    REPOSITION_TOP;



    private static final ActionType[] shouldMerge = {ActionType.CARD_DRAG, ActionType.ROTATE};

    /**
     * A method to determine whether an ActionType is eligible to have multiple of its
     * {@link GameAction}s merged in a recording file
     *
     * @return true if it can be merged, false otherwise
     */

    public boolean shouldMerge() {
        return Arrays.asList(shouldMerge).contains(this);
    }
}
