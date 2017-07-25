package Enumerations;

import java.util.ArrayList;

/**
 *
 * @author Jeezy
 */
public enum PAUSE_MENU_OPTION {
  CONTINUE {
    @Override
    public PAUSE_MENU_OPTION prev() {
      return RESTART;
    };
  },
  
  // ADD ANY ADDITONAL ENUMERATIONS HERE!!! MUST BE IN BETWEEN START_GAME AND EXIT
  
  RESTART {
    @Override
    public PAUSE_MENU_OPTION next() {
      return CONTINUE;
    };
  };
  
  public PAUSE_MENU_OPTION next() {
    return values()[ordinal() + 1];
  }
  
  public PAUSE_MENU_OPTION prev() {
    return values()[ordinal() - 1];
  }
  
  public static ArrayList<String> getValuesArray() {
    ArrayList<String> values = new ArrayList<>();
    for(PAUSE_MENU_OPTION smo : values()) {
      values.add(smo.toString().replace("_", " "));
    }
    return values;
  }
}
