package Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;
import java.util.LinkedList;
import Drawing.DrawLib;
import com.jogamp.opengl.GL2;

/**
 * TEST CLASS
 * Provides functionality to display test info on screen and saved as a log.
 * 
 * @author Nate
 */
public class TestDisplay {
    
    private static final LinkedList<TestData> DISP_DATA = new LinkedList<>();;
    private static final double[] COLOR = {1.0, 0.5, 0.0};
    private static final double SPACE = 30;
    private static final double X_POS = 450;
    private static final double Y_POS = 600;
    private static String logText;
    
    private File f;
    private FileWriter fw;
    
    public TestDisplay(){
        logText = "";
        String docPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        String fileName = "Test";
        int fileNumber = 1;
        String fileExt = ".txt";
        String filePath;
        boolean fileExists = true;
        do{
            filePath = docPath + "\\" + fileName + fileNumber + fileExt;
            f = new File(filePath);
            if(f.exists()) {
                fileNumber++;
            }else{
                fileExists = false;
            }
        }while(fileExists);
    }
    
    /**
     * Add test data to log for display on screen and to write to the test log
     * @param dataString: Data to add
     */
    public static void addTestData(String dataString){
        logText += dataString + "\r\n";
        DISP_DATA.add(new TestData(dataString));
    }
    
    /**
     * Writes the test data that hasn't timed out to the screen.
     */
    public static void writeToScreen(GL2 gl){
        double yPos = Y_POS;
        boolean go = true;
        while(go && DISP_DATA.size() > 0){
            if(DISP_DATA.peek().isExpired() || DISP_DATA.size() > 7){
                DISP_DATA.pop();
            }else{
                go = false;
            }
        }
        gl.glPushMatrix();
        gl.glTranslated(0, -DrawLib.getTexture(DrawLib.TEX_HUD).getHeight()/2, 0);
        for(TestData td : DISP_DATA){
            DrawLib.drawText(td.getTestData(), COLOR, X_POS, yPos);
            yPos -= SPACE;
        }
        gl.glPopMatrix();
    }
    
    /**
     * Writes the current testResults to the fileWriter to save results
     */
    public void writeToFile(){
        try{
            fw = new FileWriter(f);
            fw.write(logText);
            fw.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
