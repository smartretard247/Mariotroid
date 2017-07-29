package Main;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Item extends Collidable {

  public Item(int id, int texId, float x, float y) {
    super(id, texId, x, y);
  }

  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects) {
    return null;
  }
  
}
