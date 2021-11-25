package up.mi.HuangDai.GestionDuDisque;


/**
 * Chaque fichier sera identifiée par son PageId
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class PageId {
	/**
	 * L'identifiant du fichier
	 */
	private int fileIdx;
	
	/**
	 * L'indice de la page dans le fichier
	 */
	private int pageIdx;
	
	/**
	 * Construit PageId qui initialise l'indice de la page et l'identifiant du fichier à 0
	 * 
	 * @param FileIdx L'identifiant du fichier
	 */
	public PageId(int fileIdx, int pageIdx) {
		this.fileIdx= fileIdx;
		this.pageIdx= pageIdx;
	}

	/**
	 * Lire l'identifiant du fichier
	 * 
	 * @return L'identifiant du fichier
	 */
	public int getFileIdx() {
		return fileIdx;
	}

	/**
	 * Lire l'indice de la page dans le fichier
	 * 
	 * @return L'indice de la page dans le fichier
	 */
	public int getPageIdx() {
		return pageIdx;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof PageId))
			return false;
		if(o == this)
			return true;
		
		PageId f = (PageId) o;
		return this.fileIdx == f.fileIdx && this.pageIdx == f.pageIdx; 
	}
	
}
