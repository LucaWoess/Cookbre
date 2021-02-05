package swp.luca;
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
	public Connection getConnection() throws SQLException{
		Connection con = null;
		con = DriverManager.getConnection(
				"jdbc:mysql://127.0.0.1:3306/cookbrerezeptdatenbank?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 	//DB
				"root",							//user
				"root"					//password
				);
		return con;
	}

	public ArrayList<String> readURLFile(String fileName) throws FileNotFoundException, IOException{
		ArrayList<String> URLs = new ArrayList<String>();
		String s;
		BufferedReader urlReader = new BufferedReader(new FileReader(fileName));
		while((s = urlReader.readLine()) != null){
			URLs.add(s);        
		}
		urlReader.close();
		return URLs;
	}

	public void scrapeDataFromMultipleLinks() {
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

	public void scrapeDataFromOneLink(String URL, boolean isVeggie) {
		String dishName = "";
		String dishCookingInstruction = "";
		Image dishImage = null;
		Map<String, Map<String, Object>> ingredients = new HashMap<>();
		APIWrapper apiWrapper = new APIWrapper();
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
		if(isVeggie || !isVeggie)
			try {
				addDish(getConnection(), dishName, dishCookingInstruction, dishImage, ingredients, isVeggie);
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

	public void createDatabase() throws SQLException{
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

	public void releaseConnection(Connection con) throws SQLException{ 
		if(con != null)
			con.close();
	}

	public void addDish(Connection con, String dishName, String dishCookingInstruction, Image dishImage, Map<String, Map<String, Object>> ingredients, boolean isVeggie) {
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("Insert into Gericht(Gericht_Name,Gericht_Kochanleitung,Gericht_Bild,Ist_Veggie) values('"+dishName+"','"+dishCookingInstruction+"','"+dishImage+"','"+(isVeggie?1:0)+"')");
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

	public void addDish(Connection con, String dishName, String dishCookingInstruction, Image dishImage, Map<String, Map<String, Object>> ingredients) {
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

	public void removeDish(Connection con, String dishName) { 
		try {
			Statement stmt = con.createStatement();
			ResultSet dishID = con.createStatement().executeQuery("Select Gericht_ID from Gericht where Gericht_Name ='"+dishName+"'");
			if(dishID.next()) {
				stmt.executeUpdate("Delete from Menge where Gericht_ID ='"+dishID.getInt(1)+"'");
				stmt.executeUpdate("Delete from Gericht where Gericht_ID ='"+dishID.getInt(1)+"'");	
			}
			else {
				//??
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getDishByName(Connection con, String search) {
		ArrayList<String> dishes = new ArrayList<String>();
		try {
			Statement stmt = con.createStatement();
			ResultSet dishID = con.createStatement().executeQuery("Select Gericht_ID from Gericht where Gericht_Name like '%"+search+"%'");
			ResultSet searchAmount = con.createStatement().executeQuery("Select count(Gericht_ID) from Gericht where Gericht_Name like '%"+search+"%'");
			if(dishID.next()&&searchAmount.next()) {
				System.out.println("Gerichte die Ihrer Suche entsprechen:");
				for(int i=1;i<=searchAmount.getInt(1);i++) {
					ResultSet dishName = con.createStatement().executeQuery("Select Gericht_Name from Gericht where Gericht_ID = '"+dishID.getInt(1)+"'");
					if(dishName.next()) {
						dishes.add(dishName.getString(i));
					}
				}
			}
			else {
				System.out.println("Es gibt in der Datebank kein Gericht mit diesem Namen.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dishes;

	}
	
	public ArrayList<String> showAllDishes(Connection con){
		ArrayList<String> allDishes = new ArrayList<String>();
		try {
			Statement stmt = con.createStatement();
			ResultSet allDishesRS = con.createStatement().executeQuery("Select Gericht_Name from Gericht");
			while(allDishesRS.next()) {
				allDishes.add(allDishesRS.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allDishes;
	}

	public ArrayList<Object> getDishByID(Connection con, int dishID) {
		ArrayList<Object> Dish = new ArrayList<Object>();	
		try {
			Statement stmt = con.createStatement();
			String dishName = "";
			ResultSet dishNameRS = con.createStatement().executeQuery("Select Gericht_Name from Gericht where Gericht_ID ='"+dishID+"'");
			if (dishNameRS.next()) dishName = dishNameRS.getString(1);
			String dishCookingInstruction = "";
			ResultSet dishCookingInstructionRS = con.createStatement().executeQuery("Select Gericht_Kochanleitung from Gericht where Gericht_ID ='"+dishID+"'");
			if (dishCookingInstructionRS.next()) dishCookingInstruction = dishCookingInstructionRS.getString(1);
			Blob dishImage = null;
			ResultSet dishImageRS = con.createStatement().executeQuery("Select Gericht_Kochanleitung from Gericht where Gericht_ID ='"+dishID+"'");
			if (dishImageRS.next()) dishImage = dishImageRS.getBlob(1);
			Map<String, Map<String, Object>> ingredients = new HashMap<>();
			ResultSet ingredientRS = con.createStatement().executeQuery("Select Zutat_ID from Menge where Gericht_ID ='"+dishID+"'");
			ArrayList<Integer> ingredientIDs = new ArrayList<Integer>(); 
			while(ingredientRS.next()) {
			ingredientIDs.add(ingredientRS.getInt(1));
			}
			for(int ingredientID: ingredientIDs) {
				ResultSet ingredientNameRS = con.createStatement().executeQuery("Select Zutat_Name from Zutat where Zutat_ID ='"+ingredientID+"'");
				String ingredient = "";
				if(ingredientNameRS.next()) ingredient = ingredientNameRS.getString(1);
				ingredients.put(ingredient, new HashMap<String, Object>());
				ResultSet ingredientAmountRS = con.createStatement().executeQuery("Select Menge from Menge where Zutat_ID ='"+ingredientID+"'");
				if(ingredientAmountRS.next()) ingredients.get(ingredient).put("amount", ingredientAmountRS.getInt(1));
				ResultSet ingredientUnitRS = con.createStatement().executeQuery("Select Einheit from Zutat where Zutat_ID ='"+ingredientID+"'");
				if(ingredientUnitRS.next()) ingredients.get(ingredient).put("unit", ingredientUnitRS.getString(1));	
			}
			Dish.add(dishName);
			Dish.add(dishCookingInstruction);
			Dish.add(dishImage);
			Dish.add(ingredients);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Dish;
	}
	/*public static void getDishByIngredient() {

	}*/
}