package up.mi.HuangDai.GestionDuDisque;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Management du disque: API du gestionnaire du disques
 * -> Alouer/ Déallouer de l'espace
 * -> Lecture/ Ecrire données
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class DiskManager {

	/**
	 * Instance de DiskManager
	 */
	private static DiskManager INSTANCE;
	
	/**
	 * Instanciation de l'instance DiskManager
	 * 
	 * @return L'instance de DiskMaanager
	 */
	public static DiskManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DiskManager();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Crée dans le sous-dossier DB un fichier Data_fileIdx.rf
	 * 
	 * @param fileIdx Identifiant du fichier 
	 * 
	 * @throws IOException 
	 */
	public void createFile(int fileIdx) throws IOException {
		try {			
			// Chemin du fichier
			String fileName= DBParams.DBPath+"Data_"+fileIdx+".rf";
			File file= new File(fileName);	
			
			file.createNewFile();

		} catch(Exception e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Rajoute une page au fichier spécifié par fileIdx 
	 * C'est-à-dire, elle rajoute pageSize octets, avec une valeur quelconque à la fin du fichier
	 * 
	 * @param fileIdx Identifiant de fichier
	 * 
	 * @return PageId correspondant à la page nouvellement rajoutée
	 */
	public PageId addPage(int fileIdx) {	
		// Le fichier a trouvé
		String fileName= DBParams.DBPath+"Data_"+fileIdx+".rf";
		File f= new File(fileName);

		int pageIdx;
		
		try {

			// Ouvrir le fichier en mode: read et write
			RandomAccessFile file= new RandomAccessFile(f, "rw");
			
			// La pageId
			pageIdx= (int)file.length() / DBParams.pageSize;
						
			do {
				// Se positionner à la fin du fichier
				file.seek(file.length());
				
				// Rajoute pageSize octets à la fin du fichier
				file.write(0);

			} while (file.length() < DBParams.pageSize * (pageIdx +1));
			
			// Fermer le fichier
			file.close();
				
			// Retourne la nouvelle page nouvellement rajoutée
			return new PageId(fileIdx, pageIdx);
			
		} catch (FileNotFoundException e) {
			System.out.println("Le fichier Data_"+fileIdx+".rf  n'existe pas, Veuillez déjà le crée.");
		} catch (IOException e) {
			System.err.println(e);
		}
		
		return null;
		
	}
	
	/**
	 * Remplir l'argument buffer avec le contenu disque de la page identifiée par l'argument pageId
	 * 
	 * @param pageId L'identifiant de la page
	 * @param buffer Un buffer
	 */
	public void readPage(PageId pageId, byte buffer[]) {
		// Récupérer le fichier où se trouve la page à lire 
		String fileName= DBParams.DBPath+"Data_"+pageId.getFileIdx()+".rf";
		File f= new File(fileName); 
		RandomAccessFile file;
						
		try {	
			// Ouvrir le fichier en lecture
			file = new RandomAccessFile(f, "r");
			
			// Position où on va lire
			int pos= DBParams.pageSize * pageId.getPageIdx();
			int i= pos;
			
			// Parcourir toute la pageId et lire 
			for (;i<pos+DBParams.pageSize; i++) {
				// Se positionner sur la pageId en question
				file.seek(pos);
				file.read(buffer);
			}
			
			//  Fermer le fichier
			file.close();
		} catch (FileNotFoundException e) {
			System.out.println("Le fichier Data_"+pageId.getFileIdx()+".rf  n'existe pas, Veuillez déjà le crée.");
		} catch (IOException e) {
			System.err.println(e);
		}
		
		
	}
	
	/**
	 * Ecrit le contenu de l'argument buffer dans le fichier et à la position indiqués par pageId
	 * 
	 * @param pageId L'identifiant de la page
	 * @param buffer Un buffer
	 */
	public void writePage(PageId pageId, byte buffer[]) {
		// Récupérer le fichier où se trouve la page à écrire
		String fileName= DBParams.DBPath+"Data_"+pageId.getFileIdx()+".rf";
		File f= new File(fileName);
		RandomAccessFile file;
		
		try {
			
			// Ouvrir le fichier en lecture et écriture 
			file= new RandomAccessFile(f, "rw");
			
			// Position où on va écrire
			int pos= DBParams.pageSize * pageId.getPageIdx();
			
			if (buffer.length<=DBParams.pageSize) {
				// Se positionner à la bonne place 
				file.seek(pos);
				// Ecrire le buffer dans la pageId en question
				file.write(buffer);
				
				/* Si la taille de l'ancien buffer est inférieur à la nouvelle, 
				 * alors il faudra écraser l'ancien buffer
				 */
				for (int i=0; i<DBParams.pageSize-buffer.length; i++) {
					file.seek(buffer.length+i);
					file.write(0);
				}	
			} else {
					System.out.println("Il n'y a pas assez de place dans la page\n");
			}
			
			//  Fermer le fichier
			file.close();
				
		} catch (FileNotFoundException e) {
			System.out.println("Le fichier Data_"+pageId.getFileIdx()+".rf  n'existe pas, Veuillez déjà le crée.");
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	
}
