package Main;

/**
 *
 * @author Jeezy
 */
public class Warp {
  public final int warpX, warpY, doorX, doorY;
  
  public Warp(int wx, int wy, int dx, int dy) {
    warpX = wx; warpY = wy; doorX = dx; doorY = dy;
  }
  
  public Warp getWarp() { return this; }
}
