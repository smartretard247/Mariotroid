package Main;

import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class NextProjectile {
  public Point screenCoord = new Point();
  public Point.Float worldCoord = new Point.Float();
  public boolean isFromPrimaryWeapon = true;
  public boolean isFromEnemy = false;
  
  public NextProjectile(Point sc, boolean fromPrimary) {
    screenCoord = sc;
    worldCoord = null;
    isFromPrimaryWeapon = fromPrimary;
    isFromEnemy = false;
  }
  
  public NextProjectile(Point.Float wc, boolean fromEnemy) {
    screenCoord = null;
    worldCoord = wc;
    isFromPrimaryWeapon = false;
    isFromEnemy = true;
  }
}
