import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CookbreMain {
	public static void main(String[] args) throws IOException {

		try {
			DBManager.createDatabase();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		DBManager.scrapeDataFromMultipleLinks();
		
		try {
			DBManager.removeDish(DBManager.getConnection());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//DBManager.scrapeDataFromOneLink();
	}
}



