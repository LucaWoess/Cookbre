import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}