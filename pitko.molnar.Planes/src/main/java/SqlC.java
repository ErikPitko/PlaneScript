import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlC {
	private Connection c;
	private Statement stmt = null;
	private ResultSet rs = null;

	public SqlC() {
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:Database.db");
			stmt = c.createStatement();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}

	public void insert(String query) throws SQLException {
//		System.out.println("sqlMessage(Debug): " + query);
		stmt.executeUpdate(query);
	}

	// "SELECT * FROM COMPANY;"
	public ResultSet select(String query) throws SQLException {
		rs = stmt.executeQuery(query);
		return rs;

	}

	// "UPDATE COMPANY set SALARY = 25000.00 where ID=1;"
	public void update(String query) throws SQLException {
		stmt.executeUpdate(query);
	}
	
	public int count(String table) throws SQLException{
		int count = 0;
		rs = stmt.executeQuery("SELECT * FROM '" + table + "';");
		while(rs.next()){
			count +=1;
		}
		return count;
		
	}

	// "DELETE from COMPANY where ID=2;"
	public void delete(String query) throws SQLException {
		stmt.executeUpdate(query);

	}

	public void close() throws SQLException {
		stmt.close();
		c.close();
	}

}
