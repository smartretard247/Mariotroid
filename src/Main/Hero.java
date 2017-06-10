package Main;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jeezy
 */
public class Hero extends GameObject {
  public Hero(int startLives, long startScore) {
    lives = startLives;
    score = startScore;
  }
  public Hero() {
    this(3,0);
  }
  
  private long score;
  private int lives;
  private int health = 10;
  
  public long getScore() { return score; }
  public void resetScore() { score = 0; }
  public void addScore(int points) { score += points; }
  
  public int getLives() { return lives; }
  public void setLives(int to) { lives = to; }
  public void resetLives() { lives = 3; }
  public void addLive() { ++lives; }
  private void die() throws GameOverException {
    if(--lives <= 0) throw new GameOverException();
  }
  
  public int getHealth() { return health; }
  public void setHealth(int to) { health = to; }
  private void resetHealth() { health = 10; }
  public void loseHealth(int amount) throws GameOverException {
    health -= amount;
    if(health <= 0) {
      die();
      resetHealth();
    }
  }
  
  @Override
  public void resetAll() {
    super.resetAll();
    this.resetScore();
    this.resetHealth();
    this.resetLives();
  }
}
