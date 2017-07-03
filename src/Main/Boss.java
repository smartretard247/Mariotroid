package Main;

import Drawing.DrawLib;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Boss extends Enemy {
  private int minXLocation = 9000; // to keep from entering the rest of the level
  
  public Boss(int objId, int startLives, int startHealth, int texId, double x, double y, Point speed) {
    super(objId, startLives, startHealth, texId, x, y, speed);
    pointsWorth = 500;
  }
  
  public Boss() {
    this(-1, 1, 1, DrawLib.TEX_ENEMY_BASIC, 0, 0, new Point(0, 0));
  }
  
  public void move() {
    super.move();
    if(x < minXLocation) speedX = -speedX;
  }
  
  public List<Collidable> processCollisions(ArrayList<Collidable> nearObjects) {
    List<Collidable> collisions = getCollisions(nearObjects);
    for(Collidable c : collisions) {
      int id = c.getTextureId();
      switch(id) {
      case DrawLib.TEX_LEVEL:
        if(movingDown()) { // falling straight down
          adjustToTopOf(c);
          speedY = -speedY;
        } else if(movingDownAndRight()) { // falling right and down
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToLeftOf(c);
            speedX = -speedX;
          } else {
            adjustToTopOf(c);
            speedY = -speedY;
          }
        } else if(movingDownAndLeft()) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            adjustToRightOf(c);
            speedX = -speedX;
          } else {
            adjustToTopOf(c);
            speedY = -speedY;
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
          } else {
            adjustToBottomOf(c);
            speedY = -speedY;
          }
        } else if(movingUpAndRight()) { // flying upward and to the right
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getBottom() - getTop())) {
            adjustToLeftOf(c);
          } else {
            adjustToBottomOf(c);
            speedY = -speedY;
          }
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
  
  public int getMinX() { return minXLocation; }
  public void setMinX(int to) { minXLocation = to; }
}
