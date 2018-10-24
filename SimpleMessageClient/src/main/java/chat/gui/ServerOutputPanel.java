package chat.gui;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.BorderUIResource;

import chat.observable.ObservableMediator;
import chat.observable.ObservableMessage;

public class ServerOutputPanel extends JPanel implements PropertyChangeListener{

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private StringBuffer textAreaText;
	private JScrollPane scrollPane;

	private static final int TAN_SHADE = 0xE5DFC5;
	private static final int BLACK_SHADE = 0x252120;
	private static final int BROWN_SHADE = 0x857c73;
	
	public ServerOutputPanel() {
		
		textAreaText = new StringBuffer("");
		this.textArea = new JTextArea("");
		this.textArea.setEditable(false);
		this.textArea.setBackground(new Color(BROWN_SHADE));
		this.textArea.setForeground(new Color(TAN_SHADE));
		
		textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
		this.scrollPane = new JScrollPane(this.textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(BorderFactory.createLineBorder(new Color(BLACK_SHADE)));
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx= 0;
		gc.gridy= 0;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.fill = GridBagConstraints.BOTH;
		this.add(this.scrollPane,  gc);
		this.setBorder(BorderFactory.createLineBorder(new Color(BLACK_SHADE)));
		ObservableMediator.registerWithListener("mostRecentServerMessage", this);
		ObservableMediator.registerWithListener("messageToSend", this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		synchronized(textAreaText) {
			String newText = (String) evt.getNewValue();
			textAreaText.append("\n>>");
			textAreaText.append(newText);
			this.textArea.setText(textAreaText.toString());
			this.textArea.setCaretPosition(textAreaText.toString().length() -1);
		}
	}

}
