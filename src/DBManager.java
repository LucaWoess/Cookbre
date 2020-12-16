import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;

import java.io.IOException;
import java.sql.Blob;

public class DBManager {
	public static Connection getConnection() throws SQLException{
		Connection con = null;
		con = DriverManager.getConnection(
				"jdbc:mysql://127.0.0.1:3306/cookbrerezeptdatenbank?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 	//DB
				"Luca",							//user
				"lucamysql127"					//password
				);
		return con;
	}
	
	public static void scrapeData() {
		String dishName = "";
		String dishCookingInstruction = "";
		Blob dishImage = null;
		boolean istVeggie = false;
		int tempvar = -1;
		String URL;
		Map<String, Map<String, Object>> ingredients = new HashMap<>();
		APIWrapper apiWrapper = new APIWrapper();
		Scanner Sc = new Scanner(System.in);
		System.out.print("Fügen sie hier den Link des gwünschten neuen Rezepts ein: ");
		URL = Sc.nextLine();
		System.out.print("\nIst dieses Gericht vegetarisch? JA(0) NEIN(1) KEINE AHNUNG(2): ");
		boolean ok;
		do {
			ok = true;
			try {
				Scanner S = new Scanner(System.in);
				tempvar = S.nextInt();
				}
			catch (InputMismatchException ex) {
				ok = false;
				}
			if(tempvar<0||tempvar>2) {
				ok = false;
			}
			if(!ok) System.out.println("Dies ist keine der 3 Auswahlmöglichkeiten! \nVersuchen Sie es erneut: ");
		}
		while(!ok);

		switch (tempvar){
			case 0:
				istVeggie = true;
				break;
				
			case 1: 
				istVeggie = false;
				break;
				
			case 2: 
				break;
		}
		
		try {
			String response = apiWrapper.getRecipe(URL).body().string();
			JSONArray array = new JSONArray(apiWrapper.getRecipe(URL).body().string());
			System.out.println(response);
			dishName = (String) (array.getJSONObject(0).get("name"));
			dishImage = (Blob) (array.getJSONObject(0).get("images"));
			dishCookingInstruction = array.getJSONObject(0).getJSONArray("ingredients").toString();
			String ingredient = "";
			for (Object object : array.getJSONObject(0).getJSONArray("ingredients")) {
				String[] arrOfStr = ((String) object).split(" ", 2);
				ingredient = arrOfStr[2];
			ingredients.put(ingredient, new HashMap<String, Object>());
			ingredients.get(ingredient).put("amount", arrOfStr[0]);
			ingredients.get(ingredient).put("unit", arrOfStr[1]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		if(istVeggie || !istVeggie)
			try {
				addDish(getConnection(), dishName, dishCookingInstruction, dishImage, ingredients, istVeggie);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		else
			try {
				addDish(getConnection(), dishName, dishCookingInstruction, dishImage, ingredients);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	public static void createDatabase() throws SQLException{
		Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
				"Luca",
				"lucamysql127");
		Statement stmt = con.createStatement();
		stmt.executeUpdate("Drop Database if exists cookbrerezeptdatenbank;");
		stmt.executeUpdate("create database cookbrerezeptdatenbank;");
		stmt.executeUpdate("use cookbrerezeptdatenbank;");
		stmt.executeUpdate("Create table Gericht ( Gericht_ID int auto_increment primary key, "
				+ "Gericht_Name varchar(50) not null, "
				+ "Gericht_Kochanleitung text not null, "
				+ "Gerich_Bild blob, "
				+ "Ist_Veggie boolean not null);"
				);
		stmt.executeUpdate("Create table Zutat (" 
				+ "Zutat_ID int auto_increment primary key," 
				+ "Zutat_Name varchar(50) not null," 
				+ "Einheit varchar(25)not null);"
				);
		stmt.executeUpdate("Create table Menge (" 
				+ "Gericht_ID int not null," 
				+ "Zutat_ID int not null," 
				+ "Menge double not null);"
				);
		stmt.executeUpdate("alter table Menge add constraint Gericht_ID foreign key (Gericht_ID) references Gericht(Gericht_ID);");
		stmt.executeUpdate("alter table Menge add constraint Zutat_ID foreign key (Zutat_ID) references Zutat(Zutat_ID);");
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
	
	public static void addDish(Connection con, String dishName, String dishCookingInstruction, Blob dishImage, Map<String, Map<String, Object>> ingredients) {
		/*try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("Insert into Gericht(Gericht_Name,Gericht_Kochanleitung,dishImage,Ist_Veggie) values('"+dishName+"','"+dishCookingInstruction+"','"+dishImage+"','null')");
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