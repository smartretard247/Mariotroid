package Game;

import Enumerations.ID;
import Enumerations.TEX;
import Main.Engine;
import Main.GameOverException;
import Test.TestDisplay;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Timer;

/**
 *
 * @author Jeezy
 */
public class Boss extends Enemy {
  private int minXLocation = 0; // to keep from entering the rest of the level
  private int maxXLocation = 12000;
  private final Timer fireTimer = new Timer(5000, null);
  
  public Boss(int objId, int startLives, int startHealth, int texId, float x, float y, Point.Float speed, int points) {
    super(objId, startLives, startHealth, texId, x, y, speed);
    pointsWorth = points;
    fireTimer.setRepeats(false);
    weight = 0;
  }
  
  public Boss() {
    this(-1, 1, 1, TEX.ENEMY_BASIC, 0, 0, new Point.Float(0, 0), 0);
  }
  
  @Override
  public void draw() {
    super.draw(false);
  }
  
  @Override
  public void move() {
    super.move();
    if(x < minXLocation + width/2 || x > maxXLocation - width/2) reverseSpeedX();
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
            reverseSpeedY();
          } else if(movingDownAndRight()) { // falling right and down
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToLeftOf(c);
              reverseSpeedX();
            } else {
              adjustToTopOf(c);
              reverseSpeedY();
            }
          } else if(movingDownAndLeft()) { // falling left and down
            if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
              adjustToRightOf(c);
              reverseSpeedX();
            } else {
              adjustToTopOf(c);
              reverseSpeedY();
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
            } else {
              adjustToBottomOf(c);
              reverseSpeedY();
            }
          } else if(movingUpAndRight()) { // flying upward and to the right
            if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
              adjustToLeftOf(c);
            } else {
              adjustToBottomOf(c);
              reverseSpeedY();
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
                Engine.getGameContainer().addGO(new Item(ID.getNewId(), TEX.HEALTH_ORB, getX(), getY()));
                Engine.getGameContainer().activateDoor();
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
  
  public int getMinX() { return minXLocation; }
  public void setMinX(int to) { minXLocation = to; }
  public int getMaxX() { return maxXLocation; }
  public void setMaxX(int to) { maxXLocation = to; }
  
  public Projectile firePrimaryWeapon(Point.Float direction) {
    fireTimer.start();
    Point.Float zRot = Projectile.calcRotation(new Point.Float(x, y), direction);
    flipY = (zRot.x < 0);
    if(Engine.isDebugging()) TestDisplay.addTestData("Boss fire to: (" + getX() + ", " + getY() + ")");
    return new Projectile(ID.getNewId(), TEX.ENEMY_WEAPON_1, zRot, getX(), getY(), 5); // fire primary, 5 damage
  }
  
  public boolean didRecentlyFire() { return fireTimer.isRunning(); }
}
