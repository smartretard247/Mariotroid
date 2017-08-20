package Game;

/**
 *
 * @author Jeezy
 */
public interface AutoFires {
  public boolean didRecentlyFire();
  public boolean closeToTarget(Hero h);
}
