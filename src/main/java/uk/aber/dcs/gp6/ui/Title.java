/*
 * Title.java
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6.ui;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import uk.aber.dcs.gp6.util.ActionType;
import uk.aber.dcs.gp6.util.GameOptions;

import java.io.File;
import java.util.Objects;

/**
 * A class designed to manage the title page .
 *
 * @author Natalia Spence, Prem Sharma, Francy Sasso
 * @version 1
 * @see Screen
 */

public class Title extends Screen {
    private static final Title Instance = new Title();
    private Text titleText;
    private Button startButton;
    private Button replayButton;
    private Button quitButton;
    private javafx.stage.Stage stage;

    /**
     * This method sets the stage for buttons on the page
     * @param stage Screen
     */

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * This is a superclass constructor for Screen
     */

    private Title() {
        super();
    }

    /**
     * This method initializes the Starting screen with buttons to corresponding pages
     */

    @Override
    protected void initializeScreen() {
        this.titleText = new Text("Cloud 6");
        this.startButton = new Button("Start");
        this.replayButton = new Button("Replay");
        this.quitButton = new Button("Quit");
        this.getChildren().addAll(this.titleText, this.startButton, this.replayButton, this.quitButton);
    }

    /**
     * This method displays the customization page for the background and back of cards
     */

    @Override
    public void displayScreen() {
        // Set background image
        String bgUrl = Objects.requireNonNull(getClass().getResource("/title-background.png")).toExternalForm();
        this.setStyle(
                "-fx-background-image: url('" + bgUrl + "'); " +
                        "-fx-background-size: cover;"
        );


        this.titleText.setFont(new Font("Comic Sans MS", 80));
        this.titleText.setY(this.getHeight()/4);
        this.titleText.setX(this.getWidth()/2 - (this.titleText.getLayoutBounds().getWidth()/2));

        this.startButton.setMinWidth(this.getWidth()/3);
        // this (and the identical lines below) are the lines causing the warnings when running the program
        // Something to do with changing the font of specifically a Button object causes it
        // Could possibly replace the buttons with rectangles to prevent the errors
        // Could even add images to make them fancy if doing that
        this.startButton.setFont(new Font(48));
        this.startButton.setLayoutY(this.getHeight()*2.5/8);
        this.startButton.setLayoutX(this.getWidth()/3);

        this.replayButton.setMinWidth(this.getWidth()/3);
        this.replayButton.setFont(new Font(48));
        this.replayButton.setLayoutY(this.getHeight()*4/8);
        this.replayButton.setLayoutX(this.getWidth()/3);

        this.quitButton.setMinWidth(this.getWidth()/3);
        this.quitButton.setFont(new Font(48));
        this.quitButton.setLayoutY(this.getHeight()*5.5/8);
        this.quitButton.setLayoutX(this.getWidth()/3);
    }

    /**
     * This method initializes events
     */

    @Override
    protected void initializeEvents() {
        this.startButton.setOnAction(actionEvent -> this.changeScreen(this, Customization.getInstance()));
        this.replayButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Game Recording File", "*.csgr")
            );
            fileChooser.setInitialDirectory(GameOptions.getInstance().getRecordingDirectory(ActionType.RECORD_GAME));
            File file = fileChooser.showOpenDialog(stage); // make sure stage is the JavaFX stage

            if (file != null && Replay.loadReplayFromFile(file)) {
                this.changeScreen(this, Table.getInstance());
                Replay.startReplay();
            } else {
                System.out.println("Failed to load replay file.");
                showInvalidFilePrompt();

            }

        });
        this.quitButton.setOnAction(actionEvent -> {
            ((Stage) this.getScene().getWindow()).close();
            Platform.exit();
        });
    }

    /**
     * Displays a prompt with an error message when an invalid recording file is chosen
     */

    private static void showInvalidFilePrompt() {
        Group promptBox = new Group();
        double boxWidth = getInstance().getWidth() / 3;
        double boxHeight = getInstance().getHeight() / 4;

        Rectangle box = new Rectangle(boxWidth, boxHeight);
        box.setStyle("-fx-fill: white; -fx-stroke: black; -fx-stroke-width: 2;");
        box.setArcWidth(20);
        box.setArcHeight(20);

        Text promptText = new Text("Invalid Replay File");
        promptText.setFont(new Font(28));
        promptText.setLayoutX(boxWidth / 2 - promptText.getLayoutBounds().getWidth() / 2);
        promptText.setLayoutY(boxHeight / 3);


        // "Return to Menu" button
        Text returnText = new Text("Return to Menu");
        returnText.setFont(new Font(20));
        Rectangle returnRect = new Rectangle(boxWidth / 1.2, 50);
        returnRect.setStyle("-fx-fill: lightcoral; -fx-stroke: black;");
        returnRect.setArcWidth(10);
        returnRect.setArcHeight(10);
        double returnTextX = (returnRect.getWidth() - returnText.getLayoutBounds().getWidth()) / 2;
        double returnTextY = (returnRect.getHeight() + returnText.getLayoutBounds().getHeight()) / 2 - 5;
        returnText.setLayoutX(returnTextX);
        returnText.setLayoutY(returnTextY);
        Group returnButton = new Group(returnRect, returnText);
        returnButton.setLayoutX((boxWidth / 12) );
        returnButton.setLayoutY(boxHeight * 0.65);
        returnButton.setOnMouseClicked(e -> {
            getInstance().changeScreen(getInstance(), Title.getInstance());
            getInstance().getChildren().remove(promptBox);
        });

        promptBox.getChildren().addAll(box, promptText, returnButton);
        promptBox.setLayoutX(getInstance().getWidth() / 2 - boxWidth / 2);
        promptBox.setLayoutY(getInstance().getHeight() / 2 - boxHeight / 2);
        Platform.runLater(() -> getInstance().getChildren().add(promptBox));
        promptBox.toFront();
    }

    /**
     * This method handles any key functions such as 'R' for reveal
     * @param keyEvent the KeyEvent to handle
     */

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        // All key input handled by JavaFX for the Title screen
    }

    /**
     * Gets the Table instance
     * @return the Table instance
     */

    public static Title getInstance() {
        return Instance;
    }
}
