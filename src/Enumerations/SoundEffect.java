package Enumerations;

import java.net.URL;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
   
public enum SoundEffect {
  SHOOT("/res/sound/shoot.wav"),
  JETPACK("/res/sound/jetpack.wav"),
  LAND("/res/sound/land.wav");
   
  public static enum Volume { MUTE, LOW, MEDIUM, HIGH }
  public static Volume volume = Volume.LOW;
  private Clip clip;
   
  SoundEffect(String soundFileName) {
    try {
      URL url = getClass().getResource(soundFileName);
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
      clip = AudioSystem.getClip();
      clip.open(audioInputStream);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      System.out.println("Cannot load sound file: " + soundFileName);
    }
  }
  
  public void play() {
    if (volume != Volume.MUTE) {
      if (clip.isRunning())
        clip.stop();   // stop the player if it is still running
      clip.setFramePosition(0); // rewind to the beginning
      clip.start();     // start playing
    }
  }
   
  public static void init() {
    values(); // calls the constructor for all the elements
  }
}
