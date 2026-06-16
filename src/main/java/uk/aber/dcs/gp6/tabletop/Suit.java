/*
 * Suit.java
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.tabletop;
import java.util.Objects;

/**
 * An enum representing the suit of a card.
 * This also provides string representations of each suit.
 *
 * @author Natalia Spence, William Shipp
 * @version 1
 */

public enum Suit {
    // Long ago, the four suits lived in harmony. Then everything changed when a new suit, forged in the fires,
    // to control them all: Joker. And Joker had only one thing to say - "Why so serious?"
    HEART("H"),
    DIAMOND("D"),
    SPADE("S"),
    CLUB("C"),
    JOKER("J");

    private final String shortHand;

    /**
     * The Suit constructor
     * Allows the enum values to have a shorthand representation
     *
     * @param shortHand a shorthand string representation of the suit
     */

    Suit(String shortHand) {
        this.shortHand = shortHand;
    }

    /**
     * Creates a custom string representation of the suit from its shorthand
     *
     * @return the shorthand string representation of the suit
     */

    @Override
    public String toString() {
        return this.shortHand;
    }

    /**
     * Converts a shorthand into a suit
     *
     * @param shortHand the shorthand to check
     * @return the suit represented by the shorthand
     */

    public static Suit convertToSuit(String shortHand) {
        if (Objects.equals(shortHand, "H")) return HEART;
        if (Objects.equals(shortHand, "D")) return DIAMOND;
        if (Objects.equals(shortHand, "S")) return SPADE;
        if (Objects.equals(shortHand, "C")) return CLUB;
        return JOKER;
    }
}
