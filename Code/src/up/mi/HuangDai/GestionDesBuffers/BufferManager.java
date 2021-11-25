package up.mi.HuangDai.GestionDesBuffers;

import up.mi.HuangDai.GestionDuDisque.DBParams;

import up.mi.HuangDai.GestionDuDisque.DiskManager;
import up.mi.HuangDai.GestionDuDisque.PageId;

/**
 * Gestionnaire du tampon (buffer manager)= reponsable du transfert des pages
 * entre le disque et la RAM
 *
 * @author Huihui Huang, Julien Dai
 *
 */
public class BufferManager {
	
	/**
	 * Buffer Pool: Liste de l'ensemble des frames 
	 */
	private Frame frames[];
	
	/**
	 * Compteur de commande effectuer
	 * Pour voir les commande les plus récent
	 * À servir pour la politique de remplacement LRU
	 */
	private int compteur;

	/**
	 * Instance de BufferManager
	 */
	private static BufferManager INSTANCE;
	
	
	/**
	 * Construit BufferManager en initialisant la liste de Frame
	 */
	public BufferManager() {
		this.frames= new Frame[DBParams.frameCount];
		for(int i = 0; i<DBParams.frameCount; i++) {
			this.frames[i] = new Frame();
		}
		this.compteur= 0;
	}
	
	/**
	 * Instanciation de l'instance BufferManager
	 * 
	 * @return L'instance de BufferManager
	 */
	public static BufferManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BufferManager();
		}
		
		return INSTANCE;
	}
	
	
	/**
	 * Affiche l'ensemble des frames du Buffer Pool
	 */
	public void affiche() {
		for (int i=0; i<frames.length; i++) {
			System.out.println("FRAME n°"+(i+1)+":");
			
			if(frames[i].getPageId() != null) {
				System.out.println("Page: "+ frames[i].getPageId().getPageIdx());
				System.out.println("Pin_count : "+frames[i].getPinCount());
				System.out.println("Dirty : "+frames[i].isDirty());
				System.out.println("Indice de la commande la plus récente : "+frames[i].getNum());
				System.out.println("Contenu : " + new String(frames[i].getBuffer()));
			} else {
				System.out.println("Page: -");
				System.out.println("Pin_count : - ");
				System.out.println("Dirty : - ");
				System.out.println("Indice de la commande la plus récente : -");
				System.out.println("Contenu : - ");
			}	
			System.out.println();
		}
	}

	/**
	 * Obtenir l'ensemble des frames
	 * @return
	 */
	public Frame[] getFrames() {
		return frames;
	}
	
	
	/**
	 * Demande de page venant des couches plus hautes
	 * S'occupe du politique de remplacement
	 * 
	 * @param pageId La pageId concernée
	 * 
	 * @return Un des buffers associées à une frame 
	 */
	public byte[] GetPage(PageId pageId) {
		
		Frame frame= rechercheFrameParPageId(pageId);
		
		int pin_count=0;
			
		// Si la pageId existe dans le frame 
		if (frame != null) {
			
			// Incrémentation du Pin_count
			frame.setPinCount(frame.getPinCount() + 1);

			// Incrémentation du compteur
			frame.setNum(compteur++);

			return frame.getBuffer();

		// Si la pageId n'existe pas dans le frame
		} else {
			
			// Si il reste frame vide 
			if (haveFrameVide()) {
				
				// Recherche une frame vide
				frame= rechercheFrameVide();
				
				// Lire la page
				DiskManager.getInstance().readPage(pageId, frame.getBuffer());
				
				// Initialise le frame
				frame.setPageId(pageId);
				pin_count ++;
				frame.setPinCount(pin_count);
				frame.setDirty(false);
				frame.setNum(compteur++);

				return frame.getBuffer();

			// Si les frames sont tous remplit alors politique de remplacement LRU	
			} else {
				
				frame = politiqueDeRemplacementLRU();

				if(frame != null) {
					if(frame.isDirty()) {
						DiskManager.getInstance().writePage(pageId, frame.getBuffer());
					}
				
					//Imprime la page dans le buffer
					DiskManager.getInstance().readPage(pageId, frame.getBuffer());
	
					frame.setPageId(pageId);
					frame.setDirty(false);
					pin_count ++;
					frame.setPinCount(pin_count);
					frame.setNum(compteur++);

					return frame.getBuffer();
				}
				else {
					System.out.println("Pas de frame libre");
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 * Décrémentation du pin_count
	 * Actualisation du flag dirty de la page
	 * Actualisation des informations concernant la politique de remplacement
	 * 
	 * @param pageId La pageId concernée
	 * @param valdirty Le flag dirty
	 */
	public void freePage (PageId pageId, boolean valdirty) {
		Frame frame= rechercheFrameParPageId(pageId);
		
			if (frame != null) {
				
				// Décrémenter le pin_count
				int pinCount= frame.getPinCount();
				pinCount--;
				frame.setPinCount(pinCount);
				
				// Actualise le flag dirty 
				if (valdirty == true) {
					frame.setDirty(valdirty);
				}
				
				// Actualise des informations concernant la politique de remplacement
				frame.setNum(compteur++);
				
			}
	}
	
	/**
	 * L'écriture de toutes les pages dont le flag dirty = 1 sur disque
	 * La remise à 0 de tous les flags/informations et contenus des buffers 
	 */
	public void flushAll() {
		
		// Parcours de tous les frames dans le BufferManager
		for (Frame frame: frames) {
			// Si dirty=1 
			if (frame.isDirty()) {
				// ecriture de la page 
				DiskManager.getInstance().writePage(frame.getPageId(), frame.getBuffer());
			}
			
			// Remise à 0 de tous informations et flags du frame (buffer pool = vide)
			frame.setPinCount(0);
			frame.setDirty(false);
			frame.setPageId(null);
			frame.setCompteur(0);
			frame.setBuffer(new byte[DBParams.pageSize]);
		}
	}
	
	/**
	 * Recherche la frame dans le Buffer Pool
	 * @param pageId PageId à trouvé
	 * @return La frame si trouvé, null sinon
	 */
	private Frame rechercheFrameParPageId(PageId pageId) {
		for(Frame frame: frames) {
			if(frame.getPageId() != null) {
				if(frame.getPageId().equals(pageId))
					return frame;
			}
		}
		return null;
	}
	
	
	/**
	 * Recherche si dans le Buffer Pool il y a des frames vides
	 * @return FALSE si l'ensemble des frames sont non vide, TRUE si 1 frame vide
	 */
	private boolean haveFrameVide() {
		for(Frame frame: frames) {
			if(frame.getPageId() == null) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Recherche de l'emplacement frame vide et donne dans le paramètre
	 * @param frame Récupère l'emplacement de la frame vide
	 * @return true si on trouve une frame vide, false sinon
	 */
	private Frame rechercheFrameVide() {
		for(Frame frame: frames) {
			if(frame.getPageId()==null) {
				return frame;
			}
		}
		return null;
	}
	
	/**
	 * Recherche la frame à pinCount égale 0 la moins récente
	 * @return La frame avec une pinCount égale à 0, null si on ne trouve pas
	 */
	private Frame politiqueDeRemplacementLRU() {
		Frame tmp = null;
		for(Frame frame: frames) {
			if(frame.getPinCount() == 0) {
				if(tmp == null) {
					tmp = frame;
				}
				else if(tmp.getNum() > frame.getNum()) {
					tmp = frame;
				}
			}
		}
		return tmp;
	}
}
