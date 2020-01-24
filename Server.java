package server;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Server {
	private static ServerSocket listener;

	public static void main(String[] args) throws Exception {
		int clientNumber = 0;
		int serverPort = 0;
		String serverAddress = "";
		
		Scanner keyboard = new Scanner(System.in);
		
		// Entrez l'adresse IP
		System.out.println("Entrez l'adresse IP du poste:");
		String serverAddressIn = keyboard.next();
		String[] tempAddr = serverAddressIn.split("\\.");
		// Validate l'adresse
		while (!Validators.validateIPAddress(tempAddr)) {
			System.out.println("Entrez l'adresse IP du poste:");
			serverAddressIn = keyboard.next();
			tempAddr = serverAddressIn.split("\\.");	
		}
		serverAddress = serverAddressIn;
		
		// Entrez le numero de port
		System.out.println("Entrez le numero du port (entre 5000 et 5050): ");
		int serverPortIn = keyboard.nextInt();
		
		// Validate le numero de port
		while (!Validators.validatePortNumber(serverPortIn)) {
			System.out.println("Entrez le numero du port (entre 5000 et 5050): ");
			serverPortIn = keyboard.nextInt();
		}
		serverPort = serverPortIn;
		
		keyboard.close();

		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		
		Validators.manageFile();
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
			System.out.println("Merci d'avoir utilisé PolySobel. À la prochaine!");
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
				Boolean userExist = Validators.validateUsername(usernameIn);
				out.writeBoolean(userExist);
				String password = in.readUTF();
				if(userExist)
				{
					out.writeBoolean(Validators.validatePassword(usernameIn, password));
				}
				else
				{
					Validators.setPassword(usernameIn,password);
				
				}
				System.out.format("Usager %s s'est connecte", usernameIn);
				
				System.out.println("Reception d'image");
				byte[] inputImage= in.readAllBytes();
				
				System.out.println("Image en traitement");
				
				InputStream inp = new ByteArrayInputStream(inputImage);
				BufferedImage imageConverted = ImageIO.read(inp);
				BufferedImage processedImaged = Sobel.process(imageConverted);
				
				//Image image = ImageIO.read(new File(fileName));
				//BufferedImage buffered = (BufferedImage) image;
				ByteArrayOutputStream baOut= new ByteArrayOutputStream();
				ImageIO.write(processedImaged,"png",baOut);
				out.write(baOut.toByteArray());
				out.flush();
				System.out.println("Image transforme envoyer au client");
				
			} catch (IOException e)
			{
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
