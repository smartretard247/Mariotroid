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
  
  public Enemy(int startHealth, int texId, double x, double y, Point speed) {
    this(startHealth, texId, x, y);
    speedX = speed.x;
    speedY = speed.y;
  }
  
  public Enemy(int startHealth, int texId, double x, double y) {
    super(startHealth, texId, x, y);
  }
  
  public Enemy() {
    super(1, DrawLib.TEX_ENEMY_BASIC, 0, 0);
  }
  
  @Override
  public List<Collidable> move(Map<Integer, Collidable> nearObjects) {
    setSpeedY(speedY - PhysicsEngine.GRAVITY);
    
    List<Collidable> collisions = super.move(nearObjects);
    for(Collidable c : collisions) {
      int id = c.getTextureId();
      switch(id) {
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
      default: break;
      }
    }
    
    return collisions;
  }
}
