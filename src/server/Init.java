package server;

import java.net.InetAddress;
import java.util.Scanner;

public class Init {
	static Scanner keyboard = new Scanner(System.in);
	
	public static void close() throws Exception {
		keyboard.close();
	}
	
	public static String serverIP() throws Exception {
        InetAddress inetAddress = InetAddress.getLocalHost();
        System.out.println("L'adresse IP détectée du poste est la suivante: " + inetAddress.getHostAddress());
        
		// Entrez l'adresse IP
		System.out.println("Entrez l'adresse IP que vous voulez utiliser:");
		String serverAddressIn = keyboard.next();
		String[] tempAddr = serverAddressIn.split("\\.");
		// Valider l'adresse
		while (!Validators.validateIPAddress(tempAddr)) {
			System.out.println("Entrez l'adresse IP du poste:");
			serverAddressIn = keyboard.next();
			tempAddr = serverAddressIn.split("\\.");	
		}
		return serverAddressIn;
	}
	
	public static int serverPort() throws Exception {
		// Entrez le numero de port
		System.out.println("Entrez le numero du port (entre 5000 et 5050): ");
		int serverPortIn = keyboard.nextInt();
		
		// Valider le numero de port
		while (!Validators.validatePortNumber(serverPortIn)) {
			System.out.println("Entrez le numero du port (entre 5000 et 5050): ");
			serverPortIn = keyboard.nextInt();
		}
		return serverPortIn;
	}
}
