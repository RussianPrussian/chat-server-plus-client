package chat.observable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

public class ObservableMessage {
	private String message;
	private String observableId;
	private PropertyChangeSupport changes;
	
	public ObservableMessage() {
		changes = new PropertyChangeSupport(this);
		message = "";
	}
	
	public ObservableMessage(String observableId) {
		changes = new PropertyChangeSupport(this);
		message = "";
		this.observableId = observableId;
	}
	
	public String getObservableId() {
		return this.observableId;
	}
	
	public void registerListener(PropertyChangeListener listener) {
		changes.addPropertyChangeListener(listener);
	}
	
	public void setMessage(String newMessage) {
		String oldMessage = this.message;
		this.message = newMessage;

		if(Objects.equals(oldMessage, this.message)) {
			oldMessage = "";
		}
		changes.firePropertyChange("message", oldMessage, this.message);
	}

}
