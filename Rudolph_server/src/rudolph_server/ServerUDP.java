package rudolph_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class ServerUDP implements Runnable
{

	@Override
	public void run() 
	{
		DatagramSocket UDPSocket;
		
		try 
		{
			UDPSocket = new DatagramSocket(8888);
			UDPSocket.setBroadcast(true);
			
			ServerWindow.notificationTxtArea.append("Ready to receive broadcast packets\n");
			
			while(true)
			{

				byte[] receiveBuffer = new byte[15000];
				DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

				UDPSocket.receive(receivedPacket);

				ServerWindow.notificationTxtArea.append("Pakcet received from: " + 
				receivedPacket.getAddress().getHostAddress() + " Port: " + receivedPacket.getPort() + "\n");
				
				ServerWindow.notificationTxtArea.append("Data: " + 
				new String(receivedPacket.getData()) + "\n");
				
				String message = new String(receivedPacket.getData()).trim();
				
				switch(message)
				{
				case "SUPERVISOR_REQUEST":
					sendMessageUDP(UDPSocket, receivedPacket, "HELLO_ACK");
					AskForStatus.addPort(receivedPacket.getPort());
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					
					String hostAddress = receivedPacket.getAddress().getHostAddress();
					String hostPort = "" + receivedPacket.getPort();
					
					/*
					 * Dodaj do listy adres klienta, jego port oraz kiedy był ostatni raz online
					 * i od tej pory odpytuj go co 10 sekund
					 * */
					AskForStatus.addClient(hostAddress);
					AskForStatus.addDate(dateFormat.format(date));
					
					boolean found = false;
					
					/*
					 * Jeżeli z jakiegoś powodu podłączy się klient o tym samym IP ora porcie i 
					 * jest on już na liście monitorowanych klientów, to nie dodawaj go drugi raz
					 * do listy
					 * */
					
					for(int i = 0; i < ServerWindow.model.getRowCount(); i++)
					{
						if(hostAddress.equals(ServerWindow.model.getValueAt(i, 1)))
						{
							if(hostPort.equals(ServerWindow.model.getValueAt(i, 2)))
							{
								found = true;
							}
						}
					}
					
					if(found == false)
					{
						ServerWindow.model.addRow(new Object[]{receivedPacket.getAddress().getHostAddress(), 
								receivedPacket.getPort(), dateFormat.format(date)});
					}
					
					break;
				
				case "READY":
					sendMessageUDP(UDPSocket, receivedPacket, "PARAMETERS_REQUEST");
					break;
				}
			}
		} 
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Fatal error ServerUDP", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void sendMessageUDP(DatagramSocket socket, DatagramPacket packet, String message)
	{	
		try 
		{
			byte[] responseMSg = message.getBytes();
			DatagramPacket responsePacket = new DatagramPacket(responseMSg, responseMSg.length, 
					packet.getAddress(), packet.getPort());
			socket.send(responsePacket);
		} 
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Fatal error sendMessageUDP in ServerUDP", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private static class DiscoveryThreadHolder
	{
		private static final ServerUDP INSTANCE = new ServerUDP();
	}
	
	public static ServerUDP getInstance()
	{
		return DiscoveryThreadHolder.INSTANCE;
	}
}
