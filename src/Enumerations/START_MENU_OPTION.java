package Enumerations;

import java.util.ArrayList;

/**
 *
 * @author Jeezy
 */
public enum START_MENU_OPTION {
  START_GAME {
    @Override
    public START_MENU_OPTION prev() {
      return EXIT;
    };
  },
  
  // ADD ANY ADDITONAL ENUMERATIONS HERE!!! MUST BE IN BETWEEN START_GAME AND EXIT
  
  EXIT {
    @Override
    public START_MENU_OPTION next() {
      return START_GAME;
    };
  };
  
  public START_MENU_OPTION next() {
    return values()[ordinal() + 1];
  }
  
  public START_MENU_OPTION prev() {
    return values()[ordinal() - 1];
  }
  
  public static ArrayList<String> getValuesArray() {
    ArrayList<String> values = new ArrayList<>();
    for(START_MENU_OPTION smo : values()) {
      values.add(smo.toString().replace("_", " "));
    }
    return values;
  }
}
