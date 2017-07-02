package Main;

import Drawing.DrawLib;
import java.awt.Point;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jeezy
 */
public class Enemy extends Living {
  protected int pointsWorth = 100;
  
  public Enemy(int objId, int startLives, int startHealth, int texId, double x, double y, Point speed) {
    super(objId, startLives, startHealth, texId, x, y, speed);
  }
  
  public Enemy() {
    this(-1, 1, 1, DrawLib.TEX_ENEMY_BASIC, 0, 0, new Point(0, 0));
  }
  
  public List<Collidable> processCollisions(Map<Integer, Collidable> nearObjects) {
    setSpeedY(speedY - PhysicsEngine.GRAVITY);
    
    List<Collidable> collisions = getCollisions(nearObjects);
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
          } else {
            adjustToTopOf(c);
            speedY = 0;
          }
        } else if(movingDownAndLeft()) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToRightOf(c);
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
        }
        break;
      default:
        if(new Projectile().getClass().isInstance(c)) {
          Projectile p = (Projectile)c;
          try {
            loseHealth(p.getDamage());
          } catch (GameOverException ex) { // enemy died
          }
        }
        break;
      }
    }
    
    return collisions;
  }
  
  public int getPointsWorth() { return pointsWorth; }
}
