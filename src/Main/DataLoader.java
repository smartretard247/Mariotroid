package Main;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Jeezy
 * DataLoader accepts a file name and will load each line of the file
 * into an ArrayList of strings.  Use getData to retrieve array after load.
 */
public class DataLoader {
    private ArrayList<String> data;
    
    public DataLoader() {
        data = new ArrayList<>();
    }
    public DataLoader(URL fileName) {
        data = new ArrayList<>();
        loadFromFile(fileName);
    }
    public DataLoader(String fileName) {
        data = new ArrayList<>();
        loadFromFile(fileName);
    }
    public DataLoader(File file) {
        data = new ArrayList<>();
        loadFromFile(file);
    }
    public DataLoader(Scanner sc) {
        data = new ArrayList<>();
        loadFromFile(sc);
    }
    
    public String getLine(int index) {
        return data.get(index);
    }
    public void addLine(String toAdd) {
        data.add(toAdd);
    }
    public void removeLine(String toRemove) {
        data.remove(toRemove);
    }
    public void removeLine(int index) {
        data.remove(index);
    }
    public void clearData() {
        data.clear();
    }
    public boolean isEmpty() {
        return data.isEmpty();
    }
    public int getDataSize() { //returns the total amount of lines in the file
        return data.size();
    }
    
    public ArrayList<String> getData() {
        return data;
    }
    public void setData(ArrayList<String> data) {
        this.data = data;
    }
    
    //get one string with all ArrayList entries
    public String getConcatenatedData() {
        String st = "";
        return getData().stream().map((line) -> line + "\n").reduce(st, String::concat);
    }
    
    public final boolean loadFromFile(String fileName) {
        File file = new File(fileName);
        return loadFromFile(file);
    }
    
    public final boolean loadFromFile(File file) {
        try {
            Scanner sc = new Scanner(file);
            loadFromFile(sc);
            return true;
        } catch(FileNotFoundException ex) {
            System.out.println("DataLoader could not find file.");
            return false;
        }
    }
    
    public final void loadFromFile(Scanner sc) {
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if(line.startsWith("/")) continue;
            if(!line.equals("\r") && !line.equals("")) { //do not add empty lines
                while(line.contains("  ")) { //loop to remove extra internal spaces
                    line = line.replace("  ", " ");
                }
                addLine(line);
            }
        }
    }
    
    public final boolean loadFromFile(URL fileName) {
      File file = new File(fileName.getFile());
      return loadFromFile(file);
    }
    
    public final boolean saveToFile(String fileName) {
      PrintWriter thePrintWriter;

      try {
        thePrintWriter = new PrintWriter(fileName);

        getData().stream().forEach((st) -> {
            thePrintWriter.println(st + "\n");
        });

        thePrintWriter.close();
      } catch (IOException ex) {
        System.out.println("DataLoader could not save to file: " + fileName);
        return false;
      }

      return true;
    }

  
}
