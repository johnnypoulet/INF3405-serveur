package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Validators {
	static String path = "credentials.csv";
	static HashMap<String, String> credentials = new HashMap<String, String>();

	// Valider l'adresse (chiffres et nombre de points)
	public static boolean validateIPAddress(String[] input) throws Exception {
		if (input.length != 4) {
			System.out.println("Erreur dans l'addresse. Veuillez reessayer.");
			return false;
		}
		for (int i = 0; i < input.length; i++) {
			try {
				int tempInt = Integer.parseInt(input[i]);
				if (tempInt < 0 || tempInt > 255) {
					return false;
				}
			} catch (Exception e) {
				System.out.println("Erreur dans l'addresse. Veuillez reessayer.");
				return false;
			}
		}
		return true;
	}
	
	// Valider le numero de port
	public static boolean validatePortNumber(int input) throws Exception {
		if (input < 5000 || input > 5050) {
			System.out.println("Erreur dans le numero de port. Veuillez reessayer.");
			return false;
		}
		return true;
	}
	
	public static boolean validateUsername(String username) throws Exception {
		if (credentials.containsKey(username)) {
			return true;
		} else {
			credentials.put(username, " ");
		}
		return false;
	}
	
	public static boolean validatePassword(String username, String password) throws Exception {
		if (credentials.get(username).equals(password)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void setPassword(String username, String password) throws Exception {
		// On met a jour la map
		credentials.put(username, password);
		// On met a jour le fichier
		FileWriter writer = new FileWriter(path);
		credentials.forEach((k, v) -> {
			try {
				writer.append(k);
				writer.append(",");
				writer.append(v);
				writer.append("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		writer.close();
	}
	
	public static void manageFile() throws Exception {
		File file = new File(path);
		// Le fichier existe deja
		if (file.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line = "";
			String[] data;
			while ((line = reader.readLine()) != null) {
				data = line.split(",");
				credentials.put(data[0], data[1]);
			}
			reader.close();
		// On doit creer le fichier
		} else {
			System.out.println("Fichier d'identifiants introuvable. Creation d'un nouveau fichier credentials.csv.");
			FileWriter writer = new FileWriter(path);
			credentials.forEach((username, password) -> {
				try {
					writer.append(username);
					writer.append(",");
					writer.append(password);
					writer.append("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			writer.close();
		};
	}
}
	
