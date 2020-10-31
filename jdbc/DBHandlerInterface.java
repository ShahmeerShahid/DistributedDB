package jdbc;

import java.sql.SQLException;

/**
* This is an interface for DBHandler for URLShortner.
*
* @author  Ali Raza, Jefferson Zhong, Shahmeer Shahid
* @version 1.0
*/
public interface DBHandlerInterface {

  /**
    * find a shortURL return the corresponding long.
    *
    * @param shortURL
    * @param longURL
    * @throws SQLException
    */
  public void saveURL(String shortURL, String longURL) throws SQLException;

  /**
     * find a shortURL return the corresponding long.
     *
     * @param shortURL
     * @throws SQLException
     * @return corresponding long
     */
  public String findURL(String shortURL) throws SQLException;

  /**
     * Get the short count in the DB.
     *
     * @throws SQLException
     */
  public int dbCount() throws SQLException;

  /**
     * Delete a shortURL
     *
     * @param shortURL
     * @throws SQLException
     */
  public void delete(String shortURL) throws SQLException;
}