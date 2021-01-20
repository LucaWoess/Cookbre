import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;

import java.awt.Image;
import java.awt.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Blob;

public class DBManager {
	public static Connection getConnection() throws SQLException{
		Connection con = null;
		con = DriverManager.getConnection(
				"jdbc:mysql://127.0.0.1:3306/cookbrerezeptdatenbank?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 	//DB
				"root",							//user
				"root"					//password
				);
		return con;
	}

	public static ArrayList<String> readURLFile(String fileName) throws FileNotFoundException, IOException{
		ArrayList<String> URLs = new ArrayList<String>();
		String s;
		BufferedReader urlReader = new BufferedReader(new FileReader(fileName));
		while((s = urlReader.readLine()) != null){
			URLs.add(s);        
		}
		urlReader.close();
		return URLs;
	}

	public static void scrapeDataFromMultipleLinks() {
		try {
			for (String URL : readURLFile("URLs.txt")) {
				String dishName = "";
				String dishCookingInstruction = "";
				Image dishImage = null;
				Map<String, Map<String, Object>> ingredients = new HashMap<>();
				APIWrapper apiWrapper = new APIWrapper();
				try {
					String response = apiWrapper.getRecipe(URL).body().string();
					JSONArray array = new JSONArray(apiWrapper.getRecipe(URL).body().string());
					dishName = (String) (array.getJSONObject(0).get("name"));
					try {
						URL url = new URL(array.getJSONObject(0).get("images").toString()
								.replace('"', ' ')
								.replace('[', ' ')
								.replace(']', ' ')
								.trim());
						dishImage = ImageIO.read(url);
					} 
					catch (IOException e) {
					}
					dishCookingInstruction = array.getJSONObject(0).getJSONArray("instructions").getJSONObject(0).getJSONArray("steps").join(" ").replaceAll("\"","");
					String ingredient = "";
					for (Object object : array.getJSONObject(0).getJSONArray("ingredients")) {
						String[] arrOfStr = ((String) object).split(" ", 3);
						ingredient = arrOfStr[2];
						ingredients.put(ingredient, new HashMap<String, Object>());
						ingredients.get(ingredient).put("amount", arrOfStr[0]);
						ingredients.get(ingredient).put("unit", arrOfStr[1]);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					addDish(getConnection(), dishName, dishCookingInstruction, dishImage, ingredients);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void scrapeDataFromOneLink() {
		String dishName = "";
		String dishCookingInstruction = "";
		Image dishImage = null;
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
			try {
				URL url = new URL(array.getJSONObject(0).get("images").toString()
						.replace('"', ' ')
						.replace('[', ' ')
						.replace(']', ' ')
						.trim());
				dishImage = ImageIO.read(url);
			} 
			catch (IOException e) {
			}
			dishCookingInstruction = array.getJSONObject(0).getJSONArray("instructions").getJSONObject(0).getJSONArray("steps").join(" ").replaceAll("\"","");
			String ingredient = "";
			for (Object object : array.getJSONObject(0).getJSONArray("ingredients")) {
				String[] arrOfStr = ((String) object).split(" ", 3);
				System.out.println(arrOfStr[0]);
				System.out.println(arrOfStr[1]);
				System.out.println(arrOfStr[2]);
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
		Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/cookbrerezeptdatenbank?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
				"root",
				"root");
		Statement stmt = con.createStatement();
		stmt.executeUpdate("Drop Database if exists cookbrerezeptdatenbank;");
		stmt.executeUpdate("create database cookbrerezeptdatenbank;");
		stmt.executeUpdate("use cookbrerezeptdatenbank;");
		stmt.executeUpdate("Create table Gericht ( Gericht_ID int auto_increment primary key, "
				+ "Gericht_Name varchar(50) not null, "
				+ "Gericht_Kochanleitung text not null, "
				+ "Gericht_Bild blob, "
				+ "Ist_Veggie boolean);"
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

	public static void addDish(Connection con, String dishName, String dishCookingInstruction, Image dishImage, Map<String, Map<String, Object>> ingredients, boolean istVeggie) {
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("Insert into Gericht(Gericht_Name,Gericht_Kochanleitung,Gericht_Bild,Ist_Veggie) values('"+dishName+"','"+dishCookingInstruction+"','"+dishImage+"','"+(istVeggie?1:0)+"')");
			for(String ingredient : ingredients.keySet()) {
				ResultSet ingredientID1 = con.createStatement().executeQuery("Select Zutat_ID from Zutat where Zutat_Name ='"+ingredient+"'");
				if(!ingredientID1.next()) {
					stmt.executeUpdate("Insert into Zutat(Zutat_Name,Einheit) values('"+ingredient+"','"+(ingredients.get(ingredient).get("unit"))+"')");			
				}
				ResultSet dishID = con.createStatement().executeQuery("Select Gericht_ID from Gericht where Gericht_Name ='"+dishName+"'");
				ResultSet ingredientID2 = con.createStatement().executeQuery("Select Zutat_ID from Zutat where Zutat_Name ='"+ingredient+"'");
				if(ingredientID2.next()&&dishID.next()) {
					stmt.executeUpdate("Insert into Menge(Gericht_ID,Zutat_ID,Menge) values('"+dishID.getInt(1)+"','"+ingredientID2.getInt(1)+"','"+(ingredients.get(ingredient).get("amount"))+"')");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void addDish(Connection con, String dishName, String dishCookingInstruction, Image dishImage, Map<String, Map<String, Object>> ingredients) {
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("Insert into Gericht(Gericht_Name,Gericht_Kochanleitung,Gericht_Bild) values('"+dishName+"','"+dishCookingInstruction+"','"+dishImage+"')");
			for(String ingredient : ingredients.keySet()) {
				ResultSet ingredientID1 = con.createStatement().executeQuery("Select Zutat_ID from Zutat where Zutat_Name ='"+ingredient+"'");
				if(!ingredientID1.next()) {
					stmt.executeUpdate("Insert into Zutat(Zutat_Name,Einheit) values('"+ingredient+"','"+(ingredients.get(ingredient).get("unit"))+"')");			
				}
				ResultSet dishID = con.createStatement().executeQuery("Select Gericht_ID from Gericht where Gericht_Name ='"+dishName+"'");
				ResultSet ingredientID2 = con.createStatement().executeQuery("Select Zutat_ID from Zutat where Zutat_Name ='"+ingredient+"'");
				if(ingredientID2.next()&&dishID.next()) {
					stmt.executeUpdate("Insert into Menge(Gericht_ID,Zutat_ID,Menge) values('"+dishID.getInt(1)+"','"+ingredientID2.getInt(1)+"','"+(ingredients.get(ingredient).get("amount"))+"')");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void removeDish(Connection con) { 
		Scanner Sc = new Scanner(System.in);
		System.out.print("Wie heißt das Gericht, dass Sie löschen möchten: ");
		String dishName = Sc.nextLine();
		try {
			Statement stmt = con.createStatement();
			ResultSet dishID = con.createStatement().executeQuery("Select Gericht_ID from Gericht where Gericht_Name ='"+dishName+"'");
			if(dishID.next()) {
				stmt.executeUpdate("Delete from Menge where Gericht_ID ='"+dishID.getInt(1)+"'");
				stmt.executeUpdate("Delete from Gericht where Gericht_ID ='"+dishID.getInt(1)+"'");	
			}
			else {
				System.out.println("Es gibt in der Datebank kein Gericht mit diesem Namen.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}