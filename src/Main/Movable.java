package Main;

import Drawing.DrawLib;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Jeezy
 */
public abstract class Movable extends GameObject {
  public Movable(int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
  }
  
  public Movable(int texId, double x, double y) {
    super(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
  }
  
  public Movable(int texId) {
    super(texId, 0, 0, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
  }
  
  public List<Collidable> move(Map<Integer,Collidable> nearObjects) {
    return super.move(nearObjects);
  }
  
  public boolean standingStill() {
    return (speedY == 0 && speedX == 0);
  }
  
  public boolean movingUp() {
    return (speedY > 0 && speedX == 0);
  }
  
  public boolean movingDown() {
    return (speedY < 0 && speedX == 0);
  }
  
  public boolean movingLeft() {
    return (speedY == 0 && speedX < 0);
  }
  
  public boolean movingRight() {
    return (speedY == 0 && speedX > 0);
  }
  
  public boolean movingUpAndLeft() {
    return (speedY > 0 && speedX < 0);
  }
  
  public boolean movingUpAndRight() {
    return (speedY > 0 && speedX > 0);
  }
  
  public boolean movingDownAndLeft() {
    return (speedY < 0 && speedX < 0);
  }
  
  public boolean movingDownAndRight() {
    return (speedY < 0 && speedX > 0);
  }
  
  public void adjustToBottomOf(Collidable c) {
    y = c.getBottom() - height/2 - 1;
  }
  
  public void adjustToTopOf(Collidable c) {
    y = c.getTop() + height/2 + 1;
  }
  
  public void adjustToRightOf(Collidable c) {
    x = c.getRight() + width/2 + 1;
  }
  
  public void adjustToLeftOf(Collidable c) {
    x = c.getLeft() - width/2 - 1;
  }
}
