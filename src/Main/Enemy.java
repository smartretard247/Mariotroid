package Main;

import Drawing.DrawLib;
import Test.TestDisplay;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Enemy extends Living {
  protected int pointsWorth = 100;
  protected static final int BASE_DAMAGE = 2; // damage dealt by contact, not projectiles
  
  public Enemy(int objId, int startLives, int startHealth, int texId, double x, double y, Point.Double speed) {
    super(objId, startLives, startHealth, texId, x, y, speed);
  }
  
  public Enemy() {
    this(-1, 1, 1, DrawLib.TEX_ENEMY_BASIC, 0, 0, new Point.Double(0, 0));
  }
  
  public List<Collidable> processCollisions(ArrayList<Collidable> nearObjects) {
    setSpeedY(speedY - PhysicsEngine.getGravity());
    
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Collidable> invalidCollisions = new LinkedList<>();
    for(Collidable c : collisions) {
      int texId = c.getTextureId();
      int objId = c.getObjectId();
      switch(texId) {
      case DrawLib.TEX_LEVEL:
        if(movingDown()) { // falling straight down
          adjustToTopOf(c);
          speedY = 0;
        } else if(movingDownAndRight()) { // falling right and down
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToLeftOf(c);
            speedX = -speedX;
          } else {
            adjustToTopOf(c);
            speedY = 0;
          }
        } else if(movingDownAndLeft()) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToRightOf(c);
            speedX = -speedX;
          } else {
            adjustToTopOf(c);
            speedY = 0;
          }
        } else if(movingLeft()) { // moving left
          adjustToRightOf(c);
          speedX = -speedX; // reverse direction
        } else if(movingRight()) { // moving right
          adjustToLeftOf(c);
          speedX = -speedX; // reverse direction
        } else if(movingUpAndLeft()) { // flying upward and to the left
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getBottom() - getTop())) {
            adjustToRightOf(c);
            speedX = -speedX; // reverse direction
          } else {
            adjustToBottomOf(c);
            speedY = 0;
          }
        } else if(movingUpAndRight()) { // flying upward and to the right
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
            adjustToLeftOf(c);
            speedX = -speedX; // reverse direction
          } else {
            adjustToBottomOf(c);
            speedY = 0;
          }
        } else if(movingUp()) { // flying straight upward
          adjustToBottomOf(c);
          speedY = 0;
        }
        break;
      default:
        if(new Projectile().getClass().isInstance(c)) {
          if(!(c.getTextureId() == DrawLib.TEX_ENEMY_WEAPON_1 || c.getTextureId() == DrawLib.TEX_ENEMY_WEAPON_2)) {
            Projectile p = (Projectile)c;
            try {
              if(Engine.isDebugging()) TestDisplay.addTestData("Enemy HP: " + getHealth());
              loseHealth(p.getDamage());
              if(Engine.isDebugging()) TestDisplay.addTestData("Enemy damage: " + p.getDamage() + " / Enemy HP: " + getHealth());
            } catch (GameOverException ex) { // enemy died
              if(Engine.isDebugging()) TestDisplay.addTestData("Enemy destroyed");
            }
          } else {
            invalidCollisions.add(c);
          }
        }
        break;
      }
    }
    collisions.removeAll(invalidCollisions); // do not count this as true collision
    return collisions;
  }
  
  @Override
  public void move() {
    if(PhysicsEngine.gravityIsInverted()) this.setSpeedY(20);
    super.move();
  }
  
  @Override
  public void draw() {
    flipX = PhysicsEngine.gravityIsInverted();
    super.draw();
  }
  
  public int getPointsWorth() { return pointsWorth; }
  public void setPointsWorth(int to) { pointsWorth = to; }
  
  public static int getBaseDamage() { return BASE_DAMAGE; }
}
