import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class SimpleMessageService {
	
	private static Map<String, ChatGuest> loggedInUsers = new ConcurrentHashMap<String, ChatGuest>();
	private static ExecutorService threadPool;
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		System.out.println("Setting up database..");
		threadPool = Executors.newCachedThreadPool();
		Class.forName ("org.h2.Driver");
		DatabaseManager manager = null;
		try {
			manager = new DatabaseManager();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Database up and running.");
		System.out.println("Setting up server to listen on 8091.");
		ServerSocket listener = new ServerSocket(8091);
		try {
			while(true) {
				Socket sock = listener.accept();
				threadPool.submit(new ChatGuest(sock, manager));
			}
		} catch (IOException e) {
		    if (listener != null && !listener.isClosed()) {
		    	listener.close();
		    }
		}
	}
	
	public static ChatGuest findPartner(String userName) {
		synchronized(loggedInUsers) {
			if(loggedInUsers.containsKey(userName)) {
				return loggedInUsers.get(userName);
			}
		}
		return null;
	}
	
	public static void addChatGuestToLoggedInMap(ChatGuest chatGuest) {
		synchronized(loggedInUsers) {
			loggedInUsers.put(chatGuest.getUserName(), chatGuest);
		}
	}

	public static void deregister(ChatGuest guest) {
		synchronized(loggedInUsers) {
			loggedInUsers.remove(guest.getUserName());
		}
	}
	
	public static Future<String> startRead(ReadInput reader){
		return threadPool.submit(reader);
	}
}
