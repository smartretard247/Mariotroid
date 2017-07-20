package Main;

import Drawing.DrawLib;
import Enumerations.ID;

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
   * @param fY Upside down?
   */
  public Door(int id, int x, int y, int wx, int wy, boolean fY) {
    super(id, DrawLib.TEX_DOOR, x, y);
    warp = new Collidable(ID.ID_WARP, DrawLib.TEX_TRANSPARENT, this.x+wx, this.y+wy);
    this.flipY = fY;
  }
  
  public Door(int id, int x, int y, int wx, int wy) {
    this(id, x, y, wx, wy, false);
  }
  
  public Door() {
    this(-1, 0, 0, 0, 0);
  }
  
  public void activate() {
    this.setTextureId(DrawLib.TEX_DOOR_POWERED);
  }
  
  public Collidable getWarp() { return warp; }
}
