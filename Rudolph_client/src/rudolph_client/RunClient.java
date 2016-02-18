package rudolph_client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class RunClient implements Runnable
{
	/* Zmienne deklarowane tutaj używane są w innych klasach paczki (static)
	 * lub globalnie w tej klasie
	 * */
	private String messageUDP;
	static boolean lostConnection = true;
	
//	definicja tego co robi wątek
	
	@Override
	public void run()
	{
//		nowa instancja klasy implementującej połączenie UDP
		UDPClient broadcast = new UDPClient();
		
		/* Pętla działająca przez cały czas działania klienta
		 * Sprawdza czy klient nie utracił połączenia z serwerem
		 * */
		while (true)
		{
//			Domyślnie po uruchomieniu klienta lostConnection = true
			if (lostConnection)	
			{
//				Dodajemy do okna powiadomień
				ClientWindow.textArea.append("CONNECTING\n");
				try 
				{
					broadcast.sendMessageUDP("SUPERVISOR_REQUEST", InetAddress.getByName("255.255.255.255"), 8888);
					//broadcast.searchThroughInterfaces("SUPERVISOR_REQUEST", 8888);
				} 
				
				catch (UnknownHostException e1) 
				{
					JOptionPane.showMessageDialog(null, "Could not find host", "Error", JOptionPane.ERROR_MESSAGE);
				}
				
//				Dopóki klient nie połaczy się z serwerem podejmuj próbę połączenia
				do
				{
//					Ustawiam timeout na otrzymanie wiadomosci na 5 sekund poprzez argument funkcji
						messageUDP = broadcast.receiveMessage(5000);
						
						/*
						 * Jeżeli klient nie otrzyma wiadomości (pusta wiadomość)
						 * to skończ obecną iterację i przejdź do nowej iteracji - 
						 * ponownie wysyła komunikat broadcast
						 *  */
						if(messageUDP == null)
						{
							ClientWindow.textArea.append("CANNOT CONNECT\n");
							break;
						}
						
//						Jeżeli otrzymana wiadomość zawiera treść to w zależności od tego co zawiera
						else
						{
							switch(messageUDP)
							{
							case "HELLO_ACK":
								broadcast.setServerIP();
								broadcast.setServerPort();
								broadcast.sendMessageUDP("READY", broadcast.getServerIP(), broadcast.getServerPort());
								break;
					
							case "PARAMETERS_REQUEST":
								try 
								{
									/*
									 * Utwórz nową instancję klasy implementującej
									 * połączenie TCP, w konstruktorze podaję adres IP
									 * serwera oraz port TCP*/
									TCPClient direct = new TCPClient(broadcast.getServerIP(), 12347);
									
//									Wyślij parametry maszyny
									direct.sendParameters();
								} 
								
								catch (IOException e) 
								{
									JOptionPane.showMessageDialog(null, "TCP connection error", "Error", JOptionPane.ERROR_MESSAGE);
								}	
								
								break;
							}
						}
				}
				while(messageUDP == null || !messageUDP.equals("PARAMETERS_REQUEST"));
				/*
				 * Pętla wykonuje się dopóty, dopóki klient nie otrzyma żądania o 
				 * parametry maszyny, lub nie otrzymuje żadnej wiadomości ze strony
				 * serwera
				 * */
		
				/*
				 * Jeżeli klient pozytywnie przeszedł wszystkie etapy nawiązywania
				 * połączenia to ustawia lostConnection na false i od tej pory klient
				 * już tylko oczekuje na odpytanie go przez serwer
				 * */
					lostConnection = false;
					ClientWindow.textArea.append("CONNECTED\n");
				
			}
			
//			Oczekiwanie na odpytanie
			else
			{
				/*
				 * Ustawiam timeout na 15 sekund czyli nieco więcej niż wynosi
				 * częstotliwość, z jaką serwer odpytuje klientów (10 sekund)
				 * */
				messageUDP = broadcast.receiveMessage(15000);
				
				/*
				 * Jeżeli serwer nie odpowiada (pusta wiadomość) lub wysłana wiadomość
				 * zawiera coś innego niż żądanie STATUS to zakomunikuj zerwanie
				 * połączenia i w ten sposób wykonaj w/w pętlę nawiązującą połączenie*/
				if(messageUDP == null || !messageUDP.equals("STATUS"))
				{
					lostConnection = true;
					ClientWindow.textArea.append("CONNECTION LOST\n");
				}
				
				else
				{
					//System.out.println(messageUDP);
					broadcast.sendMessageUDP("OK", broadcast.getServerIP(), broadcast.getServerPort());
				}
				
			}
		}
	}
	
	/*
	 * metoda i klasa niezbędna do implementacji klasy Singleton
	 * */
	
	private static class DiscoveryThreadHolder
	{
		private static final RunClient INSTANCE = new RunClient();
	}
	
	public static RunClient getInstance()
	{
		return DiscoveryThreadHolder.INSTANCE;
	}

}
