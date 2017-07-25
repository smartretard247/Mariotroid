package Test;

import java.time.Instant;

/**
 * TEST CLASS
 * Test data and timer for that data
 * 
 * @author Nate
 */
public class TestData {
    private final Instant time;
    private final String testData;
    private boolean isViewable;
    private final int TIME_LIMIT = 10;
    
    public TestData(String data) {
      time = Instant.now();
      testData = data;
      isViewable = true;
    }
    
    /**
     * Gets testData
     * @return String of test data
     */
    public String getTestData(){
      return testData;
    }
    
    /**
     * Checks to see if data has passed its time limit
     * @return true if data has passed its time limit
     */
    public boolean isExpired(){
      if(Instant.now().getEpochSecond() - time.getEpochSecond() > TIME_LIMIT) {
        isViewable = false;
      }
      return !isViewable;
    }
}
