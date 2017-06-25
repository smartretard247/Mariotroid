package Main;

import Drawing.DrawLib;
import Enumerations.GAME_MODE;
import java.awt.Point;
import java.util.Map;

/**
 *
 * @author Jeezy
 */
public class Hero extends GameObject {
  private static final int MAX_SECONDARY_AMMO = 5;
  private static final int JUMP_SPEED = 70;
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
  private boolean isClimbing;
  private Collidable lastCollidedObject;
  
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
    landHeight = y;
    isClimbing = false;
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
    
    //pickupSecondaryWeapon(); // for testing secondary weapon
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
    for(Map.Entry<Integer, Collidable> e : collisions.entrySet()) {
      System.out.println("Collision, source object coord/speed: " + x + ", " + y + " / " + speedX + ", " + speedY);
      int id = e.getKey();
      Collidable c = e.getValue();
      switch(id) {
      case DrawLib.TEX_LEVEL:
        if(speedY < 0 && speedX == 0) { // falling straight down
          y = c.getTop() + height/2 + 1;
          speedY = 0;
          doLand();
        } else if(speedY < 0 && speedX > 0) { // falling right and down
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
            x = c.getLeft() - width/2 - 1;
            if(speedY != -10) speedX = 0;
            speedY = 0;
          } else {
            y = c.getTop() + height/2 + 1;
            if(speedY != -10) speedX = 0;
            speedY = 0;
            doLand();
          }
        } else if(speedY < 0 && speedX < 0) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            x = c.getRight() + width/2 + 1;
            if(speedY != -10) speedX = 0;
            speedY = 0;
          } else {
            y = c.getTop() + height/2 + 1;
            if(speedY != -10) speedX = 0;
            speedY = 0;
            doLand();
          }
        } else if(speedY == 0 && speedX < 0) { // moving left
          x = c.getRight() + width/2 + 1;
          speedX = 0;
        } else if(speedY == 0 && speedX > 0) { // moving right
          x = c.getLeft() - width/2 - 1;
          speedX = 0;
        } else if(speedY > 0 && speedX < 0) { // flying upward and to the left
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getBottom() - getTop())) {
            x = c.getRight() + width/2 + 1;
            speedX = 0;
            speedY = 0;
          } else {
            y = c.getBottom() - height/2 - 1;
            speedX = 0;
            speedY = 0;
          }
        } else if(speedY > 0 && speedX > 0) { // flying upward and to the right
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
            x = c.getLeft() - width/2 - 1;
            speedX = 0;
            speedY = 0;
          } else {
            y = c.getBottom() - height/2 - 1;
            speedX = 0;
            speedY = 0;
          }
        } else if(speedY > 0 && speedX == 0) { // flying straight upward
          y = c.getBottom() - height/2 - 1;
          speedY = 0;
        } else if(speedY == 0 && speedX == 0) { // not moving, must be a different object
          System.out.println("Hero not moving, source must be a different object");
          y += 1; //try to offset falling through the ground...
        }
        lastCollidedObject = c;
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
    
    return collisions;
  }
  
  public boolean canJump() { return !jumped; }
  public void doJump() {
    jumped = true;
    setSpeedY(JUMP_SPEED);
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
    
    this.setTextureId(DrawLib.TEX_HERO);
  }
  
  public boolean hasSecondaryWeapon() { return hasSecondaryWeapon; }
  public void pickupSecondaryWeapon() { hasSecondaryWeapon = true; }
  public void dropSecondaryWeapon() { hasSecondaryWeapon = false; }
  private int calcProjectileRotation(Point direction) {
    if(direction.x - x == 0) {
      return y < 0 ? -90 : 90; // shoot up or down when div by 0
    }
    float slope = (float) ((direction.y - y) / (direction.x - x)); // rise/run
    if(slope >= -2.0 && slope < -0.5) { return -45; // shoot down and right
    } else if(slope >= -0.5 && slope < 0.5) { return 0; // shoot straight ahead
    } else if(slope >= 0.5 && slope < 2.0) { return 45;// shoot diagonal up and right
    } else if(slope >= 2.0 || slope < -2.0) { return 90;// shoot up
    } else { return -90; // shoot down
    }
  }
  public Point calcProjectileSpeed(int rotation) {
    int maxX = 50, maxY = 50;
    switch(rotation) {
      case -90: return new Point(0, -maxY);
      case -45: return new Point((int) Math.sqrt(maxX*maxX/2), (int) -Math.sqrt(maxY*maxY/2));
      case 0: return new Point(maxX, 0);
      case 45: return new Point((int) Math.sqrt(maxX*maxX/2), (int) Math.sqrt(maxY*maxY/2));
      case 90: return new Point(0, maxY);
      default: return null;
    }
  }
  public Projectile firePrimaryWeapon(Point direction) {
    int zRot = calcProjectileRotation(direction);
    Point speed = calcProjectileSpeed(zRot);
    double xOffset = 40;
    return new Projectile(DrawLib.TEX_SHELL, zRot,
            (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
            getY(), speed.x, speed.y, isFlippedOnY()); //fire primary
  }
  public Projectile fireSecondaryWeapon(Point direction) {
    if(hasSecondaryWeapon && secondaryAmmoCount > 0) {
      int zRot = calcProjectileRotation(direction);
      Point speed = calcProjectileSpeed(zRot);
      if(--secondaryAmmoCount == 0) hasSecondaryWeapon = false;
      return new Projectile(DrawLib.TEX_ALT_WEAPON, zRot + 90,
              getX(), getY(), speed.x, speed.y, isFlippedOnY()); //fire
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
  
  public boolean canClimb() {
    return ((getRight() + 1) == lastCollidedObject.getLeft() || (getLeft() - 1) == lastCollidedObject.getRight()) &&
            !reachedTop();
  }
  
  private boolean reachedTop() {
    return !(getBottom() <= (lastCollidedObject.getTop() + 3));
  }
  
  public boolean isClimbing() { return isClimbing; }
  public void setClimbing(boolean to) { isClimbing = to; }
  
  public Collidable getLastCollision() { return this.lastCollidedObject; }
}
