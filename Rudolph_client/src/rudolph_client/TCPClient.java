package rudolph_client;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.swing.JOptionPane;

public class TCPClient 
{
	/* Zmienne deklarowane tutaj używane są w innych klasach paczki (static)
	 * lub globalnie w tej klasie
	 * */
	private InetAddress serverIP;
	private int serverPort;
	private Socket TCPSocket;
	private BufferedWriter writer;
	private NetworkInterface network;
	
	TCPClient(InetAddress SERVER_IP, int SERVER_PORT) throws UnknownHostException, IOException
	{
//		Tworzymy socket, który łączy się z podanym adresem IP i portem
		TCPSocket = new Socket(SERVER_IP, SERVER_PORT);
		writer = new BufferedWriter(new OutputStreamWriter(TCPSocket.getOutputStream()));
		setServerIP(SERVER_IP);
		setServerPort(SERVER_PORT);
	}	
	
	public void sendMessageTCP(String message)
	{
		try 
		{
			/*
			 * Przekazujemy do writer wiadomość message i dodajemy na końcu znak nowej linii
			 * Jest to niezbędne przy wysyłaniu wiadomości TCP, gdyż bez niego bufor będzie oczekiwał
			 * na znak końca wiadomości \n. Metoda flush() opróżnia bufor i wysyła wiadomość
			 * nawet jeżeli bufor nie jest pełny
			 * */
			writer.write(message + "\n");
			writer.flush();
		} 
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Error sending TCP message", "Error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
//	Funkcja realizująca wysyłanie parametrów
	
	public void sendParameters()
	{
		
		try 
		{
			/*
			 * Poniższy fragment kodu ma za zadanie wykrycie, który interfejs sieciowy 
			 * odpowiada za localhost
			 * */
			
			InetAddress localhost = null;
			
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
			while (networkInterfaces.hasMoreElements()) 
			{
//				Wybierz następny interfejs sieciowy z listy
                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
                
//              Podaj wszystkie adresy IP przypisane danemu interfejsowi
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                
//              Jeżeli w danym interfejsie są jakieś adresy IP
                while(nias.hasMoreElements()) 
                {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    
//                    Sprawdzenie czy dany adres IP odpowiada za localhost (nie loopback)
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) 
                    {
                        localhost = ia;
                    }
                }
            }
			
//			Po znaleznieniu adresu IP localhost przypisz go do zmiennej network
			network = NetworkInterface.getByInetAddress(localhost);
			
//			Pobierz adres MAC interfejsu, któremu odpowiada adres localhost
			byte[] mac = network.getHardwareAddress();
			
			/*
			 * Odpowiednio sformatuj pobrany adres MAC tak, by był czytelny dla człowieka
			 * */
			
			StringBuilder macAddress = new StringBuilder();
			
			for(int i = 0; i < mac.length; i++)
			{
				macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1)? "-":""));
			}
			
			String MAC = macAddress.toString();
			
			/*
			 * TYLKO SYSTEMY UNIX LUB KAŻDY INNY ZAWIERAJĄCY BASH
			 * Poniższy kawałek kodu realizuje funkcję rozpoznania parametrów maszyny
			 * poprzez wykonanie poleceń bash, ponieważ tylko w tym przypadku
			 * zwrócone dane będą odpowiadały faktycznym parametrom maszyny, a nie
			 * obecnej instancji JVM*/
			
			/*
			 * Funckje buildExecCommand i buildString zaimplementowane poniżej
			 * */
			
			Process loadCPU = Runtime.getRuntime().exec(buildExecCommand("ps aux | sort -n -k 3 | tail -20"));
			Process loadRAM = Runtime.getRuntime().exec(buildExecCommand("ps aux | sort -n -k 4 | tail -20"));
			Process totalRam = Runtime.getRuntime().exec(buildExecCommand("cat /proc/meminfo | grep Mem"));
			Process countCPU = Runtime.getRuntime().exec("nproc");
			
			String top20CPU = buildString(loadCPU);
			String top20RAM = buildString(loadRAM);
			String totalMemory = buildString(totalRam);
			String cores = buildString(countCPU);
			
			String[] parameters = {localhost.getHostAddress() + "\n" + MAC + "\n", cores, totalMemory, 
					"TOP 20 CPU:\n\n" + top20CPU, "TOP 20 RAM:\n\n" + top20RAM};
			
			for(int i = 0; i < parameters.length; i++)
			{
				sendMessageTCP(parameters[i]);
			}
		} 
		
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Fatal error sending parameters", "Error", 
					JOptionPane.ERROR_MESSAGE);
		} 
		
	}
	
	private String[] buildExecCommand(String command)
	{
		/*
		 * Tylko w takiej formie metoda Runtime.getRuntime().exec() wykona polecenie command
		 * która zawiera pipe
		 * */
		String[] cmd = {"/bin/sh", "-c", command};
		
		return cmd;
	}
	
	/*
	 * Uzyskaj output danej komendy w odpowiedniej formie
	 * */
	
	private String buildString(Process processToRun) throws IOException
	{
		String tmp, outputString;
		BufferedReader output = new BufferedReader(new InputStreamReader(processToRun.getInputStream()));
		StringBuilder builder = new StringBuilder();
		
		/*
		 * Dopóki wynik polecenia zawiera nową linię to przeczytaj ją, dodaj do wynikowego Stringa
		 * oraz przejdź do nowej linii
		 * */
		while ((tmp = output.readLine()) != null)
		{
			builder.append(tmp + "\n");
		}
		
		outputString = builder.toString();
		
		return outputString;
	}
	
	public void setServerIP(InetAddress SERVER_IP)
	{
		serverIP = SERVER_IP;
	}
	
	public InetAddress getServerIP()
	{
		return serverIP;
	}
	
	public void setServerPort(int SERVER_PORT)
	{
		serverPort = SERVER_PORT;
	}
	
	public int getServerPort()
	{
		return serverPort;
	}
}