package rudolph_server;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.Box;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.JSeparator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;

public class ServerWindow 
{
	/* Zmienne deklarowane tutaj używane są w innych klasach paczki (static)
	 * lub globalnie w tej klasie
	 * */
	private JFrame frmRudolphServer;
	private static Thread UDPConnection, TCPConnection, Status;
	static JTextArea notificationTxtArea;
	static DefaultTableModel model;
	static JTable table;

	/**
	 * Launch the application. Jedynie do ogarnięcia ActionListener dla
	 * przycisku Usuń klienta btnRemoveClient
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					ServerWindow window = new ServerWindow();
					window.frmRudolphServer.setVisible(true);
					
					UDPConnection = new Thread(ServerUDP.getInstance());
					TCPConnection = new Thread(ServerTCP.getInstance());
					Status = new Thread(AskForStatus.getInstance());
					UDPConnection.start();
					TCPConnection.start();
					Status.start();
				} 
				
				catch (Exception e) 
				{
					JOptionPane.showMessageDialog(null, "General error mainServer", 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerWindow() 
	{
		initialize();
	}

	/**
	 * Inicjalizacja okna.
	 */
	private void initialize() 
	{
		frmRudolphServer = new JFrame();
		frmRudolphServer.setTitle("Rudolph Server");
		frmRudolphServer.setResizable(false);
		frmRudolphServer.setBounds(100, 100, 650, 300);
		frmRudolphServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRudolphServer.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmRudolphServer.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Management", null, panel, null);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.WEST);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.Y_AXIS));
		
		Component verticalStrut = Box.createVerticalStrut(80);
		panel_3.add(verticalStrut);
		
		JButton btnStartServer = new JButton("Start server");
		panel_3.add(btnStartServer);
		
		Component verticalStrut_1 = Box.createVerticalStrut(7);
		panel_3.add(verticalStrut_1);
		
		JButton btnStopServer = new JButton("Stop server");
		panel_3.add(btnStopServer);
		
		JPanel panel_4 = new JPanel();
		panel.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		notificationTxtArea = new JTextArea();
		notificationTxtArea.setWrapStyleWord(true);
		notificationTxtArea.setLineWrap(true);
		panel_4.add(notificationTxtArea, BorderLayout.CENTER);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		panel_4.add(separator, BorderLayout.WEST);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Clients", null, panel_1, null);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel clientButtonsPanel = new JPanel();
		panel_1.add(clientButtonsPanel, BorderLayout.WEST);
		clientButtonsPanel.setLayout(new BoxLayout(clientButtonsPanel, BoxLayout.PAGE_AXIS));
		
		Component rigidArea = Box.createRigidArea(new Dimension(20, 100));
		clientButtonsPanel.add(rigidArea);
		
		JButton removeClientButton = new JButton("Remove Client");
		removeClientButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				int[] rowsToDelete = table.getSelectedRows();
				
				for(int i = 0; i < rowsToDelete.length; i++)
				{
					if(AskForStatus.clientsList.contains(model.getValueAt(rowsToDelete[i], 0)))
					{
						if(AskForStatus.portsList.contains(model.getValueAt(rowsToDelete[i], 1)))
						{
							AskForStatus.clientsList.remove(model.getValueAt(rowsToDelete[i], 0));
							AskForStatus.portsList.remove(model.getValueAt(rowsToDelete[i], 1));
							AskForStatus.lastConnected.remove(model.getValueAt(rowsToDelete[i], 2));
						}
					}
					
					model.removeRow(rowsToDelete[i]);
				}
	
			}
		});
		clientButtonsPanel.add(removeClientButton);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(20, 5));
		clientButtonsPanel.add(rigidArea_2);
		
		JButton btnProperties = new JButton("Properties");
		clientButtonsPanel.add(btnProperties);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.setModel(new DefaultTableModel(new Object[][] {}, new String[] 
				{"Client IP", "Port", "Last connected"}));
		model = (DefaultTableModel) table.getModel();
		scrollPane.setViewportView(table);
	}
}
