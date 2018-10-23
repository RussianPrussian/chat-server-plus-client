import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReadInput implements Callable<String> {
	
	private final static int QUEUE_LIMIT = 1000;
	
	private BufferedReader in;
	private AtomicBoolean readCompleted;//should be false whenever this ReadInput is blocking for input;
										//only one instance of this class should ever be created for a particular 
										//bufferedReader; but we can work on that
										//later...
	private BlockingQueue<String> recentMessage;
	private Lock lock;
	private Lock readCompletedLock;
	
	public ReadInput(BufferedReader in, Lock lock) {
		this.in = in;
		this.lock = new ReentrantLock();
		readCompleted = new AtomicBoolean(true);
		this.recentMessage = new LinkedBlockingQueue<String>(QUEUE_LIMIT);
		readCompletedLock = new ReentrantLock();
	}
	
	public String getMessage() {
		return recentMessage.poll();
	}

	@Override
	public String call() throws Exception {
		String line = "";//only thing we can do is lock until the queue has been cleared.
		synchronized(readCompleted) {
			readCompletedLock.lock();
			readCompleted.set(false);
			readCompletedLock.unlock();
			line = in.readLine();
			System.out.println("Read in Line: " + line);
			System.out.println("Finished the read.");
			readCompletedLock.lock();
			System.out.println("Readable acquired lock to set it to true");
			//i.e. we're making sure that when we read the line, we really don't complete the read
			//until the message is actually available
			if(line != null) {
				try {
					recentMessage.put(line);
				}catch(IllegalStateException e){
					e.printStackTrace();
					System.out.println(line);
				}
			}
			readCompleted.set(true);
			readCompletedLock.unlock();
			
		}
		return line;
	}
	
	public synchronized boolean readCompleted() {
		return readCompleted.get();
	}

}
