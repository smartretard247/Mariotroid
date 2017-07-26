package Main;

import Enumerations.ID;
import Enumerations.TEX;

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
    super(id, TEX.TEX_DOOR, x, y);
    warp = new Collidable(ID.ID_WARP, TEX.TEX_TRANSPARENT, this.x+wx, this.y+wy);
    flipX = fX;
  }
  
  public Door(int id, int x, int y, int wx, int wy) {
    this(id, x, y, wx, wy, false);
  }
  
  public Door() {
    this(-1, 0, 0, 0, 0);
  }
  
  public void activate() {
    this.setTextureId(TEX.TEX_DOOR_POWERED);
  }
  
  public Collidable getWarp() { return warp; }
}
