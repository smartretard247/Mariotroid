package Main;

import Drawing.DrawLib;
import Enumerations.GAME_MODE;
import java.util.Map;

/**
 *
 * @author Jeezy
 */
public class Hero extends GameObject {
  private static final int MAX_SECONDARY_AMMO = 5;
  private static final int JUMP_SPEED = 60;
  private static final int MAX_JUMP_HEIGHT = 300;
  public int fallCount; // to prevent user from "slowing" fall by repeatedly tapping spacebar
  private long score;
  private int lives;
  private int health;
  private boolean jumped;
  private boolean hasDoubleJump;
  private boolean doubleJumped;
  private boolean maxJumpExceeded;
  private boolean hasSecondaryWeapon;
  private int secondaryAmmoCount;
  private double landHeight; // for calculating jump and double jump heights
  
  public Hero(int startLives, long startScore, int startHealth, int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
    lives = startLives;
    score = startScore;
    health = startHealth;
    fallCount = 0;
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
    jumped = false;
    hasDoubleJump = false;
    doubleJumped = false;
    maxJumpExceeded = false;
    landHeight = Y;
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
    dropSecondaryWeapon();
    dropJetpack();
    doLand(); // reset jump as if on ground
    
    pickupSecondaryWeapon();
  }
  
  @Override
  public Map<Integer, Collidable> move(Map<Integer,Collidable> nearObjects)  {
    if(getY() >= getMaxJumpHeight() && !maxJumpExceeded) {
      setSpeedY(-PhysicsEngine.GRAVITY);
      maxJumpExceeded = true;
    }
    
    Map<Integer, Collidable> collisions = super.move(nearObjects);
    if(this.getY() < -3000) { //fell off map
      try {
        resetPosition();
        setSpeed(0, -10);
        die();
        resetHealth();
      } catch (GameOverException ex) {
        Engine.gameMode = GAME_MODE.GAME_OVER;
      }
    }
    
    // additional things that the hero should do with each of the collided objects
    for(Integer id : collisions.keySet()) {
      System.out.println("Collision, source object coord/speed: " + X + ", " + Y + " / " + speedX + ", " + speedY);
      switch(id) {
      case DrawLib.TEX_LEVEL:
        if (speedX < 0 && speedY == 0) {
          X = collisions.get(id).getRight() + width/2 + 1;
          speedX = 0;
        } else if (speedX > 0 && speedY == 0) {
          X = collisions.get(id).getLeft() - width/2 - 1;
          speedX = 0;
        } else if (speedY > 0) {
          Y = collisions.get(id).getBottom() - height/2 - 1;
          speedY = -10;
        } else if (speedY < 0) {
          Y = collisions.get(id).getTop() + height/2 + 1;
          speedY = 0;
          doLand();
        }
        break;
      case DrawLib.TEX_JETPACK:
        pickupJetpack();
        break;
      case DrawLib.TEX_ALT_WEAPON:
        pickupSecondaryWeapon();
        break;
      default: break;
      }
    }
    
    if(collisions.isEmpty()) {
      if(speedY < 0) PhysicsEngine.fall(this);// apply gravity
      if(speedY == 0 && speedX == 0) PhysicsEngine.fall(this);// apply gravity
    }
    
    if(getSpeedX() != 0 && didLand()) this.setTextureId(DrawLib.TEX_HERO_RUN1);
    return collisions;
  }
  
  public boolean canJump() { return !jumped; }
  public void doJump() {
    jumped = true;
    setSpeedY(JUMP_SPEED);
    //this.setTextureId(DrawLib.TEX_HERO_JUMP);
  }
  public boolean canDoubleJump() { return !doubleJumped && jumped && hasDoubleJump; }
  public void doDoubleJump() {
    doubleJumped = true;
    setSpeedY(JUMP_SPEED);
    this.setTextureId(DrawLib.TEX_HERO_BACKPACK1);
    maxJumpExceeded = false;
    landHeight = getY();
  }
  public boolean didLand() {
    return !jumped && !doubleJumped;
  }
  public void doLand() {
    jumped = false;
    doubleJumped = false;
    fallCount = 0; // reset fall count, see fallCount in Engine for definition
    this.setTextureId(DrawLib.TEX_HERO);
    maxJumpExceeded = false;
    landHeight = getY();
  }
  
  public boolean hasSecondaryWeapon() { return hasSecondaryWeapon; }
  public void pickupSecondaryWeapon() { hasSecondaryWeapon = true; }
  public void dropSecondaryWeapon() { hasSecondaryWeapon = false; }
  public Projectile firePrimaryWeapon() {
    int zRot = 0;
    double xOffset = 40;
    double speed = 40;
    //(isFlippedOnY()) ? getX()-w-xOffset : getX()+xOffset
    return new Projectile(DrawLib.TEX_SHELL, zRot,
            (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
            getY(), speed, isFlippedOnY()); //fire primary
  }
  public Projectile fireSecondaryWeapon() {
    if(hasSecondaryWeapon && secondaryAmmoCount > 0) {
      int zRot = 90;
      double speed = 50;
      if(--secondaryAmmoCount == 0) hasSecondaryWeapon = false;
      return new Projectile(DrawLib.TEX_ALT_WEAPON, zRot, getX(), getY(), speed, isFlippedOnY()); //fire
    }
    return null;
  }
  public int getAmmoCount() { return secondaryAmmoCount; }
  
  public void resetAmmo() {
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
  }
  
  public void pickupJetpack() { hasDoubleJump = true; };
  public void dropJetpack() { hasDoubleJump = false; };
  
  /**
   * Returns the maximum height the current jump (single or double) can go.
   * @return 
   */
  public double getMaxJumpHeight() {
    return landHeight + MAX_JUMP_HEIGHT;
  }
}
