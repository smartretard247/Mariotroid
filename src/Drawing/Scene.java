package Drawing;

/**
 *
 * @author Jeezy
 */
public class Scene {
  // variables to translate the scene
  public double transX;
  public double transY;
  public double transZ;
  public double scaleX;
  public double scaleY;
  public double scaleZ;
  public double globalZ;
  
  private double defTransX;
  private double defTransY;
  private double defTransZ;
  private double defScaleX;
  private double defScaleY;
  private double defScaleZ;
  private double defGlobalZ;
  
  public Scene(double tX, double tY, double tZ, double sX, double sY, double sZ) {
    transX = tX;
    transY = tY;
    transZ = tZ;
    scaleX = sX;
    scaleY = sY;
    scaleZ = sZ;
    
    defTransX = tX;
    defTransY = tY;
    defTransZ = tZ;
    defScaleX = sX;
    defScaleY = sY;
    defScaleZ = sZ;
    defGlobalZ = 0;
  }
  
  public Scene() {
    this(0, 0, 0, 1, 1, 1);
  }
  
  public void setTranslation(double tX, double tY, double tZ) {
    transX = tX;
    transY = tY;
    transZ = tZ;
  }
  
  public void setScale(double sX, double sY, double sZ) {
    transX = sX;
    transY = sY;
    transZ = sZ;
  }
  
  public void resetTranslation() {
    transX = defTransX;
    transY = defTransY;
    transZ = defTransZ;
  }
  
  public void resetGlobalZ() {
    globalZ = defGlobalZ;
  }
  
  public void resetScale() {
    scaleX = defScaleX;
    scaleY = defScaleY;
    scaleZ = defScaleZ;
  }
  
  public void resetAll() {
    resetTranslation();
    resetGlobalZ();
    resetScale();
    resetGlobalZ();
  }
}
