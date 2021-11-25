package up.mi.HuangDai.Main;


import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import up.mi.HuangDai.GestionDuDisque.DBParams;

/**
 * Contient les informations de schéma pour l'ensemble de notre base de donées
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class DBInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * Instance de DBManager
	 */
	private static DBInfo INSTANCE;
	
	/**
	 * Une liste de RelationInfo
	 */
	private List<RelationInfo> relationAllInfo= new ArrayList<RelationInfo>();
	
	/**
	 * Compteur de relations
	 */
	private int compteur=0 ;
	
	/**
	 * Instanciation de l'instance DBManager
	 * 
	 * @return L'instance de DBManager
	 */
	public static DBInfo getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DBInfo();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Obtenir la liste de relationInfo
	 * @return La liste de relationInfo
	 */
	public List<RelationInfo> getRelationAllInfo() {
		return relationAllInfo;
	}

	/**
	 * Obtenir le compteur
	 * @return Le compteur
	 */
	public int getCompteur() {
		return compteur;
	}

	public void setCompteur(int compteur) {
		this.compteur = compteur;
	}

	/**
	 * Initialisation de DBInfo
	 */
	@SuppressWarnings("unchecked")
	public void init() {		
		// Le fichier a trouvé
		String fileName= DBParams.DBPath+"Catalog.def";
		File f= new File(fileName);
		
		if (f.exists()) {
			try {
				FileInputStream file = new FileInputStream(f);
				ObjectInputStream os = new ObjectInputStream(file);
			
				this.compteur= os.readInt();
				this.relationAllInfo.clear();
				this.relationAllInfo=  (List<RelationInfo>) os.readObject();	
				
				file.close();
				os.close();
				
			} catch (FileNotFoundException e) {
				System.out.println("Le fichier Catalog.def n'existe pas, Veuillez déjà le crée.");
			} catch (ClassNotFoundException c) {
		        System.out.println("Classe non trouvé");
		        c.printStackTrace();
		        return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}

	
	/**
	 * S'occupe du "ménage"
	 * @throws IOException 
	 */
	public void finish() {
		
		// Le fichier a trouvé
		String fileName= DBParams.DBPath+"Catalog.def";
		File file= new File(fileName);
		
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream os = new ObjectOutputStream(f);
		
			os.writeInt(this.compteur);
			os.writeObject(this.relationAllInfo);
			
			os.close();
			f.close();
			
			
		} catch (FileNotFoundException e) {
			System.out.println("Le fichier Catalog.def n'existe pas, Veuillez déjà le crée.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Rajoute RelationInfo à la liste et actualise le compteur
	 * 
	 * @param relationInfo - Information "de schéma" d'une relation
	 */
	public void addRelation(RelationInfo relationInfo) {
		relationAllInfo.add(relationInfo);
		// Si la relation existe dans relationAllInfo alors incrémenter le compteur
		if (relationAllInfo.contains(relationInfo)) compteur ++;
	}
	
	/**
	 * Affiche DBInfo
	 */
	public void affiche() {
		System.out.println("Nombre total de relation: "+compteur+"\n");
		
        for (RelationInfo relationInfo : relationAllInfo) {
        	System.out.print("Nom de la relation: " + relationInfo.getNom()+"\n");
    		System.out.print("Nombre de colonne: " + relationInfo.getNbreColonne()+"\n");
    		
    		System.out.println("Les noms des colonnes: typeColonne");
    		for (int i=0; i<relationInfo.getNomColonne().size(); i++) {
    			System.out.println(relationInfo.getNomColonne().get(i)+":"+relationInfo.getTypeColonne().get(i));
    		}		
        }
	}

	public void reset() {
		relationAllInfo.clear();
		compteur = 0;
	}
}
