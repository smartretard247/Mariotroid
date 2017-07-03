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
  
  public Projectile(int objId, int texId, double zrot, double x, double y, int d) {
    super(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
    Point.Double speed = calcSpeed((int) zrot);
    speedX = speed.x;
    speedY = speed.y;
    zRot = (int) zrot;
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
      DrawLib.drawTexturedRectangle(this.getTextureId(), width, height);
    } else {
      GL.glColor3d(color[0], color[1], color[2]);
      GL.glLineWidth((float) height);
      DrawLib.drawLine(0, 0, width, 0);
    }
    GL.glPopMatrix();
  }
  
  public static double calcRotation(Point.Double center, Point.Double direction) {
    double dirX = direction.x, dirY = direction.y, cX = center.x, cY = center.y;
    double rise = (dirY - cY), run = (dirX - cX);
    if(run == 0) { return cY < 0 ? -90 : 90; } // shoot up or down when div by 0
    double slope = rise / run;
    
    if(slope >= -2.0 && slope < -0.5) {
      return (rise < 0) ? -45 : 135; // shoot down and right, or up and left
    } else if(slope >= -0.5 && slope < 0.5) {
      return (run < 0) ? 180 : 0; // directly behind, or shoot straight ahead 
    } else if(slope >= 0.5 && slope < 2.0) {
      return (rise > 0) ? 45 : -135;// shoot diagonal up and right, or down and left
    } else if(slope >= 2.0 || slope < -2.0) {
      return (rise > 0) ? 90 : -90;// shoot up, or down
    } else {
      return 0; // default to shooting straight
    }
  }
  
  /**
   * Given a rotation in degrees, calculates horizontal and vertical speeds, and returns as a Point.
   * @param rotation
   * @return 
   */
  private static Point.Double calcSpeed(int rotation) {
    int maxX = HORIZONTAL_SPEED, maxY = VERTICAL_SPEED;
    switch(rotation) {
      case -135: return new Point.Double(Math.sqrt(maxX*maxX/2) * -1, Math.sqrt(maxY*maxY/2) * -1);
      case -90: return new Point.Double(0, -maxY);
      case -45: return new Point.Double(Math.sqrt(maxX*maxX/2), -Math.sqrt(maxY*maxY/2));
      case 0: return new Point.Double(maxX, 0);
      case 45: return new Point.Double(Math.sqrt(maxX*maxX/2), Math.sqrt(maxY*maxY/2));
      case 90: return new Point.Double(0, maxY);
      case 135: return new Point.Double(Math.sqrt(maxX*maxX/2) * -1, Math.sqrt(maxY*maxY/2));
      case 180: return new Point.Double(-maxX, 0);
      default: return null;
    }
  }
  
  public int getDamage() { return damage; }
  public void setDamage(int to) { damage = to; }
}
