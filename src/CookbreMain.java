import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CookbreMain {
	public static void main(String[] args) throws IOException {
		APIWrapper apiWrapper = new APIWrapper();
		
		try {
			DBManager.createDatabase();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		try {
			Connection con = DBManager.getConnection();
			try {
				String response = apiWrapper.getRecipe().body().string();
				JSONArray array = new JSONArray(apiWrapper.getRecipe().body().string());
				System.out.println(response);
				System.out.println(array.getJSONObject(0).get("name"));
				System.out.println(array.getJSONObject(0).get("images"));
				for (Object object : array.getJSONObject(0).getJSONArray("ingredients")) {
					System.out.println(object);
					}
				System.out.println(array.getJSONObject(0).get("instructions"));
				}
			catch (JSONException e) {
				e.printStackTrace();
				DBManager.releaseConnection(con);
				} 
			}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}



