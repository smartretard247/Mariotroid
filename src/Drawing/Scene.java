package Drawing;

/**
 *
 * @author Jeezy
 */
public class Scene {
  // variables to translate the scene
  public float transX;
  public float transY;
  public float transZ;
  public float scaleX;
  public float scaleY;
  public float scaleZ;
  public float globalZ;
  public final int LEVEL_DEPTH = 40;
  
  private float defTransX;
  private float defTransY;
  private float defTransZ;
  private float defScaleX;
  private float defScaleY;
  private float defScaleZ;
  private float defGlobalZ;
  
  public Scene(float tX, float tY, float tZ, float sX, float sY, float sZ) {
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
  
  public void setTranslation(float tX, float tY, float tZ) {
    transX = tX;
    transY = tY;
    transZ = tZ;
  }
  
  public void setScale(float sX, float sY, float sZ) {
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
