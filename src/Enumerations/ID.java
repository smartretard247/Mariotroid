package Enumerations;

/**
 *
 * @author Jeezy
 */
public final class ID {
  private static int nextId = 1000;
  
  public static final int ID_HERO = 0;
  public static final int ID_JETPACK = 1;
  public static final int ID_ALT_WEAPON = 2;
  public static final int ID_ENEMY_1 = 3;
  public static final int ID_ENEMY_2 = 4;
  public static final int ID_ENEMY_3 = 5;
  
  public static final int getNewId() { return nextId++; }
}
