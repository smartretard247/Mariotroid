package Main;

import Enumerations.GAME_MODE;

/**
 *
 * @author Jeezy
 */
public class Hero extends DrawableGameObject {
  private static final int MAX_SECONDARY_AMMO = 10;
  
  public Hero(int startLives, long startScore, int startHealth, int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
    lives = startLives;
    score = startScore;
    health = startHealth;
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
  }
  
  private long score;
  private int lives;
  private int health;
  private boolean hasSecondaryWeapon;
  private int secondaryAmmoCount;
  
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
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
  }
  
  @Override
  public void move(GameObject[] nearObjects)  {
    super.move(nearObjects);
    if(this.getY() < -1000) { //fell off map
      try {
        resetPosition();
        setSpeed(0, 0);
        die();
        resetHealth();
      } catch (GameOverException ex) {
        Engine.gameMode = GAME_MODE.GAME_OVER;
      }
    }
  }
  
  public void pickupSecondaryWeapon() { hasSecondaryWeapon = true; }
  public void firePrimaryWeapon() {
    //fire primary
  }
  public void fireSecondaryWeapon() {
    if(hasSecondaryWeapon && secondaryAmmoCount > 0) {
      //fire
    }
  }
}
