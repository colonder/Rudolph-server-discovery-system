package rudolph_client;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.Component;
import javax.swing.Box;

public class ClientWindow 
{
	/* Zmienne deklarowane tutaj używane są w innych klasach paczki (static)
	 * lub globalnie w tej klasie
	 * */
	private JFrame frmRudolphClient;
	static JTextArea textArea;
	/**
	 * Uruchom aplikację
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					ClientWindow window = new ClientWindow();
					window.frmRudolphClient.setVisible(true);
					
					Thread client = new Thread(RunClient.getInstance());
					client.start();
					//Thread client1 = new Thread(RunClient.getInstance());
					//client1.start();
					//Thread client2 = new Thread(RunClient.getInstance());
					//client2.start();
				} 
				
				catch (Exception e) 
				{
					JOptionPane.showMessageDialog(null, "General error mainClient", "Error", 
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/**
	 * Utwórz okno
	 */
	public ClientWindow() 
	{
		initialize();
	}

	/*
	 * Inicjalizacja elementów okna, do pominięcia. Wygenerowane przez creator
	 */
	private void initialize() {
		frmRudolphClient = new JFrame();
		frmRudolphClient.setTitle("Rudolph client");
		frmRudolphClient.setResizable(false);
		frmRudolphClient.setBounds(100, 100, 450, 300);
		frmRudolphClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRudolphClient.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frmRudolphClient.getContentPane().add(panel, BorderLayout.WEST);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		Component verticalStrut_1 = Box.createVerticalStrut(80);
		panel.add(verticalStrut_1);
		
		JButton btnStartClient = new JButton("Start client");
		panel.add(btnStartClient);
		
		Component verticalStrut = Box.createVerticalStrut(7);
		panel.add(verticalStrut);
		
		JButton btnStopClient = new JButton("Stop client");
		panel.add(btnStopClient);
		
		JPanel panel_1 = new JPanel();
		frmRudolphClient.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		panel_1.add(textArea, BorderLayout.CENTER);
	}

}
