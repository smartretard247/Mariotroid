package Main;

import Drawing.DrawLib;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class Projectile extends Collidable {
  private static final int MAX_SPEED_X = 50;
  private static final int MAX_SPEED_Y = 50;
  
  private int zRot;
  private double speedX, speedY; // speed will be calculated by rise/run
  
  public Projectile(int texId, int zrot, double x, double y, double w, double h, double sX, double sY, boolean flipY) {
    super(texId, x, y, w, h);
    speedX = sX;
    speedY = sY;
    this.flipY = flipY;
    setColor(1.0, 0.0, 0.0);
    zRot = zrot;
  }
  
  public Projectile(int texId, int zrot, double x, double y, double sX, double sY, boolean flipY) {
    super(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getWidth());
    speedX = sX;
    speedY = sY;
    this.flipY = flipY;
    zRot = zrot;
  }
  
  public Projectile(int texId, int zrot, double x, double y, boolean flipY) {
    super(texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getWidth());
    Point speed = calcSpeed(zrot);
    speedX = speed.x;
    speedY = speed.y;
    this.flipY = flipY;
    zRot = zrot;
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
      DrawLib.drawTexturedRectangle(this.getTextureId(), width*3, height*3);
    } else {
      GL.glColor3d(color[0], color[1], color[2]);
      GL.glLineWidth((float) height);
      if(flipY) 
        DrawLib.drawLine(0, 0, -width, 0);
      else
        DrawLib.drawLine(0, 0, width, 0);
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
    int maxX = MAX_SPEED_X, maxY = MAX_SPEED_Y;
    switch(rotation) {
      case -90: return new Point(0, -maxY);
      case -45: return new Point((int) Math.sqrt(maxX*maxX/2), (int) -Math.sqrt(maxY*maxY/2));
      case 0: return new Point(maxX, 0);
      case 45: return new Point((int) Math.sqrt(maxX*maxX/2), (int) Math.sqrt(maxY*maxY/2));
      case 90: return new Point(0, maxY);
      default: return null;
    }
  }
}
