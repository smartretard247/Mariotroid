package Main;

import Drawing.DrawLib;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class LevelBoss extends Boss {
  private Warp warp;
  
  public LevelBoss(int objId, int startLives, int startHealth, int texId, double x, double y, Point.Double speed, int points) {
    super(objId, startLives, startHealth, texId, x, y, speed, points);
  }
  
  public LevelBoss() {
    this(-1, 1, 1, DrawLib.TEX_ENEMY_BASIC, 0, 0, new Point.Double(0, 0), 0);
  }
  
  public Warp getWarp() { return warp; }
  public void setWarp(Warp w) {
    warp = w;
  }
}
