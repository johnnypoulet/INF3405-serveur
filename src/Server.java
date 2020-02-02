package server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class Server {
	private static ServerSocket listener;

	public static void main(String[] args) throws Exception {
		System.out.println("Bienvenue dans l'application PolySobel - Serveur! (Copyright Derek Bernard & Jean-Olivier Dalphond 2020)");
		
		int clientNumber = 0;
		int serverPort = 0;
		String serverAddress = "";
        
		serverAddress = Init.serverIP();
		serverPort = Init.serverPort();

		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		Validators.manageCredentials();
		System.out.format("Le serveur fonctionne sur %s:%d%n", serverAddress, serverPort);
		
		try
		{
			while (true)
			{
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		}
		finally
		{
			System.out.println("Merci d'avoir utilise PolySobel. A la prochaine!");
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread
	{
		private Socket socket;
		private int clientNumber;
		
		public ClientHandler(Socket socket, int clientNumber)
		{
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("Nouvelle connexion avec le client #" + clientNumber + " sur " + socket + ".");
		}
		
		public void run()
		{
			try
			{
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				String usernameIn = in.readUTF();
				Boolean userExists = Validators.validateUsername(usernameIn);
				out.writeBoolean(userExists);
				String password = in.readUTF();
				
				if (userExists) {
					if (Validators.validatePassword(usernameIn, password)) {
						out.writeBoolean(true);
						System.out.format("Usager existant %s s'est connecte. En attente d'une image...\n", usernameIn);
					} else {
						out.writeBoolean(false);
						System.out.format("Usager existant %s: mot de passe refuse. \n", usernameIn);
						return;
					}
				} else {
					Validators.setPassword(usernameIn, password);
					out.writeBoolean(true);
					System.out.format("Nouvel usager %s s'est connecte. En attente d'une image...\n", usernameIn);
				}
				
				// Attente de la taille et du nom de l'image
				int len = in.readInt();
				String imageName = in.readUTF();
				
				// Obtention de la date
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
				Date now = new Date();
				
				// Attente de l'image
				System.out.format("Taille de l'image: %s octets. Reception d'image...\n", len);
				byte[] inputImage = in.readNBytes(len);
				
				// Image recue
				System.out.println("[" + usernameIn + " - " + Validators.cleanIPAddressFormat(socket.getRemoteSocketAddress().toString()) + ":" + socket.getLocalPort() + " - " + sdf.format(now) + "] : Image " + imageName + " recue pour traitement.");
				InputStream inp = new ByteArrayInputStream(inputImage);
				BufferedImage imageConverted = ImageIO.read(inp);
				BufferedImage processedImage = Sobel.process(imageConverted);
				
				System.out.println("Image traitee. Transmission...");
				ByteArrayOutputStream baOut= new ByteArrayOutputStream();
				ImageIO.write(processedImage, "png", baOut);
				byte[] lenMod = ByteBuffer.allocate(4).putInt(baOut.size()).array();
				out.write(lenMod);
				out.write(baOut.toByteArray());
				out.flush();
				System.out.println("Image envoyee au client.");
			} catch (IOException e)	{
				System.out.println("Erreur dans le traitement demande par le client # " + clientNumber + ": " + e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				try
				{
					socket.close();
					Validators.addToCredentials();
				}
				catch (IOException e)
				{
					System.out.println("Impossible de fermer le socket.");
				}
				System.out.println("Connexion avec le client # " + clientNumber + " fermee.");
			}
		}
	}
}
