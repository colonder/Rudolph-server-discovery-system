package rudolph_client;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.swing.JOptionPane;

public class UDPClient 
{
	/* Zmienne deklarowane tutaj używane są w innych klasach paczki (static)
	 * lub globalnie w tej klasie
	 * */
	
	private InetAddress serverIP;
	private int serverPort;
	private DatagramPacket receivedPacket;
	private DatagramSocket UDPSocket;
	
	UDPClient()
	{
		//Znajdź serwer poprzez broadcast UDP
		try 
		{
			//Otwórz losowy port (który aktualnie jest dostępny), aby wysłać pakiet
			UDPSocket = new DatagramSocket();	
			UDPSocket.setBroadcast(true);	
		} 
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Error creating UDPSocket", "Error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void sendMessageUDP(String messageToSend, InetAddress addressToSend, int portToSend)
	{
		try 
		{
			byte[] byteMsg = messageToSend.getBytes();
			DatagramPacket packet = new DatagramPacket(byteMsg, byteMsg.length, addressToSend, portToSend);
			UDPSocket.send(packet);
			ClientWindow.textArea.append("Request packet sent to: " + addressToSend + "\n");
		}
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Error sending UDP packet", "Error", 
					JOptionPane.ERROR_MESSAGE);
		}   
	}
	
	/*
	 * Funkcja bardzo zbliżona ideą do sendParameters w TCPClient. Ma za zadanie wysłać żądanie
	 * o system nadzorcy z każdego adresu broadcast każdego interfejsu, który go posiada. Jest
	 * to metoda awaryjna w razie gdyby router blokował żądanie wysłanie na 255.255.255.255
	 * */
	
	public void searchThroughInterfaces(String message, int port) throws SocketException
	{
		// Wyślij wiadomość broadcast na wszystkie interfejsy
		  Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		  while (interfaces.hasMoreElements()) 
		  {
		    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

		    if (networkInterface.isLoopback() || !networkInterface.isUp()) 
		    {
		      continue; // Nie wysyłaj wiadomości broadcast na interfejs loopback
		    }

		    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) 
		    {
		      InetAddress broadcast = interfaceAddress.getBroadcast();
		      if (broadcast == null) 
		      {
		        continue;
		      }

		      // Wyślij wiadomość broadcast na wykryty adres broadcast
		      try 
		      {
		        sendMessageUDP(message, broadcast, port);
		      } 
		      
		      catch (Exception e) 
		      {
		    	  JOptionPane.showMessageDialog(null, "Error searching through interfaces", "Error", 
							JOptionPane.ERROR_MESSAGE);
		      }

		      ClientWindow.textArea.append("Request packet sent to: " + broadcast.getHostAddress() + 
		    		  "; Interface: " + networkInterface.getDisplayName() + "\n");
		    }
		  }
		  
		  ClientWindow.textArea.append("Done looping over all network interfaces\n");
	}
	
	public String receiveMessage(int timeoutInMilis)
	{
		byte[] receiveBuffer = new byte[15000];
        
		try 
        {
			UDPSocket.setSoTimeout(timeoutInMilis);
			receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			UDPSocket.receive(receivedPacket);
			ClientWindow.textArea.append("Broadcast response from server: " + 
			receivedPacket.getAddress().getHostAddress() + "\n");
			
	        //setServerIP();
	        //setServerPort();
	        
	        String response = new String(receivedPacket.getData()).trim();
	  
	        return response;
		} 
		
		catch(InterruptedIOException iioe)
		{
			RunClient.lostConnection = true;
			ClientWindow.textArea.append("COULD NOT RECEIVE MESSAGE\n");
		}
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Fatal error UDPClient", "Error", 
					JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}
	
	public void setServerIP()
	{
		serverIP = receivedPacket.getAddress();
	}
	
	public InetAddress getServerIP()
	{
		return serverIP;
	}
	
	public void setServerPort()
	{
		serverPort = receivedPacket.getPort();
	}
	
	public int getServerPort()
	{
		return serverPort;
	}
	
}