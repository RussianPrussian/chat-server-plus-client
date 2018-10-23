package chat.observable;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObservableMediator {
	
	private static final Map<String, ObservableMessage> obsMessageMap = new ConcurrentHashMap<>();
	private static Lock lock = new ReentrantLock();
	
	public static ObservableMessage getObservableMessage(String observableName) {
		lock.lock();
		ObservableMessage observableToReturn;
		if(!obsMessageMap.containsKey(observableName)) {
			obsMessageMap.put(observableName, new ObservableMessage(observableName));
		}
		observableToReturn = obsMessageMap.get(observableName);
		lock.unlock();
		return observableToReturn;
	}
	
	public static void registerWithListener(String observableName, PropertyChangeListener listener) {
		lock.lock();
		if(!obsMessageMap.containsKey(observableName)) {
			obsMessageMap.put(observableName, new ObservableMessage(observableName));
		}
		obsMessageMap.get(observableName).registerListener(listener);
		lock.unlock();
	}
}
