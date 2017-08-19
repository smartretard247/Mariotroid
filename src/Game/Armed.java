package Game;

import java.awt.Point;

/**
 *
 * @author Jeezy
 */
public interface Armed {
  public abstract Projectile firePrimaryWeapon(Point.Float direction);
  public abstract Projectile fireSecondaryWeapon(Point.Float direction);
}
