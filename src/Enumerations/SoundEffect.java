package Enumerations;

import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
   
public enum SoundEffect {
  SHOOT("shoot.wav");
   
  public static enum Volume {
    MUTE, LOW, MEDIUM, HIGH
  }
   
  public static Volume volume = Volume.LOW;
  private Clip clip;
   
  SoundEffect(String soundFileName) {
    try {
      URL url = getClass().getClassLoader().getResource(soundFileName);
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
   
  static void init() {
    values(); // calls the constructor for all the elements
  }
}
