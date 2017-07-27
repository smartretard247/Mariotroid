package Main;

import Drawing.DrawLib;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class Heavy extends Movable {
  protected float weight;
  
  public Heavy(int objId, int texId, float x, float y, float w, float h, Point.Float s) {
    super(objId, texId, x, y, w, h, s);
    weight = 1f;
    PhysicsEngine.addHeavy(this);
  }
  
  public Heavy(int objId, int texId, float x, float y, float w, float h) {
    this(objId, texId, x, y, w, h, new Point.Float(0, 0));
  }
  
  public Heavy(int objId, int texId, float x, float y) {
    this(objId, texId, x, y, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight(), new Point.Float(0, 0));
  }
  
  public Heavy(int objId, int texId) {
    this(objId, texId, 0, 0, DrawLib.getTexture(texId).getWidth(), DrawLib.getTexture(texId).getHeight(), new Point.Float(0, 0));
  }
  
  public Heavy() {
    this(-1, -1, 0, 0, 0, 0, new Point.Float(0, 0));
  }
  
  public void setWeight(float to) { weight = to; }
  public float getWeight() { return weight; }
  public boolean isWeightless() { return weight == 0; }
}
