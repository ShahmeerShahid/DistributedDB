package jdbc;

import java.io.File;

/**
* This is initialization script for the Database required by URLShortner.
*
* @author  Ali Raza, Jefferson Zhong, Shahmeer Shahid
* @version 1.0
*/
public class DBInit {

  /**
   * This main method initializes the Database required by a node URLShortner.
   * @param args Unused.
   * @return Nothing.
   */
  public static void main(String args[]) {

    String PATH = String.format("/virtual/%s/URLShortner", System.getProperty("user.name"));

    File directory = new File(PATH);
    if (!directory.exists()) {
      directory.mkdirs();
    }

    try {

      DBHandler dbHandler = new DBHandler(String.format("%s/urlshortner.db", PATH));
      System.out.println("short = " + "gg" + " long = " + dbHandler.findURL("gg"));
      dbHandler.getConnection().close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Operation done successfully");
  }
}
