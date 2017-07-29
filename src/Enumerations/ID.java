package Enumerations;

import Main.ObjectContainer;

/**
 *
 * @author Jeezy
 */
public final class ID {
  public static final int WARP = -1;
  public static final int HERO = 0;
  public static final int JETPACK = 1;
  public static final int ALT_WEAPON = 2;
  public static final int ENEMY_1 = 3;
  public static final int ENEMY_2 = 4;
  public static final int ENEMY_3 = 5;
  public static final int CALAMITY = 6;
  public static final int ARMOR = 7;
  public static final int DOOR = 8;
  public static final int SHELL = 9;
  public static final int SWITCH = 10;
  public static final int FALLING_BOX = 11;
  public static final int FLYING_BOX = 12;
  public static final int FLYING_BOX_2 = 13;
  public static final int FLYING_BOX_3 = 14;
  
  public static final int getNewId() { return ObjectContainer.getNewId(); }
  public static final int getLastId() { return ObjectContainer.getLastId(); }
}
