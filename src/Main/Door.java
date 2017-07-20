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
   * @param wx
   * @param wy
   */
  public Door(int id, int x, int y, int wx, int wy) {
    super(id, DrawLib.TEX_DOOR, x, y);
    warp = new Collidable(ID.ID_WARP, DrawLib.TEX_TRANSPARENT, getX()+wx, getY()+wy);
  }
  
  public Door(int id, int x, int y, int warpOffsetX, int warpOffsetY, boolean flipY) {
    this(id, x, y, warpOffsetX, warpOffsetY);
    setFlipY(flipY);
  }
  
  public Door() {
    this(-1, 0, 0, 0, 0);
  }
  
  public void activate() {
    this.setTextureId(DrawLib.TEX_DOOR_POWERED);
  }
  
  public Collidable getWarp() { return warp; }
}
