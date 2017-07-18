package Enumerations;

import java.net.URL;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
   
public enum SOUND_EFFECT {
  GUN("/res/sound/gun.wav"),
  JETPACK("/res/sound/jetpack.wav"),
  JUMP("/res/sound/jump.wav"),
  LASER("/res/sound/laser.wav"),
  WIN("/res/sound/win.wav"),
  THEME("/res/sound/theme.wav");
   
  public static enum Volume {
    MUTE, LOW, MEDIUM, HIGH {
      @Override
      public Volume up() {
        return MUTE;
      };
    };
    
    public Volume up() {
      return values()[ordinal() + 1];
    }
  }
  
  public static Volume volume = Volume.LOW;
  private Clip clip;
   
  SOUND_EFFECT(String soundFileName) {
    try {
      URL url = getClass().getResource(soundFileName);
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
      clip = AudioSystem.getClip();
      clip.open(audioInputStream);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      System.out.println("Cannot load sound file: " + soundFileName);
    }
  }
  
  public void play(float atGain) {
    if (volume != Volume.MUTE) {
      if (clip.isRunning())
        clip.stop();   // stop the player if it is still running
      clip.setFramePosition(0); // rewind to the beginning
      FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      gain.setValue(atGain);
      clip.start();     // start playing
    }
  }
  
  public void play() {
    if (volume != Volume.MUTE) {
      if (clip.isRunning())
        clip.stop();   // stop the player if it is still running
      clip.setFramePosition(0); // rewind to the beginning
      FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      gain.setValue(getGain());
      clip.start();     // start playing
    }
  }
  
  public void playLoop(float atGain) {
    if (volume != Volume.MUTE) {
      clip.loop(Clip.LOOP_CONTINUOUSLY);
      FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      gain.setValue(atGain);
      clip.start();     // start playing
    }
  }
  
  public void playLoop() {
    if (volume != Volume.MUTE) {
      clip.loop(Clip.LOOP_CONTINUOUSLY);
      FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      gain.setValue(getGain());
      clip.start();     // start playing
    }
  }
  
  public void stop() {
    clip.stop();   // stop the player if it is still running
  }
   
  public static void init() {
    values(); // calls the constructor for all the elements
  }
  
  public static Float getGain() {
    switch(volume) {
    case LOW: return 0f;
    case MEDIUM: return 3f;
    case HIGH: return 6f;
    default: return null;
    }
  }
}
