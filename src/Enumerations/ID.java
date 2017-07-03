package Enumerations;

import Main.ObjectContainer;

/**
 *
 * @author Jeezy
 */
public final class ID {
  public static final int ID_HERO = 0;
  public static final int ID_JETPACK = 1;
  public static final int ID_ALT_WEAPON = 2;
  public static final int ID_ENEMY_1 = 3;
  public static final int ID_ENEMY_2 = 4;
  public static final int ID_ENEMY_3 = 5;
  public static final int ID_CALAMITY = 6;
  public static final int ID_ARMOR = 7;
  public static final int ID_DOOR = 8;
  public static final int ID_DOOR_POWERED = 9;
  
  public static final int getNewId() { return ObjectContainer.getNewId(); }
}
