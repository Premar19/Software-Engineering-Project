/*
 * Main.java
 *
 * Copyright (c) 2025 Aberystwyth University.
 * All rights reserved
 */

package uk.aber.dcs.gp6;

import javafx.application.Platform;
import uk.aber.dcs.gp6.ui.Replay;
import uk.aber.dcs.gp6.ui.Screen;
import uk.aber.dcs.gp6.ui.Title;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uk.aber.dcs.gp6.util.Recording;
import java.io.IOException;

/**
 * The main class of the program
 * This class is executed to run the program
 *
 * @author Natalia Spence
 * @version 1
 */
public class Main extends Application {

    /**
     * Start the JavaFX application
     *
     * @param stage the stage for the application
     * @throws IOException if an I/O exception occurs
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Set stage style to undecorated
        // This should prevent issues with maximising and minimising the UI
        // In theory this should also prevent window resizing, but that has been left here in case I'm missing anything
        stage.initStyle(StageStyle.UNDECORATED);

        // Create the Title Screen to be displayed
        Title root = Title.getInstance();
        root.setStage(stage);

        // Create the Scene using the Pane as the root
        // The int arguments are the dimensions of the window, width and height
        Scene scene = new Scene(root, 1920, 1080);

        // On key press, send to method in display
        scene.setOnKeyPressed(keyEvent -> {
            if (scene.getRoot() instanceof Screen screen) {
                screen.keyPressed(keyEvent);
            }
        });

        // Set the window name
        stage.setTitle("Hello!");

        // Set the current scene
        stage.setScene(scene);

        // Listen for window size changes
        ChangeListener<Number> windowSizeChangeListener = (observable, oldVal, newVal) -> {
            final Parent sceneRoot = stage.getScene().getRoot();
            if (!(sceneRoot instanceof Screen)) return;
            ((Screen) sceneRoot).displayScreen();
        };
        stage.widthProperty().addListener(windowSizeChangeListener);
        stage.heightProperty().addListener(windowSizeChangeListener);

        // End any running recording threads when the window is closed
        stage.setOnCloseRequest(windowEvent -> {
            Recording.endAllRecordings();
            Replay.endReplayThread();
            Platform.exit();
        });

        // Initialize Title
        root.displayScreen();

        // Actually display the screen
        stage.show();

        // Maximise the screen
        stage.setMaximized(true);
        stage.setFullScreen(true);

        // Reposition Title
        root.displayScreen();
    }

    /**
     * The main method
     * Runs the program
     *
     * @param args any program arguments
     */

    public static void main(String[] args) {
        launch();
    }
}
