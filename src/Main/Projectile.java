package Main;

import Drawing.DrawLib;
import Enumerations.ID;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Projectile extends Movable {
  private int damage;
  
  private static final int SPEED = 50;
  
  private int zRot;
  
  public Projectile(int objId, int texId, Point.Double zrot, double x, double y, int d) {
    super(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
    Point.Double speed = calcSpeed(zrot);
    speedX = speed.x;
    speedY = speed.y;
    zRot = (int)Math.toDegrees(Math.atan2(zrot.y, zrot.x));
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
  
  public static Point.Double calcRotation(Point.Double center, Point.Double direction) {
    double dx, dy, x, y, mag;
    dx = direction.x - center.x;
    dy = direction.y - center.y;
    if(dx == 0 && dy == 0){
      x = 0;
      y = 0;
    } else if(dx == 0){
      mag = dy;
      x = 0;
      y = dy / mag;
    } else if(dy == 0){
      mag = dx;
      x = dx / mag;
      y = 0;
    } else{
      mag = Math.abs(Math.sqrt((dx*dx) + (dy*dy)));
      x = dx / mag;
      y = dy / mag;
    }
    return new Point.Double(x, y);
   }
  
  /**
   * Given a rotation in degrees, calculates horizontal and vertical speeds, and returns as a Point.
   * @param rotation
   * @return 
   */
  private static Point.Double calcSpeed(Point.Double rotation) {
    return new Point.Double((SPEED * rotation.x), (SPEED * rotation.y));
  }
  
  public int getDamage() { return damage; }
  public void setDamage(int to) { damage = to; }
  
  public List<Collidable> processCollisions(ArrayList<Collidable> nearObjects) {
    List<Collidable> collisions = getCollisions(nearObjects);
    for(Collidable c : collisions) {
      int collisiontexId = c.getTextureId();
      switch(collisiontexId) {
      case DrawLib.TEX_LEVEL: // do nothing
        break;
      default:
        collisions.remove(c); // remove all but level collisions, will be processed be other classes
        break;
      }
    }
    
    return collisions;
  }
}
