package Main;

import Drawing.DrawLib;
import Enumerations.TEX;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Projectile extends Movable {
  private int damage;
  
  private static final int SPEED = 50;
  
  private int zRot;
  
  public Projectile(int objId, int texId, Point.Float zrot, float x, float y, int d) {
    super(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight());
    setSpeed(calcSpeed(zrot));
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
  
  public static Point.Float calcRotation(Point.Float center, Point.Float direction) {
    float dx, dy, x, y, mag;
    dx = direction.x - center.x;
    dy = direction.y - center.y;
    if(dx == 0 && dy == 0) {
      x = 0;
      y = 0;
    } else if(dx == 0) {
      mag = dy;
      x = 0;
      y = dy / mag;
    } else if(dy == 0) {
      mag = dx;
      x = dx / mag;
      y = 0;
    } else {
      mag = (float)Math.abs(Math.sqrt((dx*dx) + (dy*dy)));
      x = dx / mag;
      y = dy / mag;
    }
    return new Point.Float(x, y);
   }
  
  /**
   * Given a rotation in degrees, calculates horizontal and vertical speeds, and returns as a Point.
   * @param rotation
   * @return 
   */
  private static Point.Float calcSpeed(Point.Float rotation) {
    return new Point.Float((SPEED * rotation.x), (SPEED * rotation.y));
  }
  
  public int getDamage() { return damage; }
  public void setDamage(int to) { damage = to; }
  
  public List<Collidable> processCollisions(ArrayList<Collidable> nearObjects) {
    List<Collidable> collisions = getCollisions(nearObjects);
    List<Collidable> invalidCollisions = new LinkedList<>();
    for(Collidable c : collisions) {
      int collisiontexId = c.getTextureId();
      switch(collisiontexId) {
      case TEX.TEX_LEVEL: // do nothing
        break;
      default:
        invalidCollisions.add(c); // remove all but level collisions, will be processed be other classes
        break;
      }
    }
    collisions.removeAll(invalidCollisions);
    return collisions;
  }
}
