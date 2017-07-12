package Drawing;

import java.io.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;

/**
 *
 * @author Jeezy
 */
public class CustomFont {
  private final Font basefont;
  private Font font;
  private final FontRenderContext context;

  public CustomFont(InputStream is) {
    basefont = loadBaseFont(is);
    context = getContext();
    setSize(48f);
  }

  private Font getFontBySize(float s) {
    return basefont.deriveFont(s);
  }

  /**
   * Sets the size of the font.
   * @param size float
   */
  public void setSize(float size) {
    font = getFontBySize(size);
  }
  
  private Font loadBaseFont(InputStream is) {
    Font newFont = null;
    try {
      newFont = Font.createFont(Font.TRUETYPE_FONT, is);
    } catch (IOException ex) {
      System.err.println("Cannot create font.");
    } catch (FontFormatException ex) {
      System.err.println("Invalid font format.");
    }
    return newFont;
  }

  private FontRenderContext getContext() {
    BufferedImage b = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = b.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    FontRenderContext frc = g.getFontRenderContext();
    return frc;
  }

  /**
   * Gets a buffered image from the specified text. 
   * @param text String
   * @return BufferedImage
   */
  public BufferedImage getImage(String text) {
    int width = (int) font.getStringBounds(text, context).getWidth();
    int height = (int) font.getMaxCharBounds(context).getHeight();
    int y = (int) font.getStringBounds(text, context).getY();
    BufferedImage bitmap = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR); //BufferedImage.TYPE_4BYTE_ABGR
    Graphics2D g = bitmap.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setFont(font);
    g.drawString(text, 0, -y);
    return bitmap;
  }
}

