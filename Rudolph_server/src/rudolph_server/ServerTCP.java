package rudolph_server;

import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

public class ServerTCP implements Runnable
{
	/* Zmienne deklarowane tutaj używane są w innych klasach paczki (static)
	 * lub globalnie w tej klasie
	 * */
	private ServerSocket tCPSocket;

	@Override
	public void run() 
	{
		try 
		{
//			nowy socket z portem
			tCPSocket = new ServerSocket(12347);
			
			while (true)
			{	
//				czekaj dopóki nikt nie podłączy sie do socketa
				Socket socket = tCPSocket.accept();
				
				BufferedReader buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientAddress = socket.getInetAddress().getHostAddress();
				int clientPort = socket.getPort();
			
				Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream("Client " + clientAddress + " port " + clientPort), "utf-8"));
				String messageTCP = buffRead.readLine();
				
				/*
				 * Jeżeli klient odpowiedział na żądanie o parametry i wiadomość, którą wysłał nie
				 * jest pusta, to utwórz nowy plik o nazwie "Client " + clientAddress + " port " + clientPort
				 * i zapisz w nim to co wysłał klient
				 * */
				while(messageTCP != null)
				{
					fileWriter.write(messageTCP + "\n");
					messageTCP = buffRead.readLine();
				}
				
				fileWriter.close();
				buffRead.close();
				socket.close();
			}
		} 
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Fatal error ServerTCP", "Error", JOptionPane.ERROR_MESSAGE);
		}	
	}
	
	public static ServerTCP getInstance()
	{
		return DiscoveryThreadHolder.INSTANCE;
	}
	
	private static class DiscoveryThreadHolder
	{
		private static final ServerTCP INSTANCE = new ServerTCP();
	}
}