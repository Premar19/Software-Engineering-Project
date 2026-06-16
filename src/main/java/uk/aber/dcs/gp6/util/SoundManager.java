package uk.aber.dcs.gp6.util;
import javafx.scene.media.AudioClip;


public class SoundManager {
    private static final AudioClip cardSelectSound = new AudioClip(
            SoundManager.class.getResource("resources/ClickSoundTest.mp3").toExternalForm());

    public static void playCardSelect(){
        cardSelectSound.play();
    }

}
