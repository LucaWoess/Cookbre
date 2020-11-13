import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	public static Connection getConnection() throws ClassNotFoundException, SQLException{
		Connection con = null;
		Class.forName("com.mysql.cj.jdbc.Driver");
		con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306", 	//DB
				"root",							//user
				""								//password
				);
		return con;
	}
}