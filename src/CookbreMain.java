import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CookbreMain {
	public static void main(String[] args) {
		APIWrapper apiWrapper = new APIWrapper();
		try {
			Connection con = DBManager.getConnection();
			try {
				String response = apiWrapper.getRecipe().body().string();
				JSONArray array = new JSONArray(apiWrapper.getRecipe().body().string());
				//System.out.println(array.getJSONObject(0).get("name"));
				System.out.println(response);
				/*for (Object object : array.getJSONObject(0).getJSONArray("ingredients")) {
					System.out.println(object);
				}*/
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DBManager.releaseConnection(con);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
