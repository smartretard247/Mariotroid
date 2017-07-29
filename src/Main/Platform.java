package Main;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Platform extends Collidable {

  public Platform(int objId, int texId, float x, float y, float w, float h) {
    super(objId, texId, x, y, w, h);
  }

  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects) {
    return null;
  }
}
