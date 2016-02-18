package rudolph_server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JOptionPane;

public class AskForStatus implements Runnable
{
	/* Zmienne deklarowane tutaj używane są w innych klasach paczki (static)
	 * lub globalnie w tej klasie. Poniżej deklaracja list, którze przechowują
	 * adresy klientów, ich porty oraz kiedy ostatni raz byli online
	 * */
	static List<String> clientsList = new ArrayList<String>();
	static List<String> lastConnected = new ArrayList<String>();
	static List<Integer> portsList = new ArrayList<Integer>();
	private String client, date;
	private DatagramPacket receivedPacket;
	private int clientPort, counter;
	
	@Override
	public void run() 
	{ 
		
		try 
		{
			byte[] ask = "STATUS".getBytes();
			byte[] buffer = new byte[100];

			while(true)
			{
				Thread.sleep(10000);
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date currDate = new Date();
				DatagramSocket socket = new DatagramSocket(9000);
				ListIterator<String> clientIterator = clientsList.listIterator();
				ListIterator<String> dateIterator = lastConnected.listIterator();
				ListIterator<Integer> portIterator = portsList.listIterator();;
				
				while(clientIterator.hasNext())
				{
					client = clientIterator.next();
					date = dateIterator.next();
					clientPort = portIterator.next();
					counter = clientIterator.nextIndex();
					
					DatagramPacket packet = new DatagramPacket(ask, ask.length, 
							InetAddress.getByName(client), clientPort);
					socket.send(packet);
					
					receivedPacket = new DatagramPacket(buffer, buffer.length);
					socket.setSoTimeout(5000);
					socket.receive(receivedPacket);

					String response = new String(receivedPacket.getData()).trim();
					/*
					 * Jeżeli klient nie odpowiada na żądanie lub nie wysłał wiadomość OK
					 * to usuń go z listy odpytywanych klientów i nie odpytuj go więcej
					 * */
					if(response == null || !response.equals("OK"))
					{
						ServerWindow.notificationTxtArea.append("No response from: " + client + " port: " 
					+ clientPort + " last connected: " + date + "\n");
						ServerWindow.model.removeRow(counter);
						clientIterator.remove();
						dateIterator.remove();
						portIterator.remove();
					}
					
					/*
					 * Jezeli klient odpowiedział poprawnie, to zaktualizuj datę,
					 * kiedy był online ostatni raz
					 * */
					
					else
					{
						dateIterator.set(dateFormat.format(currDate));
						ServerWindow.table.setValueAt(date, counter, 2);
					}	
					
				}
				
				socket.close();
				
				//updateLogFile();
			}
		} 
		
		catch (InterruptedException e)
		{
			ServerWindow.notificationTxtArea.append("No response from: " + client + " port: " + 
		clientPort + " last connected: " + date + "\n");
		} 
		
		catch (SocketException e) 
		{
			JOptionPane.showMessageDialog(null, "Socket error AskForStatus", "Error", 
					JOptionPane.ERROR_MESSAGE);
		} 
		
		catch (UnknownHostException e) 
		{
			JOptionPane.showMessageDialog(null, "Unknown host", "Error", JOptionPane.ERROR_MESSAGE);
		} 
		
		catch (IOException e) 
		{
			ServerWindow.notificationTxtArea.append("No response from: " + client + 
					" port: " + clientPort + " last connected: " + date + "\n");
			ServerWindow.model.removeRow(counter);
			
		}
	}
	
	public static void addClient(String clientAddress)
	{
		clientsList.add(clientAddress);
	}
	
	public static void addDate(String time)
	{
		lastConnected.add(time);
	}
	
	public static void addPort(int port)
	{
		portsList.add(port);
	}
	
	public static AskForStatus getInstance()
	{
		return DiscoveryThreadHolder.INSTANCE;
	}
	
	private static class DiscoveryThreadHolder
	{
		private static final AskForStatus INSTANCE = new AskForStatus();
	}
	
}
