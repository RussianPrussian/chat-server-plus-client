import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {
	
	private static Lock lock = new ReentrantLock();
	private static Connection conn;
	
	public DatabaseManager() throws SQLException {
		conn = initiateConnection();
		conn.createStatement().execute("CREATE TABLE users ( id BIGINT AUTO_INCREMENT NOT NULL, userName VARCHAR(50) NOT NULL, hashpass VARCHAR(100));");
		conn.createStatement().executeQuery("Select * from users");
		conn.commit();
	}
	
	private Connection initiateConnection() throws SQLException {
		return DriverManager.getConnection ("jdbc:h2:mem:test", "sa", ""); 
	}
	
	public boolean insertUser(String user, String password) throws SQLException {
		String hashPass = BCrypt.hashpw(password, BCrypt.gensalt());
		lock.lock();
		if(!checkUserNameAvailability(user)) {
			lock.unlock();
			return false;
		}
		PreparedStatement statement = conn.prepareStatement("Insert into users (userName, hashpass) values (?, ?)");
		statement.setString(1, user);
		statement.setString(2, hashPass);
		statement.execute();
		lock.unlock();
		return true;
	}
	
	private boolean checkUserNameAvailability(String user) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("select userName from users where userName = ?");
		statement.setString(1, user);
		ResultSet results = statement.executeQuery();
		boolean userNameAvailable = !results.next();
		return userNameAvailable;
	}
	
	public boolean logIn(String user, String password) throws SQLException {
		lock.lock();
		Connection conn = initiateConnection();
		PreparedStatement statement = conn.prepareStatement("select userName, hashPass from users where userName = ?");
		statement.setString(1, user);
		ResultSet results = statement.executeQuery();
		if(!results.next()) {
			lock.unlock();
			return false;
		}
		String hashedPass = results.getString("hashPass");
		lock.unlock();
		return BCrypt.checkpw(password, hashedPass);
	}

}
