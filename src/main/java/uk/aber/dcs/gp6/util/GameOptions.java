/*
 * GameOptions.java
 * User Stories: #1, #2, #3, #4, #12, #13, #15, #16
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.util;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * A utility class for storing current options and file saving/loading
 *
 * @author Francy Sasso, Reemer Fraser, Natalia Spence, Prem Sharma
 * @version 1
 * @see uk.aber.dcs.gp6.ui.Customization
 * @see uk.aber.dcs.gp6.ui.CardFrontCustomization
 */

public class GameOptions {
    private static final GameOptions Instance = new GameOptions();
    public final FileChooser fileChooser;
    private int deckCount = 1;
    private int jokerCount = 0;
    private int cardNumber = 1;
    private Image tableBackgroundImage;

    /**
     * The GameOptions constructor, private to ensure only one instance ever exists
     */

    private GameOptions() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
    }

    /**
     * A getter to return the instance of GameOptions
     *
     * @return the GameOptions instance
     */

    public static GameOptions getInstance() {
        return Instance;
    }

    /**
     * Copy a file to a new directory
     * Used for storing user uploaded images
     *
     * @param currentDir the current directory of the file
     * @param newFileName the new name/directory of the file
     */

    public static void copyFileToDirectory(String currentDir, String newFileName) {
        if (GameOptions.getInstance().getCardNumber() == 14){
            newFileName = newFileName.substring(1);
        }

        // Create File object from the existing file path
        File sourceFile = new File(currentDir);
        String destPath = "target/classes/user-images";

        if (newFileName.startsWith("cards/")) {
            newFileName = newFileName.substring(6);
            destPath += "/cards";
        }

        File destinationDir = new File(destPath);

        if (!destinationDir.exists()) {
            try {
                Files.createDirectories(destinationDir.toPath());
            } catch (IOException e) {
                System.err.println("Error creating directory: " + e.getMessage());
            }
        }

        try {
            // Define the destination file path with the new name
            Path destinationFilePath = new File(destinationDir, newFileName).toPath();

            // Copy the file to the new directory with a new name
            Files.copy(sourceFile.toPath(), destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully to: " + destinationFilePath);
        } catch (IOException e) {
            System.err.println("Error copying file: " + e.getMessage());
        }
    }

    /**
     * Get the image for the {@link uk.aber.dcs.gp6.ui.Table} background
     *
     * @param width the width the image should be
     * @param height the height the image should be
     * @return the Image for the table background
     */

    public Image getTableBackgroundImage(double width, double height) {
        File file = new File("target/classes/user-images/table-background.png");

        if (file.exists()) {
            this.tableBackgroundImage = new Image(file.toURI().toString(), width, height, false, true);
        } else {
            InputStream imgResource = this.getClass().getResourceAsStream("/table-background.jpg");
            if (imgResource != null)
                this.tableBackgroundImage = new Image(imgResource, width, height, false, true);
            else System.err.println("Error: Default table background not found.");
        }
        return this.tableBackgroundImage;
    }

    /**
     * Set the {@link uk.aber.dcs.gp6.ui.Table} background image
     *
     * @param tableBackgroundImage the Image to use for the table background
     */

    public void setTableBackgroundImage(Image tableBackgroundImage) {
        this.tableBackgroundImage = tableBackgroundImage;
    }

    public File getRecordingDirectory(ActionType type) {
        File dir = null;
        try {
            if (type == ActionType.RECORD_GAME) {
                dir = new File("target/classes/recordings/replays");
                Files.createDirectories(dir.toPath());
            } else if (type == ActionType.RECORD_LAYOUT) {
                dir = new File("target/classes/recordings/layouts");
                Files.createDirectories(dir.toPath());
            }
        } catch (IOException e) {
            System.err.println("Failed to get path");
            return null;
        }
        return dir;
    }

    /**
     * Gets the number of decks to display
     *
     * @return the number of decks to display
     */

    public int getDeckCount() {
        return this.deckCount;
    }

    /**
     * Change the current number of decks
     *
     * @param num the value to change the deck count by
     */

    public void changeDeckCount(final int num) {
        if (this.deckCount + num > 4 || this.deckCount + num < 1) return;
        this.deckCount += num;
    }

    /**
     * Loads the image to display on the {@link uk.aber.dcs.gp6.tabletop.Card} backs from the file explorer
     *
     * @param window the current Window
     * @return the Image to display on the card backs
     */

    public Image loadCardBackFromExplorer(Window window) {
        Image cardBack = loadImageFromExplorer(window);
        if (cardBack == null) return null;
        copyFileToDirectory(getCardDir(cardBack), "BACK.png");
        return cardBack;
    }

    /**
     * Loads an image from the file explorer
     *
     * @param window the current Window
     * @return the Image to be used
     */

    public Image loadImageFromExplorer(Window window) {
        File file = fileChooser.showOpenDialog(window);
        if (file == null) return null;
        return new Image(file.toURI().toString(), window.getWidth(), window.getHeight(), false, true);
    }

    /**
     * Gets the directory of a card image
     *
     * @param image the image to get the directory of
     * @return the directory of the image as a string
     */

    public String getCardDir(Image image) {
        return image.getUrl().substring(5);
    }

    /**
     * Gets the number of jokers to be included in each deck
     *
     * @return the number of jokers
     */

    public int getJokerCount() {
        return this.jokerCount;
    }

    /**
     * Change the number of jokers in a deck
     *
     * @param num the value to change the joker count by
     */

    public void changeJokerCount(final int num) {
        if (this.jokerCount + num > 6 || this.jokerCount + num < 0) return;
        this.jokerCount += num;
    }

    /**
     * Get the current card number for {@link uk.aber.dcs.gp6.ui.CardFrontCustomization}
     *
     * @return the current card number
     */

    public int getCardNumber() {
        return this.cardNumber;
    }

    /**
     * Change the current card number for {@link uk.aber.dcs.gp6.ui.CardFrontCustomization}
     *
     * @param num the number to change the current card number by
     */

    public void changeCardNumber(final int num) {
        if (this.cardNumber + num > 14) this.cardNumber = 1;
        else if (this.cardNumber + num < 1) this.cardNumber = 14;
        else this.cardNumber += num;
    }
}
