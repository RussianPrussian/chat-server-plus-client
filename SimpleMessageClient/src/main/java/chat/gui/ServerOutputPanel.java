package chat.gui;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.plaf.BorderUIResource;

import chat.observable.ObservableMediator;
import chat.observable.ObservableMessage;

public class ServerOutputPanel extends JPanel implements PropertyChangeListener{

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private StringBuffer textAreaText;
	
	public ServerOutputPanel() {
		textAreaText = new StringBuffer("");
		this.textArea = new JTextArea("");
		this.textArea.setEditable(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx= 0;
		gc.gridy= 0;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.fill = GridBagConstraints.BOTH;
		this.add(this.textArea,  gc);
		this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		ObservableMediator.registerWithListener("mostRecentServerMessage", this);
		ObservableMediator.registerWithListener("messageToSend", this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		synchronized(textAreaText) {
			ObservableMessage sourceMessage = (ObservableMessage) evt.getSource();//...we append it no matter what actually
			String newText = (String) evt.getNewValue();
			textAreaText.append("\n>>");
			textAreaText.append(newText);
			this.textArea.setText(textAreaText.toString());
		}
	}

}
