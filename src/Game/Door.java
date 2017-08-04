package Game;

import Enumerations.ID;
import Enumerations.TEX;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeezy
 */
public class Door extends Collidable {
  private final Collidable warp;
  
  /**
   * Create a deactivated door.
   * @param id ID
   * @param x X Position
   * @param y Y Position
   * @param wx Warp's x location
   * @param wy Warp's y location
   * @param fX Upside down?
   */
  public Door(int id, int x, int y, int wx, int wy, boolean fX) {
    super(id, TEX.DOOR, x, y);
    warp = new Item(ID.WARP, TEX.TRANSPARENT, this.x+wx, this.y+wy);
    flipX = fX;
  }
  
  public Door(int id, int x, int y, int wx, int wy) {
    this(id, x, y, wx, wy, false);
  }
  
  public Door() {
    this(-1, 0, 0, 0, 0);
  }
  
  public void activate() {
    this.setTextureId(TEX.DOOR_POWERED);
  }
  
  public Collidable getWarp() { return warp; }

  @Override
  public List<Integer> processCollisions(ArrayList<Collidable> nearObjects) {
    return null;
  }
}
