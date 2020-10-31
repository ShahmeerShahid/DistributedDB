package urlshortner;

import java.sql.SQLException;

import jdbc.DBHandler;

class URLDataAccessObject {

	DBHandler dbHandler;

	public URLDataAccessObject(DBHandler db) {
		this.dbHandler = db;
	}

	String find(String shortURL) {

		try {
			return dbHandler.findURL(shortURL);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	void save(String shortURL, String longURL) {
		try {
			dbHandler.saveURL(shortURL, longURL);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
