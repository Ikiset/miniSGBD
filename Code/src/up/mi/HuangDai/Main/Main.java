package up.mi.HuangDai.Main;

import java.util.Scanner;
import up.mi.HuangDai.GestionDuDisque.DBParams;

public class Main {

	public static void main(String[] args) {
		// Chemin vers le dossier DB
		DBParams.DBPath = "../DB/";
				
		// Taille d'une page
		DBParams.pageSize= 4096;
		
		// Case
		DBParams.frameCount=2;
		
		// Appel de la méthode init
		DBManager.getInstance().init();
		
		
		Scanner scanner= new Scanner (System.in); 
		
		String command;
		

		do {
			System.out.println("Bonjour, Veuillez saisir une commande ?\n");
			command = scanner.nextLine();
				
			if (command.equals("EXIT")) {
				System.out.println("Au revoir!\n");
				DBManager.getInstance().finish();
				break;
			} else {
				DBManager.getInstance().processCommand(command);
			}	
		} while (command != "EXIT");
			
		scanner.close();


	}

}
