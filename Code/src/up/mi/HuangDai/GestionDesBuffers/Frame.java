package up.mi.HuangDai.GestionDesBuffers;

import up.mi.HuangDai.GestionDuDisque.DBParams;
import up.mi.HuangDai.GestionDuDisque.PageId;

/**
 * Gestion des cases
 *
 * @author Huihui Huang, Julien Dai
 *
 */
public class Frame {
	
	/**
	 * La zone mémoire correspondant à la case
	 * = là où on va copier le contenur des pages disques
	 */
	private byte[] buffer;
	
	/**
	 * L'indice de la page d'un fichier qui s"y trouve chargée
	 */
	private PageId pageId;
	
	/**
	 * Pin_count: Compteur d'utilisations d'une page
	 * Une page peut être remplacée ssi son pin_count= 0
	 */
	private int pinCount;
	
	/**
	 * Flag dirty:Page modifié ou non 
	 */
	private boolean dirty;
	
	/**
	 * Compteur pour le politique de remplacement 
	 */
	private int compteur;
	
	/**
	 * Indice de la commande la plus récent
	 */
	private int num;
	
	/**
	 * Construit un Frame vide
	 */
	public Frame() {
		this.buffer = new byte[DBParams.pageSize];
		this.pageId = null;
		this.dirty= false;
	}

	/**
	 * Obtenir le contenu de la case
	 * 
	 * @return Le contenu de la case
	 */
	public byte[] getBuffer() {
		return buffer;
	}

	/**
	 * Ecrire du contenu dans la case
	 * @param buffer - Le contenu de la case
	 */
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	/**
	 * Obtenir la pageId
	 * @return La pageId
	 */
	public PageId getPageId() {
		return pageId;
	}

	/**
	 * Ecrire l'indice de la page d'un fichier dans la case
	 * @param pageId - L'indice de la page dans un fichier
	 */
	public void setPageId(PageId pageId) {
		this.pageId = pageId;
	}

	/**
	 * Obtenir le pin_count
	 * @return Le compteur d'utilisation d'une page
	 */
	public int getPinCount() {
		return pinCount;
	}

	/**
	 * Ecrire le pin_count
	 * @param pinCount Le compteur d'utilisation d'une page
	 */
	public void setPinCount(int pinCount) {
		this.pinCount = pinCount;
	}

	/**
	 * Obtenir le flag dirty
	 * @return Flag dirty 
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Ecrire le flag dirty 
	 * @param dirty Le flag dirty
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	/**
	 * Obtenir le compteur 
	 * @return Le compteur
	 */
	public int isCompteur() {
		return compteur;
	}
	
	/**
	 * Ecrire le compteur 
	 * @param compteur Le compteur
	 */
	public void setCompteur(int compteur) {
		this.compteur= compteur;
	}
	
	/**
	 * Obtenir l'indice de la commande la plus récent 
	 * @return L'indice de la commande la plus récent
	 */ 
	public int getNum() {
		return num;
	}
	
	/**
	 * Ecrire l'indice de la commande la plus récent
	 * @param num L'indice de la commande la plus récent
	 */
	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Frame))
			return false;
		if(o == this)
			return true;
		
		Frame f = (Frame) o;
		return this.pageId.equals(f.pageId);
	}
}
