package up.mi.HuangDai.HeapFile;

import up.mi.HuangDai.GestionDuDisque.PageId;

/**
 * L'identifiant d'un enregistrement (fichier)
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class Rid {
	/**
	 * Page à laquelle appartient le record
	 */
	private PageId pageId;
	
	/**
	 * Indice de la case où le record est stocké
	 */
	private int slotIdx;
	
	/**
	 * Construit l'identifiant d'un enregistrement en fonction de la pageId et slotIdx
	 * @param pageId
	 * @param slotIdx
	 */
	public Rid() {
		this.pageId= null;
		this.slotIdx= 0;
	}

	/**
	 *  Obtenir la page à laquelle appartient le record
	 * @return La page à laquelle appartient le record
	 */
	public PageId getPageId() {
		return pageId;
	}

	/**
	 * Ecrire la page à laquelle appartient le record
	 * @param pageId
	 */
	public void setPageId(PageId pageId) {
		this.pageId = pageId;
	}

	/**
	 * Obtenir l'indice de la case où le record est stocké
	 * @return Indice de la case où le record est stocké
	 */
	public int getSlotIdx() {
		return slotIdx;
	}

	/**
	 * Ecrire l'indice de la case où le record est stocké
	 * @param slotIdx
	 */
	public void setSlotIdx(int slotIdx) {
		this.slotIdx = slotIdx;
	}
}
