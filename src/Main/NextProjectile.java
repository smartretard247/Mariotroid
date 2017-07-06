package Main;

import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class NextProjectile {
  public Point screenCoord = new Point();
  public Point.Double worldCoord = new Point.Double();
  public boolean isFromPrimaryWeapon = true;
  public boolean isFromEnemy = false;
  
  public NextProjectile(Point sc, boolean fromPrimary) {
    screenCoord = sc;
    worldCoord = null;
    isFromPrimaryWeapon = fromPrimary;
    isFromEnemy = false;
  }
  
  public NextProjectile(Point.Double wc, boolean fromEnemy) {
    screenCoord = null;
    worldCoord = wc;
    isFromPrimaryWeapon = false;
    isFromEnemy = true;
  }
}
