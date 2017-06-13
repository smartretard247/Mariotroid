package Main;

import Drawing.DrawLib;
import Enumerations.DIRECTION;
import Enumerations.GAME_MODE;

/**
 *
 * @author Jeezy
 */
public class Hero extends GameObject {
  private static final int MAX_SECONDARY_AMMO = 10;
  private static final int JUMP_SPEED = 60;
  public int fallCount; // to prevent user from "slowing" fall by repeatedly tapping spacebar
  private long score;
  private int lives;
  private int health;
  private boolean jumped;
  private boolean doubleJumped;
  private boolean hasSecondaryWeapon;
  private int secondaryAmmoCount;
  
  public Hero(int startLives, long startScore, int startHealth, int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
    lives = startLives;
    score = startScore;
    health = startHealth;
    fallCount = 0;
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
    jumped = false;
    doubleJumped = false;
  }
  
  public long getScore() { return score; }
  public void resetScore() { score = 0; }
  public void addScore(int points) { score += points; }
  
  public int getLives() { return lives; }
  public void setLives(int to) { lives = to; }
  public void resetLives() { lives = 3; }
  public void addLive() { ++lives; }
  private void die() throws GameOverException { if(--lives <= 0) throw new GameOverException(); }
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
    resetScore();
    resetHealth();
    resetLives();
    resetAmmo();
    doLand(); // reset double jump as if on ground
  }
  
  @Override
  public DIRECTION move(Collidable[] nearObjects)  {
    DIRECTION ofCollision = super.move(nearObjects);
    if(this.getY() < -2000) { //fell off map
      try {
        resetPosition();
        setSpeed(0, 0);
        die();
        resetHealth();
      } catch (GameOverException ex) {
        Engine.gameMode = GAME_MODE.GAME_OVER;
      }
    }
    if(ofCollision == DIRECTION.BOTTOM)
      doLand();
    if(getSpeedX() != 0) this.setTextureId(DrawLib.TEX_HERO_RUN1);
    return ofCollision;
  }
  
  public boolean canJump() { return !jumped; }
  public void doJump() {
    jumped = true;
    setSpeedY(JUMP_SPEED);
  }
  public boolean canDoubleJump() { return !doubleJumped && jumped; }
  public void doDoubleJump() {
    doubleJumped = true;
    setSpeedY(JUMP_SPEED);
    this.setTextureId(DrawLib.TEX_HERO_BACKPACK1);
  }
  public boolean didLand() {
    return !jumped && !doubleJumped;
  }
  public void doLand() {
    jumped = false;
    doubleJumped = false;
    fallCount = 0; // reset fall count, see fallCount in Engine for definition
    this.setTextureId(DrawLib.TEX_HERO);
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
  
  public void resetAmmo() {
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
  }
}
