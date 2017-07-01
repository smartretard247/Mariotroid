package Main;

import Drawing.DrawLib;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class Projectile extends Movable {
  private int damage;
  
  private static final int HORIZONTAL_SPEED = 50;
  private static final int VERTICAL_SPEED = 50;
  
  private int zRot;
  private double speedX, speedY; // speed will be calculated by rise/run
  
  public Projectile(int objId, int texId, int zrot, double x, double y, boolean flipY, int d) {
    super(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
    Point speed = calcSpeed(zrot);
    speedX = speed.x;
    speedY = speed.y;
    this.flipY = flipY;
    zRot = zrot;
    damage = d;
  }
  
  public Projectile() {
    super(-1, -1, 0, 0, 0, 0);
  }
  
  public void setZRot(int to) {
    zRot = to;
  }
  
  @Override
  public void draw() {
    GL.glPushMatrix();
    GL.glTranslated(getX(), getY(), 0);
    GL.glRotated(zRot, 0, 0, 1); // will rotate based on clicked position
    if(this.getTextureId() >= 0) {
      //GL.glRotated(flipY ? 90 : -90, 0, 0, 1); // rotate it 90 degress onto its side
      DrawLib.drawTexturedRectangle(this.getTextureId(), (flipY) ? -width : width, height);
    } else {
      GL.glColor3d(color[0], color[1], color[2]);
      GL.glLineWidth((float) height);
      DrawLib.drawLine(0, 0, (flipY) ? -width : width, 0);
    }
    GL.glPopMatrix();
    if(flipY) { //update location based on speed
      x -= speedX;
      y -= speedY;
    } 
    else {
      x += speedX;
      y += speedY;
    }
  }
  
  public static int calcRotation(Point center, Point direction) {
    if(direction.x - center.x == 0) {
      return center.y < 0 ? -90 : 90; // shoot up or down when div by 0
    }
    float slope = (float) ((direction.y - center.y) / (direction.x - center.x)); // rise/run
    if(slope >= -2.0 && slope < -0.5) { return -45; // shoot down and right
    } else if(slope >= -0.5 && slope < 0.5) { return 0; // shoot straight ahead
    } else if(slope >= 0.5 && slope < 2.0) { return 45;// shoot diagonal up and right
    } else if(slope >= 2.0 || slope < -2.0) { return 90;// shoot up
    } else { return -90; // shoot down
    }
  }
  
  /**
   * Given a rotation in degrees, calculates horizontal and vertical speeds, and returns as a Point.
   * @param rotation
   * @return 
   */
  private static Point calcSpeed(int rotation) {
    int maxX = HORIZONTAL_SPEED, maxY = VERTICAL_SPEED;
    switch(rotation) {
      case -90: return new Point(0, -maxY);
      case -45: return new Point((int) Math.sqrt(maxX*maxX/2), (int) -Math.sqrt(maxY*maxY/2));
      case 0: return new Point(maxX, 0);
      case 45: return new Point((int) Math.sqrt(maxX*maxX/2), (int) Math.sqrt(maxY*maxY/2));
      case 90: return new Point(0, maxY);
      default: return null;
    }
  }
  
  public int getDamage() { return damage; }
  public void setDamage(int to) { damage = to; }
}
