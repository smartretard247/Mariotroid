package Main;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Jeezy
 */
public class Enemy extends GameObject {
  
  public Enemy(int texId, double x, double y) {
    super(texId, x, y);
  }
  
  public Enemy(int texId, double x, double y, double w, double h) {
    super(texId, x, y, w, h);
  }
  
  @Override
  public List<Collidable> move(Map<Integer, Collidable> nearObjects) {
    return super.move(nearObjects);
  }
}
