package Game;

import Drawing.DrawLib;
import Enumerations.ID;
import Enumerations.TEX;
import Main.Engine;
import Main.GameOverException;
import Main.PhysicsEngine;
import Test.TestDisplay;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Timer;

/**
 *
 * @author Jeezy
 */
public class Phantom extends Enemy implements Armed, AutoFires {
  private final Timer fireTimer = new Timer(2000, null);
  
  public Phantom(int objId, int startLives, int startHealth, int texId, float x, float y, Point.Float speed, int points) {
    super(objId, startLives, startHealth, texId, x, y, speed);
    pointsWorth = points;
    fireTimer.setRepeats(false);
    dropTex = DrawLib.generateDropTex(new int[] { TEX.HEALTH_ORB, TEX.AMMO_ORB, TEX.BIG_HEALTH_ORB, TEX.BIG_AMMO_ORB },
                                      new float[] { 0.25f, 0.5f, 0.75f, 1.0f });
  }
  
  public Phantom() {
    this(-1, 1, 1, TEX.ENEMY_BASIC, 0, 0, new Point.Float(0, 0), 0);
  }
  
  @Override
  public void move() {
    super.move();
  }
  
  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects) {
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Integer> toRemove = new LinkedList<>();
    for(Collidable c : collisions) {
      int texId = c.getTextureId();
      int objId = c.getObjectId();
      switch(texId) {
        case TEX.BOX:
        case TEX.LEVEL:
          if(movingDown()) { // falling straight down
            adjustToTopOf(c);
            setSpeedY(0);
          } else if(movingDownAndRight()) { // falling right and down
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToLeftOf(c);
              reverseSpeedX();
            } else {
              adjustToTopOf(c);
              setSpeedY(0);
            }
          } else if(movingDownAndLeft()) { // falling left and down
            if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToRightOf(c);
              reverseSpeedX();
            } else {
              adjustToTopOf(c);
              setSpeedY(0);
            }
          } else if(movingLeft()) { // moving left
            adjustToRightOf(c);
            reverseSpeedX(); // reverse direction
          } else if(movingRight()) { // moving right
            adjustToLeftOf(c);
            reverseSpeedX(); // reverse direction
          } else if(movingUpAndLeft()) { // flying upward and to the left
            if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getBottom() - getTop())) {
              adjustToRightOf(c);
              reverseSpeedX(); // reverse direction
            } else {
              adjustToBottomOf(c);
              setSpeedY(0);
            }
          } else if(movingUpAndRight()) { // flying upward and to the right
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
              adjustToLeftOf(c);
              reverseSpeedX(); // reverse direction
            } else {
              adjustToBottomOf(c);
              setSpeedY(0);
            }
          }
          break;
        default:
          if(new Projectile().getClass().isInstance(c)) {
            if(!(c.getTextureId() == TEX.ENEMY_WEAPON_1 || c.getTextureId() == TEX.ENEMY_WEAPON_2)) {
              Projectile p = (Projectile)c;
              try {
                if(Engine.isDebugging()) TestDisplay.addTestData("Boss HP: " + getHealth());
                loseHealth(p.getDamage());
                if(Engine.isDebugging()) TestDisplay.addTestData("Boss damage: " + p.getDamage() + " / Boss HP: " + getHealth());
              } catch (GameOverException ex) { // enemy died
                if(Engine.isDebugging()) TestDisplay.addTestData("Boss destroyed");
                Engine.addScore(getPointsWorth());
                deathAction();
                toRemove.add(getObjectId());
              }
              toRemove.add(objId);
            }
          }
          break;
      }
    }
    return toRemove;
  }
  
  public Projectile firePrimaryWeapon(Point.Float direction) {
    fireTimer.start();
    Point.Float zRot = Projectile.calcRotation(new Point.Float(x, y), direction);
    flipY = (zRot.x < 0);
    if(Engine.isDebugging()) TestDisplay.addTestData("Phantom fire to: (" + getX() + ", " + getY() + ")");
    return new Projectile(ID.getNewId(), TEX.ENEMY_WEAPON_1, zRot, getX(), getY(), 5); // fire primary, 5 damage
  }
  
  @Override
  public Projectile fireSecondaryWeapon(Point2D.Float direction) {
    return null;
  }
  
  @Override
  public boolean didRecentlyFire() { return fireTimer.isRunning(); }

  @Override
  public boolean closeToTarget(Hero h){ return (Math.abs(h.getX() - getX()) < 2000); }
}
