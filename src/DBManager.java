import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.sql.Blob;

public class DBManager {
	public static Connection getConnection() throws SQLException{
		Connection con = null;
		con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306", 	//DB
				"root",							//user
				""								//password
				);
		return con;
	}
	
	public static void releaseConnection(Connection con) throws SQLException{ 
			if(con != null)
				con.close();
	}
	
	public static void addDish(Connection con, String dishName, String dishCookingInstruction, Blob dishImage, Map<String, Map<String, Object>> ingredients, boolean istVeggie) {
		/*try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("Insert into Gericht(Gericht_Name,Gericht_Kochanleitung,dishImage,Ist_Veggie) values('"+dishName+"','"+dishCookingInstruction+"','"+dishImage+"','"+istVeggie+"')");
			for(String ingredient : ingredients.keySet()) {
			stmt.executeUpdate("Insert into Zutat(Zutat_Name) values('"+((Map)ingredients.get(ingredient))+"')");
			int ingredientID = stmt.executeUpdate("Select Zutat_ID from table Zutat where Zutat_Name ='"+((Map)ingredients.get(ingredient))+"'");
			int dishID = stmt.executeUpdate("Select Gericht_ID from table Gericht where Gericht_Name ='"+dishName+"'");
			stmt.executeUpdate("Insert into Menge(Zutat_ID,Zutat_Namem,Einheit) values('"+ingredientID+"','"+dishID+"','"+((Map)ingredients.get(ingredient)).get("unit")+"')");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
	
	public static void addDish(Connection con, String dishName, String dishCookingInstruction, Map<String, Map<String, Object>> ingredients, boolean istVeggie) {
		/*try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("Insert into Gericht(Gericht_Name,Gericht_Kochanleitung,dishImage,Ist_Veggie) values('"+dishName+"','"+dishCookingInstruction+"','null','"+istVeggie+"')");
			for(String ingredient : ingredients.keySet()) {
			stmt.executeUpdate("Insert into Zutat(Zutat_Name) values('"+((Map)ingredients.get(ingredient))+"')");
			int ingredientID = stmt.executeUpdate("Select Zutat_ID from table Zutat where Zutat_Name ='"+((Map)ingredients.get(ingredient))+"'");
			int dishID = stmt.executeUpdate("Select Gericht_ID from table Gericht where Gericht_Name ='"+dishName+"'");
			stmt.executeUpdate("Insert into Menge(Zutat_ID,Zutat_Namem,Einheit) values('"+ingredientID+"','"+dishID+"','"+((Map)ingredients.get(ingredient)).get("unit")+"')");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
	
	public static void removeDish(Connection con) {
		/*try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("Delete from Gericht where /");
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
}