package Game;

import Enumerations.ID;
import Enumerations.SOUND_EFFECT;
import Enumerations.TEX;
import Main.Engine;
import Main.GameOverException;
import Main.PhysicsEngine;
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
  private static final int JUMP_SPEED = 85;
  private int fallCount; // to prevent user from "slowing" fall by repeatedly tapping spacebar
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
  
  public Hero(int objId, int startLives, int startHealth, int texId, float x, float y) {
    super(objId, startLives, startHealth, texId, x, y, new Point.Float(0, 0));
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
  
  public Hero(int objId, int startLives, int startHealth, int texId) {
    this(objId, startLives, startHealth, texId, 0, 0);
  }
  
  public Hero() {
    this(-1, 1, 1, TEX.HERO, 0, 0);
  }
  
  @Override
  public boolean loseHealth(int amount) throws GameOverException {
    if(Engine.isDebugging()) TestDisplay.addTestData("Hero HP: " + getHealth());
    if(!godMode) { super.loseHealth(amount); }
    if(Engine.isDebugging()) TestDisplay.addTestData("Hero damage: " + amount + " / Hero HP: " + getHealth());
    return true;
  }
  
  @Override
  public void resetAll() {
    super.resetAll();
    godMode = false;
    setTextureId(TEX.HERO);
    resetAmmo();
    resetArmor();
    dropSecondaryWeapon();
    dropJetpack();
    doLand();
  }
  
  @Override
  public void move() {
    super.move();
    if(Math.abs(this.getY()) > 3000) { //fell off map
      die();
    }
  }
  
  /**
   * Checks for collisions with all objects in nearObjects.
   * @param nearObjects
   * @return an array list of valid id collisions
   */
  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects)  {
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Integer> toRemove = new LinkedList<>();
    
    // additional things that the hero should do with each of the collided objects
    for(Collidable c : collisions) {
      int texId = c.getTextureId();
      int objId = c.getObjectId();
      switch(texId) {
        case TEX.SWITCH: break;
        case TEX.PRI_WEAPON: break;
        case TEX.ALT_WEAPON: break;
        case TEX.HEALTH_ORB:
          Engine.setStatusMessage("Picked up a health orb.");
          if(Engine.isDebugging()) TestDisplay.addTestData("Hero HP: " + getHealth());
          addHealth(3);
          if(Engine.isDebugging()) TestDisplay.addTestData("Health orb: " + 3 + " / Hero HP: " + getHealth());
          toRemove.add(objId);
          break;
        case TEX.BOX:
        case TEX.LEVEL:
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
          }
          break;
        default: // then check for object ids to react to (like enemies)
          switch(objId) {
            case ID.ENEMY_1: // these are simple damage, from contact with enemy sprites
            case ID.ENEMY_2:
            case ID.ENEMY_3:
            case ID.CALAMITY:
              if(!wasRecentlyDamaged()) {
                if(Engine.isDebugging()) TestDisplay.addTestData("Hero hit by enemy");
                recentDamageTimer.start();
                try {
                  loseHealth((int)(Enemy.getBaseDamage()*armor));
                } catch (GameOverException ex) {
                  toRemove.add(this.getObjectId());
                }
              }
              break;
            case ID.SHELL:
              Engine.setStatusMessage("Got missles!");
              Engine.addScore(1000);
              pickupSecondaryWeapon();
              toRemove.add(objId);
              break;
            case ID.ARMOR:
              Engine.setStatusMessage("Got armor!");
              Engine.addScore(275);
              pickupArmor();
              toRemove.add(objId);
              break;
            case ID.JETPACK:
              Engine.setStatusMessage("Got jetpack!");
              Engine.addScore(250);
              pickupJetpack();
              toRemove.add(objId);
              break;
            case ID.WARP:
              toRemove.add(objId); // tag for renewal to tell Engine to call jumpToNextLevel()
              break;
            default: 
              if(new Projectile().getClass().isInstance(c)) {
                if(c.getTextureId() == TEX.ENEMY_WEAPON_1 || c.getTextureId() == TEX.ENEMY_WEAPON_2) {
                  if(!wasRecentlyDamaged()) {
                    if(Engine.isDebugging()) TestDisplay.addTestData("Hero hit by projectile");
                    recentDamageTimer.start();
                    Projectile p = (Projectile)c;
                    try {
                      loseHealth((int)(p.getDamage()*armor));
                    } catch (GameOverException ex) {
                      deathAction();
                    }
                    toRemove.add(objId);
                  }
                }
              }
              break;
          }
          break;
      }
    }
    // Move on top of object if reached the top
    if(reachedTop()) {
      x += (x < lastWallCollision.getX()) ? 20 : -20;
      setClimbing(false);
    }
    return toRemove;
  }
  
  public boolean canJump() { return !jumped; }
  public void doJump() {
    if(Engine.isSoundEnabled()) SOUND_EFFECT.JUMP.play();
    fallCount++;
    jumped = true;
    setSpeedY(JUMP_SPEED);
  }
  public boolean canDoubleJump() { return !floatJumped && jumped && hasDoubleJump; }
  public void doDoubleJump() {
    if(Engine.isSoundEnabled()) SOUND_EFFECT.JETPACK.play();
    fallCount++;
    floatJumped = true;
    setSpeedY(JUMP_SPEED);
  }
  
  public void doLand() {
    //if(Engine.isSoundEnabled()) SOUND_EFFECT.LAND.play();
    jumped = false;
    floatJumped = false;
    fallCount = 0; // reset fall count
    setClimbing(false);
  }
  
  public boolean hasSecondaryWeapon() { return hasSecondaryWeapon; }
  public void pickupSecondaryWeapon() { hasSecondaryWeapon = true; }
  public void dropSecondaryWeapon() { hasSecondaryWeapon = false; }
  
  public Projectile firePrimaryWeapon(Point.Float direction) {
    if(Engine.isSoundEnabled()) SOUND_EFFECT.LASER.play();
    Point.Float zRot = Projectile.calcRotation(new Point.Float(x, y), direction);
    flipY = (zRot.x < 0);
    float xOffset = 20; // so projectile doesn't come from the hero's chest
    return new Projectile(ID.getNewId(), TEX.PRI_WEAPON, zRot,
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
      return new Projectile(ID.getNewId(), TEX.ALT_WEAPON, zRot,
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
  public void setClimbing(boolean to) { 
    isClimbing = to;
    if(isClimbing) weight = 0;
    else weight = 1f;
  }
  
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
        setTextureId(TEX.HERO_TRANSPARENT);
      } else { // second animation sequence
        if(floatJumped) setTextureId(TEX.HERO_BACKPACK1);
        else if(standingStill()) setTextureId(TEX.HERO);
        else if(movingLeft() || movingRight()) {
          if(firstSequence) setTextureId(TEX.HERO_RUN1);
          else setTextureId(TEX.HERO_RUN2);
        }
      }
    } else {
      setTextureId(TEX.HERO_DEAD);
    }
    
    setFlipX(PhysicsEngine.gravityIsInverted());
    super.draw();
  }
  
  @Override
  protected void die() {
    try {
      super.die();
    } catch (GameOverException ex) {
      this.setSpeedX(0);
      godMode = true; // prevent further damage, so damage timer will eventually expire
    }
  }
  
  public boolean getGodMode() { return godMode; }
  public void setGodMode(boolean to) {
    godMode = to;
    if(godMode) PhysicsEngine.removeHeavy(this);
    else PhysicsEngine.addHeavy(this);
  }
  public void toggleGodMode() { setGodMode(!godMode); }
  
  /**
   * Performs whatever action is associated to the currently selected object.  If no valid object
   * is selected, will simply update status message to say so.
   * @param i the object to interact with
   */
  public void interact(Interactive i) {
    if(i != null) {
      i.doAction();
      if(i.isComplete()) Engine.setStatusMessage("Can no longer interact with that object.");
      else  Engine.setStatusMessage("Picked an interactive object"); // do something
    } else {
      Engine.setStatusMessage("Nothing around to interact with.");
    }
  }

  @Override
  protected void deathAction() {
    this.setLives(0);
  }
}
