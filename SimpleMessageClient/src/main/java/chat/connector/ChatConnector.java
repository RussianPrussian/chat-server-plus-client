package chat.connector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import chat.observable.ObservableMediator;
import chat.observable.ObservableMessage;

//Listens to changes on send message property
public class ChatConnector implements Runnable, PropertyChangeListener{
	
	private BufferedReader serverFeed;
	private PrintWriter writerToServer;
	Socket socket;
	private ObservableMessage mostRecentServerMessage;
	private static final String hostName = "yolo-message-service.us-east-2.elasticbeanstalk.com";
	
	public ChatConnector() throws UnknownHostException, IOException {
		InetAddress address = InetAddress.getByName(hostName);
		socket = new Socket(address, 8091);
		serverFeed = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writerToServer = new PrintWriter(socket.getOutputStream());
		mostRecentServerMessage = ObservableMediator.getObservableMessage("mostRecentServerMessage");
		ObservableMediator.registerWithListener("messageToSend", this);
	}

	@Override
	public void run() {
		
		try {
			while(true) {
					if(serverFeed.ready()) {
						mostRecentServerMessage.setMessage(serverFeed.readLine());
					}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverFeed.close();
				socket.close();
				writerToServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String messageToWriteToServer = (String) evt.getNewValue();
		writerToServer.println(messageToWriteToServer);
		writerToServer.flush();
	}

}
