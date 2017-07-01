package Main;

import Drawing.DrawLib;

/**
 *
 * @author Jeezy
 */
public class Living extends Movable {
  private int health;
  private final int defHealth;
  private int lives;
  private final int defLives;
  
  public Living(int startLives, int startHealth, int texId, double x, double y) {
    super(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
    lives = startLives;
    defLives = startLives;
    health = startHealth;
    defHealth = startHealth;
  }
  
  public Living(int startHealth, int texId, double x, double y) {
    this(1, startHealth, texId, x, y);
  }
  
  public int getHealth() { return health; }
  public void setHealth(int to) { health = to; }
  public void resetHealth() { health = defHealth; }
  
  /**
   * Reduces health by given amount and return the living status, i.e. true means still alive.
   * @param amount
   * @return
   * @throws GameOverException 
   */
  public boolean loseHealth(int amount) throws GameOverException {
    health -= amount;
    if(health <= 0) {
      die();
      return false;
    } else {
      return true;
    }
  }
  
  public int getLives() { return lives; }
  public void setLives(int to) { lives = to; }
  public void resetLives() { lives = defLives; }
  public void addLive() { ++lives; }
  private void die() throws GameOverException { if(--lives <= 0) throw new GameOverException(); }
}
