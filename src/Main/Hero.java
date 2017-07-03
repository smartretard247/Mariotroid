package Main;

import Drawing.DrawLib;
import Enumerations.GAME_MODE;
import Enumerations.ID;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

/**
 *
 * @author Jeezy
 */
public class Hero extends Living {
  private static final int MAX_SECONDARY_AMMO = 5;
  private static final int JUMP_SPEED = 70;
  public int fallCount; // to prevent user from "slowing" fall by repeatedly tapping spacebar
  private long score;
  
  private boolean jumped;
  private boolean hasDoubleJump;
  private boolean doubleJumped;
  private boolean hasSecondaryWeapon;
  private int secondaryAmmoCount;
  private boolean isClimbing;
  private Collidable lastWallCollision;
  private Timer recentDamageTimer = new Timer(3000, null);
  private double armor; // armor is a MULTIPLIER, do reduce damage set to a value less than 1
  private boolean hasArmor;
  
  public Hero(int objId, int startLives, int startHealth, long startScore, int texId, double x, double y) {
    super(objId, startLives, startHealth, texId, x, y, new Point(0, 0));
    score = startScore;
    fallCount = 0;
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
    jumped = false;
    hasDoubleJump = false;
    doubleJumped = false;
    isClimbing = false;
    recentDamageTimer.setRepeats(false);
    armor = 1;
    hasArmor = false;
  }
  
  public Hero() {
    this(-1, 1, 1, 0, DrawLib.TEX_HERO, 0, 0);
  }
  
  public long getScore() { return score; }
  public void resetScore() { score = 0; }
  public void addScore(int points) { score += points; }
  
  public boolean loseHealth(int amount) throws GameOverException {
    if(!super.loseHealth(amount)) { // if dead after losing health
      resetHealth();
      resetPosition();
    }
    return true;
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
  
  public List<Collidable> processCollisions(Map<Integer,Collidable> nearObjects)  {
    if(!isClimbing) setSpeedY(speedY - PhysicsEngine.GRAVITY);
    
    //move();
    List<Collidable> collisions = getCollisions(nearObjects);
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
      int texId = c.getTextureId();
      int objId = c.getObjectId();
      switch(texId) {
      case DrawLib.TEX_ALT_WEAPON:
          pickupSecondaryWeapon();
          break;
      case DrawLib.TEX_SHELL: break;
      case DrawLib.TEX_LEVEL:
        if(movingDown()) { // falling straight down
          adjustToTopOf(c);
          speedY = 0;
          doLand();
        } else if(movingDownAndRight()) { // falling right and down
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToLeftOf(c);
            lastWallCollision = c;
          } else {
            adjustToTopOf(c);
            speedY = 0;
            doLand();
          }
        } else if(movingDownAndLeft()) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToRightOf(c);
            lastWallCollision = c;
          } else {
            adjustToTopOf(c);
            speedY = 0;
            doLand();
          }
        } else if(movingLeft()) { // moving left
          adjustToRightOf(c);
          lastWallCollision = c;
        } else if(movingRight()) { // moving right
          adjustToLeftOf(c);
          lastWallCollision = c;
        } else if(movingUpAndLeft()) { // flying upward and to the left
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getBottom() - getTop())) {
            adjustToRightOf(c);
            lastWallCollision = c;
          } else {
            adjustToBottomOf(c);
            speedY = 0;
          }
        } else if(movingUpAndRight()) { // flying upward and to the right
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
            adjustToLeftOf(c);
            lastWallCollision = c;
          } else {
            adjustToBottomOf(c);
            speedY = 0;
          }
        } else if(movingUp()) { // flying straight upward
          adjustToBottomOf(c);
          speedY = 0;
        } else if(standingStill()) { // not moving, must be a different object
          System.out.println("Hero not moving, source must be a different object");
        }
        //lastCollidedObject = c;
        break;
      default: // then check for object ids to react to (like enemies)
        switch(objId) {
          case ID.ID_ENEMY_1:
          case ID.ID_ENEMY_2:
          case ID.ID_ENEMY_3:
            if(!wasRecentlyDamaged()) {
              recentDamageTimer.start();
              try {
                loseHealth((int)(2*armor));
              } catch (GameOverException ex) {
                Engine.gameMode = GAME_MODE.GAME_OVER;
              }
            }
            break;
          case ID.ID_CALAMITY:
            if(!wasRecentlyDamaged()) {
              recentDamageTimer.start();
              try {
                loseHealth((int)(5*armor));
              } catch (GameOverException ex) {
                Engine.gameMode = GAME_MODE.GAME_OVER;
              }
            }
            break;
          case ID.ID_ARMOR:
            pickupArmor();
            break;
          case ID.ID_JETPACK:
            pickupJetpack();
            break;
          default: 
            if(new Projectile().getClass().isInstance(c)) {
              Projectile p = (Projectile)c;
              try {
                loseHealth(p.getDamage());
              } catch (GameOverException ex) {
              }
            }
            break;
        }
        break;
      }
    }
    // Move on top of object if reached the top
    if(reachedTop()){
      x += (x < lastWallCollision.getX()) ? 20 : -20;
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
  }
  
  public boolean hasSecondaryWeapon() { return hasSecondaryWeapon; }
  public void pickupSecondaryWeapon() { hasSecondaryWeapon = true; }
  public void dropSecondaryWeapon() { hasSecondaryWeapon = false; }
  
  public Projectile firePrimaryWeapon(Point direction) {
    int zRot = Projectile.calcRotation(new Point((int)x, (int)y), direction);
    flipY = (zRot == 135 || zRot == -135 || zRot == 180);
    double xOffset = 0; // so projectile doesn't come from the hero's chest
    return new Projectile(ID.getNewId(), DrawLib.TEX_SHELL, zRot,
            (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
            getY(), 1); //fire primary, 1 damage
  }
  public Projectile fireSecondaryWeapon(Point direction) {
    if(hasSecondaryWeapon && secondaryAmmoCount > 0) {
      int zRot = Projectile.calcRotation(new Point((int)x, (int)y), direction);
      flipY = (zRot == 135 || zRot == -135 || zRot == 180);
      double xOffset = 0; // so projectile doesn't come from the hero's chest
      if(--secondaryAmmoCount == 0) hasSecondaryWeapon = false;
      return new Projectile(ID.getNewId(), DrawLib.TEX_ALT_WEAPON, zRot,
            (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
            getY(), 5); //fire, 5 damage
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
    if(lastWallCollision != null) {
      return ((getRight() + 1) == lastWallCollision.getLeft() || (getLeft() - 1) == lastWallCollision.getRight()) &&
              (getTop() > lastWallCollision.getBottom() && getBottom() < lastWallCollision.getTop());
    } else {
      return false;
    }
  }
  
  private boolean reachedTop() {
    return isClimbing && getBottom() > lastWallCollision.getTop();
  }
  
  public boolean isClimbing() { return isClimbing; }
  public void setClimbing(boolean to) { isClimbing = to; }
  
  public boolean wasRecentlyDamaged() { return recentDamageTimer.isRunning(); }
  
  public void pickupArmor() { armor = 0.5; hasArmor = true; } // 50% reduction in damage
  public boolean hasArmor() { return hasArmor; }
}
