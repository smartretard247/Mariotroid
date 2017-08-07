package Main;

import Enumerations.ID;
import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public class NextProjectile {
  public Point screenCoord = new Point();
  public Point.Float worldCoord = new Point.Float();
  public boolean isFromPrimaryWeapon = true;
  public int firedID;
  
  public NextProjectile(Point sc, boolean fromPrimary) {
    screenCoord = sc;
    worldCoord = null;
    isFromPrimaryWeapon = fromPrimary;
    firedID = ID.HERO;
  }
  
  public NextProjectile(Point.Float wc, int firedFrom) {
    screenCoord = null;
    worldCoord = wc;
    isFromPrimaryWeapon = false;
    firedID = firedFrom;
  }
}
