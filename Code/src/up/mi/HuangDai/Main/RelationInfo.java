package up.mi.HuangDai.Main;

import java.io.Serializable;
import java.util.List;

/**
 * Garde les informations "de schéma" d'une relation
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class RelationInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * Nom de la relation
	 */
	private String nom;
	
	/**
	 * Nombre de colonnes 
	 */
	private int nbreColonne;
	
	/**
	 * Liste des noms de colonnes 
	 */
	private List<String> nomColonne;
	
	/**
	 * Liste des types de colonnes 
	 */
	private List<String> typeColonne;
	
	/**
	 * Indice du fichier disque qui stocke la relation
	 */
	private int fileIdx;
	
	/**
	 * Taille d'un record
	 */
	private int recordSize;
	
	/**
	 * Nombre de cases (slots) sur une page
	 */
	private int slotCount;
	
	/**
	 * Construit une RelationInfo en fonction:
	 * - nom de la relation
	 * - nombre de colonne 
	 * - nom et type de colonne données
	 * 
	 * @param nom Nom de la relation
	 * @param nbreColonne Nombre de colonnes
	 * @param nomColonne Liste des noms de colonnes
	 * @param typeColonne Liste des types de colonnes
	 */
	public RelationInfo(String nom, int nbreColonne, List<String> nomColonne, List<String> typeColonne) {
		this.nom= nom;
		this.nbreColonne= nbreColonne;
		this.nomColonne= nomColonne;
		this.typeColonne= typeColonne;
	}
	
	/**
	 * Construit une RelationInfo en fonction:
	 * - nom de la relation
	 * - nombre de colonne 
	 * - nom et type de colonne données
	 * - indice du fichier disque qui stocke la relation
	 * - taille d'un record
	 * - nombre de cases sur une page
	 * 
	 * @param nom Nom de la relation
	 * @param nbreColonne Nombre de colonnes
	 * @param nomColonne Liste des noms de colonnes
	 * @param typeColonne Liste des types de colonnes
	 * @param fileIdx Indice du fichier disque qui stocke la relation
	 * @param recordSize Taille d'un record
	 * @param slotCount Nombre de cases sur une page
	 */
	public RelationInfo(String nom, int nbreColonne, List<String> nomColonne, List<String> typeColonne, int fileIdx, int recordSize, int slotCount) {
		this(nom, nbreColonne, nomColonne, typeColonne);
		this.fileIdx= fileIdx;
		this.recordSize= recordSize;
		this.slotCount= slotCount;
	}
	
	/**
	 * Construit une RelationInfo en fonction:
	 * - indice du fichier disque qui stocke la relation
	 * - taille d'un record
	 * - nombre de cases sur une page
	 * 
	 * @param fileIdx Indice du fichier disque qui stocke la relation
	 * @param recordSize Taille d'un record
	 * @param slotCount Nombre de cases sur une page
	 */
	public RelationInfo(int fileIdx, int recordSize, int slotCount) {
		this.fileIdx= fileIdx;
		this.recordSize= recordSize;
		this.slotCount= slotCount;
	}

	/**
	 * Obtenir la liste des types de colonnes
	 * @return La liste des types de coloones
	 */
	public List<String> getTypeColonne() {
		return typeColonne;
	}

	/**
	 * Obtenir le nom de la relation
	 * @return Le nom de la relation
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Obtenir le nombre de colonne de la relation
	 * @return Le nombre de colonne de la relation
	 */
	public int getNbreColonne() {
		return nbreColonne;
	}

	/** 
	 * Obtenir la liste des noms de colonnes de la relation
	 * @return La liste des noms de colonnes
	 */
	public List<String> getNomColonne() {
		return nomColonne;
	}

	/**
	 * Obtenir l'ndice du fichier disque qui stocke la relation
	 * @return Indice du fichier disque qui stocke la relation
	 */
	public int getFileIdx() {
		return fileIdx;
	}

	/**
	 * Obtenir la taille d'un record
	 * @return Taille d'un record
	 */
	public int getRecordSize() {
		return recordSize;
	}

	/**
	 * Obtenir le nombre de cases (slots) sur une page
	 * @return Nombre de cases (slots) sur une page
	 */
	public int getSlotCount() {
		return slotCount;
	}
}
