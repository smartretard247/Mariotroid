package Main;

import com.jogamp.opengl.util.packrect.Rect;

/**
 *
 * @author Jeezy
 */
public class GameObject {
  private static double maxSpeedX = PhysicsEngine.TERMINAL_SPRINT;
  private static double maxSpeedY = PhysicsEngine.TERMINAL_VELOCITY;
  
  public GameObject(double x, double y, double speedx, double speedy, int w, int h) {
    X = x;
    Y = y;
    speedX = 0.0;
    speedY = 0.0;
    width = w;
    height = h;
  }
  public GameObject() {
    this(0.0, 0.0, 0.0, 0.0, 50, 50);
  }
  
  private double X; // for moving x direction
  private double Y; // for moving y direction
  private double speedX; // movement increment x
  private double speedY; // movement increment y
  
  private int width, height; // for building the collision rect
  
  public double getX() { return X; }
  public double getY() { return Y; }
  public double getSpeedX() { return speedX; }
  public double getSpeedY() { return speedY; }
  
  public void setX(double posX) { X = posX; }
  public void setY(double posY) { Y = posY; }
  public void setPosition(double posX, double posY) { X = posX; Y = posY; }
  public void setSpeedX(double spdX) { speedX = spdX; }
  public void setSpeedY(double spdY) { speedY = spdY; }
  public void setSpeed(double spdX, double spdY) { speedX = spdX; speedY = spdY; }
  public void move() {
    X += speedX;
    Y += speedY;
  }
  public void increaseSpeed(double deltaX, double deltaY) {
    speedX += deltaX;
    speedY += deltaY;
    if(Math.abs(speedX) > maxSpeedX)
      speedX = (speedX < 0) ? -maxSpeedX : maxSpeedX;
    if(Math.abs(speedY) > maxSpeedY)
      speedY = (speedY < 0) ? -maxSpeedY : maxSpeedY;
  }
  
  public void setDimensions(int w, int h) {
    width = w;
    height = h;
  }
  
  public Rect getCollisionRect() {
    return new Rect((int)X, (int)Y, width, height, null); // possibility of using custom object here
  }
  
  /**
   * Tests an intersection with the supplied Rect.  Returns false if no collision.
   * @param dest
   * @return 
   */
  public boolean intersect(Rect dest) {
    Rect src = this.getCollisionRect();
    return !(src.x() > dest.x() + dest.w() // R1 is right to R2
          || src.x() + src.w() < dest.x() // R1 is left to R2
          || src.y() < dest.y() + dest.h() // R1 is above R2
          || src.y() + src.h() > dest.y()); // R1 is below R1
  }
  
  public void resetAll() {
    this.setPosition(0.0, 0.0);
    this.setSpeed(0, 0);
  }
}
