package Main;

/**
 *
 * @author Jeezy
 */
public enum START_MENU_OPTIONS {
  START_GAME {
    @Override
    public START_MENU_OPTIONS prev() {
        return EXIT;
    };
  },
  
  // ADD ANY ADDITONAL ENUMERATIONS HERE!!! MUST BE IN BETWEEN START_GAME AND EXIT
  
  EXIT {
    @Override
    public START_MENU_OPTIONS next() {
        return START_GAME;
    };
  };
  
  public START_MENU_OPTIONS next() {
        return values()[ordinal() + 1];
  }
  
  public START_MENU_OPTIONS prev() {
        return values()[ordinal() - 1];
  }
}
