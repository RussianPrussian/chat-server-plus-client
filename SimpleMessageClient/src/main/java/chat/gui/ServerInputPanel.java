package chat.gui;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import chat.observable.ObservableMediator;
import chat.observable.ObservableMessage;

public class ServerInputPanel extends JPanel implements ActionListener{
	
	private JTextArea inputTextArea;
	private JButton sendMessageButton;
	private ObservableMessage messageToSend;
	
	public ServerInputPanel() {
		this.messageToSend = ObservableMediator.getObservableMessage("messageToSend");
		
		this.inputTextArea = new JTextArea();
		this.sendMessageButton = new JButton("Submit");
		this.sendMessageButton.addActionListener(this);
		Action enterAction =  new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = inputTextArea.getText();
				inputTextArea.setText("");
				messageToSend.setMessage(text);//we'll extract to a method;
			}
		};
		
		this.sendMessageButton.getInputMap(JComponent.WHEN_FOCUSED)
							  .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		this.sendMessageButton.getActionMap().put("Enter",enterAction);
		this.inputTextArea.getInputMap(JComponent.WHEN_FOCUSED)
		  .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		this.inputTextArea.getActionMap().put("Enter",enterAction);
		
		this.inputTextArea.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar(); 
				if (c == KeyEvent.VK_ENTER){
					e.consume();
					sendMessageButton.dispatchEvent(e);
				}
			}
		});
		
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = .9;
		gc.weighty = 1;
		this.add(inputTextArea, gc);
		
		gc.gridx = 1;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = .1;
		this.add(sendMessageButton, gc);
		this.inputTextArea.requestFocusInWindow();
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String text = inputTextArea.getText();
		inputTextArea.setText("");
		messageToSend.setMessage(text);
	}
}
