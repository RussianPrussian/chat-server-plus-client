import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatGuest implements Runnable {
	
	private static final int READ_TIMEOUT = 5;
	
	private final Socket socket;
	private final BufferedReader reader;
	private final PrintWriter writer;
	private String userName;
	private volatile String mostRecentMessage = "";
	private ChatGuest partnerMessageHandler;
	private DatabaseManager dbManage;
	private ReadInput readerCallable;
	private Lock readerFinishedLock;
	
	/**
	 * Flag indicates that this guest has a pending request from another guest
	 */
	private AtomicBoolean outstandingChatRequest;
	/**
	 * Lock+condition enables mutual exclusion between prompting this guest to request a partner and prompting this guest
	 * to accept a partner
	 */
	private Lock lock;
	private Condition partneredUpCondition;

	public ChatGuest(Socket socket, DatabaseManager dbManage) throws IOException {
		super();
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream(), true);
		outstandingChatRequest = new AtomicBoolean(false);
		lock = new ReentrantLock();
		partneredUpCondition = lock.newCondition();
		this.dbManage = dbManage;
		readerFinishedLock = new ReentrantLock();
		readerCallable = new ReadInput(reader, readerFinishedLock);
	}
	
	/**
	 * The target of this method is asked to pair with with the parameter ChatGuest. Returns true if pairing
	 * was successful (i.e. this guest accepted the request and nothing else went wrong)
	 * @throws GuestDisconnectedException 
	 */
	public boolean requestPairing(ChatGuest guestRequestFrom) throws IOException, GuestDisconnectedException {
		boolean pairSuccessful = false;
		if(partnerMessageHandler == null && guestRequestFrom != null) {
			this.outstandingChatRequest.set(true);
			writer.println(guestRequestFrom.getUserName() + " would like to talk to you. Accept? (Y/N) ");
			writer.flush();
			boolean finishedRequesting = false;

			lock.lock();
			try {
				while(!finishedRequesting) {
					String acceptResponse = null;
					if(!readerCallable.readCompleted()) {
						while(!readerCallable.readCompleted() || acceptResponse == null) {
							acceptResponse = readerCallable.getMessage();//keep checking queue until we have a response
						}
					}
					
					if(acceptResponse == null && reader.ready()) {
						acceptResponse = reader.readLine();//block for response ((we should change this to a similar time out);
					}
						
					if(Objects.equals(acceptResponse, "Y")) {
						this.partnerMessageHandler = guestRequestFrom;
						this.outstandingChatRequest.set(false);
						finishedRequesting = true;
						pairSuccessful=true;
					}else if(Objects.equals(acceptResponse, "N")) {
						this.outstandingChatRequest.set(false);
						finishedRequesting = true;
						pairSuccessful=false;
					}
				
					//Check to see if this guest is online; if not -> fail pairing
					//also check to see that the requesting partner is online and fail if they're not
					try {
						this.ping();
						guestRequestFrom.ping();
					}catch(IOException e) {
						this.outstandingChatRequest.set(false);
						this.writer.println("Looks like pairing failed. " + guestRequestFrom.userName + " may have disconnected.");
						finishedRequesting = true;
						pairSuccessful = false;
					}
				}
			}finally {
				partneredUpCondition.signalAll();
				lock.unlock();
			}
		}
		return pairSuccessful;
	}
	
	public void sendMessage(String message, String sourceUserName) {
		if(message != null) {
			writer.println(sourceUserName + ": " + message);
			writer.flush();
		}
	}
	
	private boolean requestUserName() throws IOException, SQLException {
		writer.println("Please set up a user name: ");
		writer.flush();
		userName = reader.readLine();
		writer.println("Please choose a password: ");
		writer.flush();
		String password = reader.readLine();
		writer.println("Please retype the password: ");
		writer.flush();
		String passwordRetype = reader.readLine();
		return password.equals(passwordRetype) && dbManage.insertUser(userName, password);
	}

	private boolean logIn() throws IOException, SQLException {
		writer.println("Please enter your userName: ");
		writer.flush();
		userName = reader.readLine();
		writer.println("Please enter your a password: ");
		writer.flush();
		String password = reader.readLine();
		if(dbManage.logIn(userName, password)) {
			return true;
		};
		return false;
	}
	
	private void initiateChatStream() throws IOException, PartnerDisconnectedException {
		writer.println("Start Chatting with your partner!");
		writer.flush();
		
		while(this.partnerMessageHandler!=null && !"EXIT".equals(mostRecentMessage)) {
			try {
				readerFinishedLock.lock();
				if(readerCallable.readCompleted()) {
					try {
						mostRecentMessage = readerCallable.getMessage();//clear queue (may have a message from last read)
					}catch(IllegalStateException e) {
						e.printStackTrace();
					}
					
					if(mostRecentMessage == null) {
						try {
							mostRecentMessage = SimpleMessageService.startRead(readerCallable).get(READ_TIMEOUT, TimeUnit.SECONDS);
							if(mostRecentMessage != null) {
								mostRecentMessage = readerCallable.getMessage();//guaranteed no read started, so clear the queue
							}
						}catch(IllegalStateException | ExecutionException e) {
							e.printStackTrace();
						}
					}
					
					if(partnerMessageHandler != null && mostRecentMessage != null) {
						partnerMessageHandler.sendMessage(mostRecentMessage, this.userName);
					}else {
						throw new TimeoutException();
					}
				}
				readerFinishedLock.unlock();
			}catch(TimeoutException e) {
				try{
					socket.getOutputStream().write(0);
				}catch(IOException ioe){
					System.out.println("Could not write because connection was closed.");
					unpair();//partner is now null;
					throw new PartnerDisconnectedException();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void promptUserForPartnerAndPair() throws IOException, GuestDisconnectedException {
		boolean requestRejected = false;
		do {
			writer.println("Please type the user name of your partner: ");
			writer.flush();
			String partnerName = "";
			while(true) {
				lock.lock();
				try {
					if(reader.ready()) {
						partnerName = reader.readLine();
						ChatGuest partner = SimpleMessageService.findPartner(partnerName);
						if(this.partnerMessageHandler == null && partner != null && partner.requestPairing(this)) {
							this.partnerMessageHandler = partner;
						}
						break;
					}
					
					//this thread has been interrupted with a chat request; handle that.
					while(this.outstandingChatRequest.get()) {
						partneredUpCondition.await();
						requestRejected = (this.partnerMessageHandler == null);
					}
					if(this.partnerMessageHandler != null) {
						break;
					}else if(requestRejected) {//reprompt
						writer.println("Please type the user name of your partner: ");
						writer.flush();
						requestRejected = false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}finally {
					lock.unlock();
				}
				
				try {
					this.ping();
				}catch(IOException e) {
					throw new GuestDisconnectedException(this.userName);
				}
			}
		}while(this.partnerMessageHandler == null);
	}
	
	private void unpair() {
		ChatGuest partner = this.partnerMessageHandler;
		this.partnerMessageHandler = null;
		if(partner.getPartnerMessageHandler() != null) {
			partner.unpair();
		}
	}
	
	private boolean askWhetherToContinue() throws IOException, InterruptedException {
		
		writer.println("Do you want to find another partner? (Y/N) ");
		writer.flush();
		boolean exitSet = false;
		boolean exit = true;
		String possiblePendingMessage = null;
		while(true) {
			try {
				lock.lock();
				while(this.outstandingChatRequest.get()) {
					partneredUpCondition.await();
				}
				if(this.partnerMessageHandler != null) {
					//this means that this guest accepted a partner in another thread, so no need to wait for messages anymore
					exit = true;
					break;
				}
				//handle leftover read from chat loop
				if(!readerCallable.readCompleted() || possiblePendingMessage == null) {
					possiblePendingMessage = readerCallable.getMessage();
					if(possiblePendingMessage != null) {
						exit = !"Y".equals(possiblePendingMessage);
						exitSet = true;
						break;
					}
				}else if(!exitSet && readerCallable.readCompleted()) {
					String shouldExit = "";
					exit = !"Y".equals(shouldExit = reader.readLine()) && shouldExit != null;
					break;
				}
			} finally {
				lock.unlock();
			}
		}
		return exit;
	}

	@Override
	public void run() {
		try {
			writer.println("Do you need to set up a userName? (Y/N): ");
			writer.flush();
			String setUpUserName = reader.readLine();
			if(setUpUserName.equals("Y")) {
				while(!requestUserName()) {/*repeat*/};
			}else {
				while(!logIn()) {/*repeat*/}
			}
			SimpleMessageService.addChatGuestToLoggedInMap(this);
			
			boolean exit = false;
			while(!exit) {
				promptUserForPartnerAndPair();
				initiateChatStream();
				exit = askWhetherToContinue();
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		} catch(PartnerDisconnectedException e) {
			e.printStackTrace();
		} catch(GuestDisconnectedException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		} finally {
			try {
				unpair();
				SimpleMessageService.deregister(this);
				reader.close();
				writer.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void ping() throws IOException {
		socket.getOutputStream().write(0);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public ChatGuest getPartnerMessageHandler() {
		return partnerMessageHandler;
	}

	public void setPartnerMessageHandler(ChatGuest partnerMessageHandler) {
		this.partnerMessageHandler = partnerMessageHandler;
	}

}
