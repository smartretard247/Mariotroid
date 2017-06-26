package Main;

import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class NextProjectile {
  public Point screenCoord = new Point();
  public boolean isFromPrimaryWeapon = true;
  
  public NextProjectile(Point sc, boolean isPrimary) {
    screenCoord = sc;
    isFromPrimaryWeapon = isPrimary;
  }
}
