package Enumerations;

import Main.ObjectContainer;

/**
 *
 * @author Jeezy
 */
public final class ID {
  public static final int WARP = -1000;
  public static final int HERO = 1000;
  public static final int JETPACK = 1001;
  public static final int ALT_WEAPON = 1002;
  //public static final int ENEMY_1 = 1003;
  //public static final int ENEMY_2 = 1004;
  //public static final int ENEMY_3 = 1005;
  //public static final int CALAMITY = 1006;
  public static final int ARMOR = 1007;
  public static final int DOOR = 1008;
  public static final int SHELL = 1009;
  public static final int SWITCH = 1010;
  public static final int FALLING_BOX = 1011;
  public static final int FLYING_BOX = 1012;
  public static final int FLYING_BOX_2 = 1013;
  public static final int FLYING_BOX_3 = 1014;
  //public static final int PHANTOM = 1015;
  public static final int KEY_HOLDER = 1016;
  
  public static final int getNewId() { return ObjectContainer.getNewId(); }
  public static final int getLastId() { return ObjectContainer.getLastId(); }
}
