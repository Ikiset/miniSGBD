package up.mi.HuangDai.GestionDuDisque;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


/**
 * Tests des méthodes de la gestionnaire du disque
 * @author Huihui Huang, Julien Dai
 *
 */
@SuppressWarnings("unused")
public class DiskManagerTests {

	public static void main(String[] args) throws IOException {
		  
		  DBParams.DBPath = "../DB/";
		  DBParams.pageSize = 4096;
		  
		  int command, fileIdx, pageIdx;
		  String phrase, bidon;
		  byte[] lire, buff;
		  		  
		  // Instanciation du DiskManager
		  DiskManager DiskManager1= DiskManager.getInstance();
		  
		  Scanner sc = new Scanner(System.in);
		  
		  System.out.println("Attention: Si le fichier existe il rajoute directement une page à le fichier\n");
		  System.out.print("Quel identifiant pour le fichier à crée ? "); 
		  fileIdx= sc.nextInt();
		 
		  // Création d'un fichier 
		  DiskManager1.createFile(fileIdx);

		  do {
				  
			  System.out.println("\n--------------- Menu ---------------\n");
			  System.out.println("\t0 - Arret\n\t1 - Ecrire\n\t2 - Lire\n\t3 - Add page\n");
			  System.out.print("Votre choix ? ");
			  command = sc.nextInt();
			  bidon = sc.nextLine();
				  
			  switch(command) {
			  	case 1:
			  		// Saisie de la position de la pageId
			  		System.out.print("Sur quel page voulez-vous écrire ? ");
		          	pageIdx= sc.nextInt();
		          	bidon = sc.nextLine();
				  
		          	PageId pageIdw= new PageId(fileIdx, pageIdx);
		          	System.out.println("PageId: ("+fileIdx+","+pageIdx+")");  
		          
		          	// Buffer à écrire 
		          	System.out.print("Phrase a ecrire : ");
		          	phrase = sc.nextLine();
		          
		          	buff = phrase.getBytes();
		          
		          	// Ecrire dans le fichier
		          	DiskManager1.writePage(pageIdw, buff);
				  	break;
				  
			  	case 2:
				  // Saisie de la position de la pageId
			  		System.out.print("Sur quel page voulez-vous lire ? ");
		          	pageIdx= sc.nextInt();
		          
		          	PageId pageIdr= new PageId(fileIdx, pageIdx);
		          	System.out.println("PageId: ("+fileIdx+","+pageIdx+")");
		          
		          	// Buffer à lire
		          	lire = new byte[DBParams.pageSize];
		          
		          	// Lire dans le fichier
		          	DiskManager1.readPage(pageIdr, lire);
		          
		          	System.out.println("Affichage : " + new String(lire));
		          	break;
		          
			  	case 3:
			  		// Ajout de page
			  		pageIdw = DiskManager1.addPage(fileIdx);
			  		break;
			  		
			  	default: 
			  		System.out.println("Mauvaise commande");
			  }
				  
		  }while(command != 0);
		  
		  sc.close();
	}

}
