package Game;

import Drawing.DrawLib;
import Main.PhysicsEngine;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Movable extends Collidable {
  public static float MAX_SPEED_X = PhysicsEngine.getTerminalX();
  public static float MAX_SPEED_Y = PhysicsEngine.getTerminalY();
  private Point.Float defSpeed;
  protected Point.Float speed;
  private static final float SPRINT_MULTIPLIER = 1.5f;
  private boolean isSprinting;

  public Movable(int objId, int texId, float x, float y, float w, float h, Point.Float s) {
    super(objId, texId, x, y, w, h);
    speed = s;
    defSpeed = new Point.Float(s.x, s.y);
    isSprinting = false;
  }
  
  public Movable(int objId, int texId, float x, float y, float w, float h) {
    this(objId, texId, x, y, w, h, new Point.Float(0, 0));
  }
  
  public Movable(int objId, int texId, float x, float y) {
    this(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight(), new Point.Float(0, 0));
  }
  
  public Movable(int objId, int texId) {
    this(objId, texId, 0, 0, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight(), new Point.Float(0, 0));
  }
  
  public Movable() {
    this(-1, -1, 0, 0, 0, 0, new Point.Float(0, 0));
  }
  
  // getters/setters
  public Point.Float getSpeed() { return speed; }
  public float getSpeedX() { return speed.x; }
  public float getSpeedY() { return speed.y; }
  public void setSpeedX(float spdX) { speed.x = spdX; }
  public void setSpeedY(float spdY) { speed.y = spdY; }
  public void setSpeed(float spdX, float spdY) { speed.x = spdX; speed.y = spdY; }
  public void setSpeed(Point.Float to) { speed.x = to.x; speed.y = to.y; }
  public void setDefaultSpeed(Point.Float to) { defSpeed = to; }
  
  public void resetAll() {
    this.resetPosition();
    this.setSpeed(defSpeed);
    if(isSprinting) toggleSprint();
    resetCurrGrid();
  }
  
  /**
   * Moves object by speedX and speedY, also flips image if direction changes.
   */
  public void move() {
    x += getSpeedX();
    y += getSpeedY();
    if(getSpeedX() != 0) setFlipY(getSpeedX() < 0); // this reverses the sprite with direction changes
    resetCurrGrid(); // updates grid location
  }
  
  public boolean standingStill() {
    return (getSpeedY() == 0 && getSpeedX() == 0);
  }
  
  public boolean movingUpIgnoreX() {
    return (getSpeedY() > 0);
  }
  
  public boolean movingDownIgnoreX() {
    return (getSpeedY() < 0);
  }
  
  public boolean movingRightIgnoreY() {
    return (getSpeedX() > 0);
  }
  
  public boolean movingLeftIgnoreY() {
    return (getSpeedX() < 0);
  }
  
  public boolean movingUp() {
    return (getSpeedY() > 0 && getSpeedX() == 0);
  }
  
  public boolean movingDown() {
    return (getSpeedY() < 0 && getSpeedX() == 0);
  }
  
  public boolean movingLeft() {
    return (getSpeedY() == 0 && getSpeedX() < 0);
  }
  
  public boolean movingRight() {
    return (getSpeedY() == 0 && getSpeedX() > 0);
  }
  
  public boolean movingUpAndLeft() {
    return (getSpeedY() > 0 && getSpeedX() < 0);
  }
  
  public boolean movingUpAndRight() {
    return (getSpeedY() > 0 && getSpeedX() > 0);
  }
  
  public boolean movingDownAndLeft() {
    return (getSpeedY() < 0 && getSpeedX() < 0);
  }
  
  public boolean movingDownAndRight() {
    return (getSpeedY() < 0 && getSpeedX() > 0);
  }
  
  public void adjustToBottomOf(Collidable c) {
    y = c.getBottom() - height/2 - 1;
    resetCurrGrid();
  }
  
  public void adjustToTopOf(Collidable c) {
    y = c.getTop() + height/2 + 1;
    resetCurrGrid();
  }
  
  public void adjustToRightOf(Collidable c) {
    x = c.getRight() + width/2 + 1;
    resetCurrGrid();
  }
  
  public void adjustToLeftOf(Collidable c) {
    x = c.getLeft() - width/2 - 1;
    resetCurrGrid();
  }
  
  public void reverseSpeedX() {
    speed.x = -speed.x;
  }
  
  public void reverseSpeedY() {
    speed.y = -speed.y;
  }
  
  public void increaseSpeed(float deltaX, float deltaY) {
    speed.x += deltaX;
    speed.y += deltaY;
    if(Math.abs(speed.x) > MAX_SPEED_X)
      speed.x = (speed.x < 0) ? -MAX_SPEED_X : MAX_SPEED_X;
    if(Math.abs(speed.y) > MAX_SPEED_Y)
      speed.y = (speed.y < 0) ? -MAX_SPEED_Y : MAX_SPEED_Y;
  }
  
  /**
   * Toggles between maximum running speeds.
   */
  public void toggleSprint() {
    if(!isSprinting) {
      MAX_SPEED_X *= SPRINT_MULTIPLIER;
      isSprinting = true;
    } else {
      MAX_SPEED_X /= SPRINT_MULTIPLIER;
      isSprinting = false;
    }
    if(speed.x != 0)
      speed.x = (speed.x > 0) ? MAX_SPEED_X : -MAX_SPEED_X;
  }
  
  public void setSprinting(boolean to) {
    if(isSprinting == to) return;
    toggleSprint();
  }
  public boolean isSprinting() { return isSprinting; }

  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects) {
    return null;
  }
}
