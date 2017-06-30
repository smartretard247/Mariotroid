package Main;

import Drawing.DrawLib;
import Enumerations.GAME_MODE;
import java.awt.Point;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jeezy
 */
public class Hero extends GameObject {
  private static final int MAX_SECONDARY_AMMO = 5;
  private static final int JUMP_SPEED = 70;
  public int fallCount; // to prevent user from "slowing" fall by repeatedly tapping spacebar
  private long score;
  private int lives;
  private int health;
  private boolean jumped;
  private boolean hasDoubleJump;
  private boolean doubleJumped;
  private boolean hasSecondaryWeapon;
  private int secondaryAmmoCount;
  private boolean isClimbing;
  private Collidable lastWallCollision;
  
  public Hero(int startLives, long startScore, int startHealth, int texId, double x, double y) {
    super(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
    lives = startLives;
    score = startScore;
    health = startHealth;
    fallCount = 0;
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
    jumped = false;
    hasDoubleJump = false;
    doubleJumped = false;
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
      resetPosition();
      //setSpeed(0, -10);
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
  }
  
  @Override
  public List<Collidable> move(Map<Integer,Collidable> nearObjects)  {
    if(!isClimbing) setSpeedY(speedY - PhysicsEngine.GRAVITY);
    
    List<Collidable> collisions = super.move(nearObjects);
    if(this.getY() < -3000) { //fell off map
      try {
        loseHealth(10);
      } catch (GameOverException ex) {
        Engine.gameMode = GAME_MODE.GAME_OVER;
      }
    }
    
    // additional things that the hero should do with each of the collided objects
    for(Collidable c : collisions) {
      System.out.println("Collision, source object coord/speed: " + x + ", " + y + " / " + speedX + ", " + speedY);
      int id = c.getTextureId();
      switch(id) {
      case DrawLib.TEX_LEVEL:
        if(speedY < 0 && speedX == 0) { // falling straight down
          y = c.getTop() + height/2 + 1;
          speedY = 0;
          doLand();
        } else if(speedY < 0 && speedX > 0) { // falling right and down
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
            x = c.getLeft() - width/2 - 1;
            lastWallCollision = c;
          } else {
            y = c.getTop() + height/2 + 1;
            speedY = 0;
            doLand();
          }
        } else if(speedY < 0 && speedX < 0) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            x = c.getRight() + width/2 + 1;
            lastWallCollision = c;
          } else {
            y = c.getTop() + height/2 + 1;
            speedY = 0;
            doLand();
          }
        } else if(speedY == 0 && speedX < 0) { // moving left
          x = c.getRight() + width/2 + 1;
          lastWallCollision = c;
        } else if(speedY == 0 && speedX > 0) { // moving right
          x = c.getLeft() - width/2 - 1;
          lastWallCollision = c;
        } else if(speedY > 0 && speedX < 0) { // flying upward and to the left
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getBottom() - getTop())) {
            x = c.getRight() + width/2 + 1;
            lastWallCollision = c;
          } else {
            y = c.getBottom() - height/2 - 1;
            speedY = 0;
          }
        } else if(speedY > 0 && speedX > 0) { // flying upward and to the right
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
            x = c.getLeft() - width/2 - 1;
            lastWallCollision = c;
          } else {
            y = c.getBottom() - height/2 - 1;
            speedY = 0;
          }
        } else if(speedY > 0 && speedX == 0) { // flying straight upward
          y = c.getBottom() - height/2 - 1;
          speedY = 0;
        } else if(speedY == 0 && speedX == 0) { // not moving, must be a different object
          System.out.println("Hero not moving, source must be a different object");
        }
        //lastCollidedObject = c;
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
    // Move on top of object if reached the top
    if(reachedTop()){
      if(x < lastWallCollision.getX()){
        x += 20;
      }else{
        x -= 20;
      }
      isClimbing = false;
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
  }
  public boolean didLand() {
    return !jumped && !doubleJumped;
  }
  public void doLand() {
    jumped = false;
    doubleJumped = false;
    fallCount = 0; // reset fall count, see fallCount in Engine for definition
    isClimbing = false;
    this.setTextureId(DrawLib.TEX_HERO);
    
    this.setTextureId(DrawLib.TEX_HERO);
  }
  
  public boolean hasSecondaryWeapon() { return hasSecondaryWeapon; }
  public void pickupSecondaryWeapon() { hasSecondaryWeapon = true; }
  public void dropSecondaryWeapon() { hasSecondaryWeapon = false; }
  
  public Projectile firePrimaryWeapon(Point direction) {
    int zRot = Projectile.calcRotation(new Point((int)x, (int)y), direction);
    double xOffset = 80; // so projectile doesn't come from the hero's chest
    return new Projectile(DrawLib.TEX_SHELL, zRot,
            (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
            getY(), isFlippedOnY()); //fire primary
  }
  public Projectile fireSecondaryWeapon(Point direction) {
    if(hasSecondaryWeapon && secondaryAmmoCount > 0) {
      int zRot = Projectile.calcRotation(new Point((int)x, (int)y), direction);
      double xOffset = 120; // so projectile doesn't come from the hero's chest
      if(--secondaryAmmoCount == 0) hasSecondaryWeapon = false;
      return new Projectile(DrawLib.TEX_ALT_WEAPON, zRot,
            (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
            getY(), isFlippedOnY()); //fire
    }
    return null;
  }
  public int getAmmoCount() { return secondaryAmmoCount; }
  
  public void resetAmmo() {
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
  }
  
  public void pickupJetpack() { hasDoubleJump = true; };
  public void dropJetpack() { hasDoubleJump = false; };
  
  public boolean canClimb() {
    return ((getRight() + 1) == lastWallCollision.getLeft() || (getLeft() - 1) == lastWallCollision.getRight()) &&
            (getTop() > lastWallCollision.getBottom() && getBottom() < lastWallCollision.getTop());
  }
  
  private boolean reachedTop() {
    return isClimbing && getBottom() > lastWallCollision.getTop();
  }
  
  public boolean isClimbing() { return isClimbing; }
  public void setClimbing(boolean to) { isClimbing = to; }
}
