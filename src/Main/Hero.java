package Main;

import Enumerations.GAME_MODE;
import Enumerations.ID;
import Enumerations.SOUND_EFFECT;
import Enumerations.TEX;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Timer;
import Test.TestDisplay;

/**
 *
 * @author Jeezy
 */
public class Hero extends Living {
  private static final int MAX_SECONDARY_AMMO = 5;
  private static final int JUMP_SPEED = 11;
  private int fallCount; // to prevent user from "slowing" fall by repeatedly tapping spacebar
  private long score;
  private boolean jumped;
  private boolean hasDoubleJump;
  private boolean floatJumped;
  private boolean hasSecondaryWeapon;
  private int secondaryAmmoCount;
  private boolean isClimbing;
  private Collidable lastWallCollision;
  private final Timer recentDamageTimer = new Timer(3000, null);
  private float armor; // armor is a MULTIPLIER, do reduce damage set to a value less than 1
  private boolean hasArmor;
  private boolean godMode = false;
  
  public Hero(int objId, int startLives, int startHealth, long startScore, int texId, float x, float y) {
    super(objId, startLives, startHealth, texId, x, y, new Point.Float(0, 0));
    score = startScore;
    fallCount = 0;
    hasSecondaryWeapon = false;
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
    jumped = false;
    hasDoubleJump = false;
    floatJumped = false;
    isClimbing = false;
    recentDamageTimer.setRepeats(false);
    armor = 1;
    hasArmor = false;
  }
  
  public Hero() {
    this(-1, 1, 1, 0, TEX.TEX_HERO, 0, 0);
  }
  
  public long getScore() { return score; }
  public void resetScore() { score = 0; }
  public void addScore(int points) { score += points; }
  
  @Override
  public boolean loseHealth(int amount) throws GameOverException {
    if(!godMode) {
      if(Engine.isDebugging()) TestDisplay.addTestData("Hero HP: " + getHealth());
      if(!super.loseHealth(amount)) { // if dead after losing health
        if(Engine.isDebugging()) TestDisplay.addTestData("Hero HP: " + getHealth());
        if(getLives() > 0) resetHealth();
        if(getLives() > 0) resetPosition();
      }
    }
    if(Engine.isDebugging()) TestDisplay.addTestData("Hero damage: " + amount + " / Hero HP: " + getHealth());
    return true;
  }
  
  @Override
  public void resetAll() {
    super.resetAll();
    godMode = false;
    setTextureId(TEX.TEX_HERO);
    resetScore();
    resetAmmo();
    resetArmor();
    dropSecondaryWeapon();
    dropJetpack();
    doLand();
    
  }
  
  public List<Collidable> processCollisions(ArrayList<Collidable> nearObjects)  {
    if(!isClimbing) setSpeedY(getSpeedY() - PhysicsEngine.getGravity());
    
    //move();
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Collidable> invalidCollisions = new LinkedList<>();
    if(Math.abs(this.getY()) > 3000) { //fell off map
      try {
        loseHealth(10);
      } catch (GameOverException ex) {
      }
    }
    
    // additional things that the hero should do with each of the collided objects
    for(Collidable c : collisions) {
      if(Engine.isDebugging()) System.out.println("Collision, source object coord/speed: " + x + ", " + y + " / " + getSpeedX() + ", " + getSpeedY());
      int texId = c.getTextureId();
      int objId = c.getObjectId();
      
      switch(texId) {
      case TEX.TEX_PRI_WEAPON: break;
      case TEX.TEX_ALT_WEAPON: break;
      case TEX.TEX_HEALTH_ORB:
        if(Engine.isDebugging()) TestDisplay.addTestData("Hero HP: " + getHealth());
        addHealth(3);
        if(Engine.isDebugging()) TestDisplay.addTestData("Health orb: " + 3 + " / Hero HP: " + getHealth());
        break;
      case TEX.TEX_FALLING_BOX:
      case TEX.TEX_FALLING_BOX_S:
      case TEX.TEX_LEVEL:
        if(movingDown()) { // falling straight down
          adjustToTopOf(c);
          setSpeedY(0);
          doLand();
        } else if(movingDownAndRight()) { // falling right and down
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToLeftOf(c);
            lastWallCollision = c;
          } else {
            adjustToTopOf(c);
            setSpeedY(0);
            doLand();
          }
        } else if(movingDownAndLeft()) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToRightOf(c);
            lastWallCollision = c;
          } else {
            adjustToTopOf(c);
            setSpeedY(0);
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
            setSpeedY(0);
            if(PhysicsEngine.gravityIsInverted()) doLand();
          }
        } else if(movingUpAndRight()) { // flying upward and to the right
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
            adjustToLeftOf(c);
            lastWallCollision = c;
          } else {
            adjustToBottomOf(c);
            setSpeedY(0);
            if(PhysicsEngine.gravityIsInverted()) doLand();
          }
        } else if(movingUp()) { // flying straight upward
          adjustToBottomOf(c);
          setSpeedY(0);
          if(PhysicsEngine.gravityIsInverted()) doLand();
        } else if(standingStill()) { // not moving, must be a different object
          adjustToBottomOf(c);
          if(Engine.isDebugging()) System.out.println("Hero not moving, source must be a different object");
        }
        break;
      default: // then check for object ids to react to (like enemies)
        switch(objId) {
          case ID.ID_ENEMY_1: // these are simple damage, from contact with enemy sprites
          case ID.ID_ENEMY_2:
          case ID.ID_ENEMY_3:
          case ID.ID_CALAMITY:
            if(!wasRecentlyDamaged()) {
              if(Engine.isDebugging()) TestDisplay.addTestData("Hero hit by enemy");
              recentDamageTimer.start();
              try {
                loseHealth((int)(Enemy.getBaseDamage()*armor));
              } catch (GameOverException ex) {
              }
            }
            break;
          case ID.ID_SHELL:
            Engine.setStatusMessage("Got missles!");
            pickupSecondaryWeapon();
            break;
          case ID.ID_ARMOR:
            Engine.setStatusMessage("Got armor!");
            pickupArmor();
            break;
          case ID.ID_JETPACK:
            Engine.setStatusMessage("Got jetpack!");
            pickupJetpack();
            break;
          default: 
            if(new Projectile().getClass().isInstance(c)) {
              if(c.getTextureId() == TEX.TEX_ENEMY_WEAPON_1 || c.getTextureId() == TEX.TEX_ENEMY_WEAPON_2) {
                if(!wasRecentlyDamaged()) {
                  if(Engine.isDebugging()) TestDisplay.addTestData("Hero hit by projectile");
                  recentDamageTimer.start();
                  Projectile p = (Projectile)c;
                  try {
                    loseHealth((int)(p.getDamage()*armor));
                  } catch (GameOverException ex) {
                    this.setLives(0);
                  }
                }
              } else {
                invalidCollisions.add(c);
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
    collisions.removeAll(invalidCollisions);
    return collisions;
  }
  
  public boolean canJump() { return !jumped; }
  public void doJump() {
    if(Engine.isSoundEnabled()) SOUND_EFFECT.JUMP.play();
    ++fallCount;
    jumped = true;
    setSpeedY(JUMP_SPEED*PhysicsEngine.getGravity());
  }
  public boolean canDoubleJump() { return !floatJumped && jumped && hasDoubleJump; }
  public void doDoubleJump() {
    if(Engine.isSoundEnabled()) SOUND_EFFECT.JETPACK.play();
    ++fallCount;
    floatJumped = true;
    setSpeedY(JUMP_SPEED*PhysicsEngine.getGravity());
  }
  
  public void doLand() {
    //if(Engine.isSoundEnabled()) SOUND_EFFECT.LAND.play();
    jumped = false;
    floatJumped = false;
    fallCount = 0; // reset fall count
    isClimbing = false;
  }
  
  public boolean hasSecondaryWeapon() { return hasSecondaryWeapon; }
  public void pickupSecondaryWeapon() { hasSecondaryWeapon = true; }
  public void dropSecondaryWeapon() { hasSecondaryWeapon = false; }
  
  public Projectile firePrimaryWeapon(Point.Float direction) {
    if(Engine.isSoundEnabled()) SOUND_EFFECT.LASER.play();
    Point.Float zRot = Projectile.calcRotation(new Point.Float(x, y), direction);
    flipY = (zRot.x < 0);
    float xOffset = 20; // so projectile doesn't come from the hero's chest
    return new Projectile(ID.getNewId(), TEX.TEX_PRI_WEAPON, zRot,
            (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
            getY(), 1); //fire primary, 1 damage
  }
  public Projectile fireSecondaryWeapon(Point.Float direction) {
    if(Engine.isSoundEnabled() && hasSecondaryWeapon) SOUND_EFFECT.GUN.play();
    if(hasSecondaryWeapon && secondaryAmmoCount > 0) {
      Point.Float zRot = Projectile.calcRotation(new Point.Float(x, y), direction);
      flipY = (zRot.x < 0);
      float xOffset = 20; // so projectile doesn't come from the hero's chest
      if(--secondaryAmmoCount == 0) hasSecondaryWeapon = false;
      return new Projectile(ID.getNewId(), TEX.TEX_ALT_WEAPON, zRot,
              (isFlippedOnY()) ? getX()-xOffset : getX()+xOffset, // fire in opposite direction if flipped
              getY(), 5); //fire, 5 damage
    }
    return null;
  }
  public int getAmmoCount() { return secondaryAmmoCount; }
  
  public void resetAmmo() {
    secondaryAmmoCount = MAX_SECONDARY_AMMO;
  }
  
  public void resetArmor() { armor = 1; }
  
  public void pickupJetpack() { hasDoubleJump = true; };
  public void dropJetpack() { hasDoubleJump = false; };
  
  public boolean canClimb() {
    if(!godMode) {
      if(lastWallCollision != null) {
        return ((getRight() + 1) == lastWallCollision.getLeft() || (getLeft() - 1) == lastWallCollision.getRight()) &&
                (getTop() > lastWallCollision.getBottom() && getBottom() < lastWallCollision.getTop());
      } else {
        return false;
      }
    } else {
      return true;
    }
  }
  
  private boolean reachedTop() {
    if(!godMode) {
      return isClimbing && getBottom() > lastWallCollision.getTop();
    } else {
      return false;
    }
  }
  
  public boolean isClimbing() { return isClimbing; }
  public void setClimbing(boolean to) { isClimbing = to; }
  
  public boolean wasRecentlyDamaged() { return recentDamageTimer.isRunning(); }
  
  public void pickupArmor() { armor = 0.5f; hasArmor = true; } // 50% reduction in damage
  public boolean hasArmor() { return hasArmor; }
  
  @Override
  public void draw() {
    int animationLengthInFrames = (isSprinting()) ? 8 : 16;
    boolean firstSequence = Engine.getFrameNumber() % animationLengthInFrames < animationLengthInFrames/2;
    
    // set texture id then call standard draw function
    if(getLives() > 0) {
      if(firstSequence && wasRecentlyDamaged() && !godMode) {
        setTextureId(TEX.TEX_HERO_TRANSPARENT);
      } else { // second animation sequence
        if(floatJumped) setTextureId(TEX.TEX_HERO_BACKPACK1);
        else if(standingStill()) setTextureId(TEX.TEX_HERO);
        else if(movingLeft() || movingRight()) {
          if(firstSequence) setTextureId(TEX.TEX_HERO_RUN1);
          else setTextureId(TEX.TEX_HERO_RUN2);
        }
      }
    }
    
    setFlipX(PhysicsEngine.getGravity() < 0);
    super.draw();
  }
  
  @Override
  protected void die() {
    try {
      super.die();
    } catch (GameOverException ex) {
      this.setSpeedX(0);
      godMode = true;
      setTextureId(TEX.TEX_HERO_DEAD);
      Engine.setGameMode(GAME_MODE.DYING);
    }
  }
  
  public boolean getGodMode() { return godMode; }
  public void setGodMode(boolean to) { godMode = to; }
  public void toggleGodMode() { godMode = !godMode; }
  
  /**
   * Performs whatever action is associated to the currently selected object.  If no valid object
   * is selected, will simply update status message to say so.
   * @param i the object to interact with
   */
  public void interact(Interactive i) {
    if(i != null) {
      i.doAction();
      Engine.setStatusMessage("Picked an interactive object"); // do something
    } else {
      Engine.setStatusMessage("Nothing around to interact with.");
    }
  }
}
