package Main;

import Drawing.DrawLib;
import java.awt.Point;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jeezy
 */
public class Enemy extends GameObject {
  
  public Enemy(int texId, double x, double y, Point speed) {
    this(texId, x, y);
    speedX = speed.x;
    speedY = speed.y;
  }
  
  public Enemy(int texId, double x, double y) {
    super(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
  }
  
  public Enemy() {
    super(DrawLib.TEX_ENEMY_BASIC, 0, 0);
  }
  
  @Override
  public List<Collidable> move(Map<Integer, Collidable> nearObjects) {
    setSpeedY(speedY - PhysicsEngine.GRAVITY);
    
    List<Collidable> collisions = super.move(nearObjects);
    for(Collidable c : collisions) {
      int id = c.getTextureId();
      switch(id) {
      case DrawLib.TEX_LEVEL:
        if(speedY < 0 && speedX == 0) { // falling straight down
          y = c.getTop() + height/2 + 1;
          speedY = 0;
        } else if(speedY < 0 && speedX > 0) { // falling right and down
          if(Math.abs(c.getLeft() - getRight()) <= Math.abs(c.getTop() - getBottom())) {
            x = c.getLeft() - width/2 - 1;
          } else {
            y = c.getTop() + height/2 + 1;
            speedY = 0;
          }
        } else if(speedY < 0 && speedX < 0) { // falling left and down
          if(Math.abs(c.getRight() - getLeft()) <= Math.abs(c.getTop() - getBottom())) {
            x = c.getRight() + width/2 + 1;
          } else {
            y = c.getTop() + height/2 + 1;
            speedY = 0;
          }
        } else if(speedY == 0 && speedX < 0) { // moving left
          x = c.getRight() + width/2 + 1;
          speedX = -speedX; // reverse direction
        } else if(speedY == 0 && speedX > 0) { // moving right
          x = c.getLeft() - width/2 - 1;
          speedX = -speedX; // reverse direction
        }
        break;
      default: break;
      }
    }
    
    return collisions;
  }
}
