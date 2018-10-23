package chat.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chat.connector.ChatConnector;

public class ClientFrame extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private ChatConnector chatConnector;
	private JPanel serverOutputPanel;
	private JPanel serverInputPanel;
	
	public ClientFrame(ChatConnector chatConn) {
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1.0;
		gc.weighty = .9;
		gc.fill = GridBagConstraints.BOTH;
		this.chatConnector = chatConn;
		serverOutputPanel = new ServerOutputPanel();
		this.add(serverOutputPanel, gc);
		
		gc.gridx = 0;
		gc.gridy = 1;
		gc.weightx = 1.0;
		gc.weighty = .1;
		gc.fill = GridBagConstraints.BOTH;
		serverInputPanel = new ServerInputPanel();
		this.add(serverInputPanel, gc);
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		ChatConnector chatConnector = new ChatConnector();
		
		SwingUtilities.invokeLater(()->{
			JFrame frame = new ClientFrame(chatConnector);
			frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			frame.setVisible(true);
			frame.setSize(800, 500);
			new Thread(chatConnector).start();
		});
	}
}
