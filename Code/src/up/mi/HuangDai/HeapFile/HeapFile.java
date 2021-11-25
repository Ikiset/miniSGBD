package up.mi.HuangDai.HeapFile;

import java.io.IOException;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import up.mi.HuangDai.GestionDesBuffers.BufferManager;
import up.mi.HuangDai.GestionDuDisque.DBParams;
import up.mi.HuangDai.GestionDuDisque.DiskManager;
import up.mi.HuangDai.GestionDuDisque.PageId;
import up.mi.HuangDai.Main.RelationInfo;
import up.mi.HuangDai.GestionDesRecords.Record;

/**
 * Gestion des pages de type Page Directory et un stockage de type bitmap 
 * pour des records de taille fixe
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class HeapFile {
	
	/**
	 * Relation
	 */
	private RelationInfo relInfo;
	
	/**
	 * Construit un HeapFile en fonction d'une relation
	 * @param relInfo
	 */
	public HeapFile(RelationInfo relInfo) {
		this.relInfo= relInfo;
	}
	
	/**
	 * Obtenir une relationInfo
	 * 
	 * @return Une relation
	 */
	public RelationInfo getRelation() {
		return relInfo;
	}
	
	/**
	 * 2 FONCTIONS: 
	 * - Création du fichier disque correspondant au HeapFile
	 * - Rajout d'une Header Page "vide" à ce fichier
	 */
	public void createNewOnDisk() {
		
		// Création du fichier disque correspondant au HeapFile
		try {
			DiskManager.getInstance().createFile(relInfo.getFileIdx());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Rajout d'une Header Page "vide" à ce fichier
		PageId pageId = DiskManager.getInstance().addPage(relInfo.getFileIdx());
		byte[] buffer= BufferManager.getInstance().GetPage(pageId);
		
		// Ecrire les valeurs 0 sur la page
		ByteBuffer buff = ByteBuffer.wrap(buffer);
		for(int i = 0; i<DBParams.pageSize/4; i++) {
			buff.putInt(0);
		}		
		
		// Libérer la page avec le flag dirty = 1
		BufferManager.getInstance().freePage(pageId,true);
	}
	
	/**
	 * Rajouter une page de données 
	 * @return Le PageId de cette page
	 */
	public PageId addDataPage() {
						
		// Rajouter une page du fichier disque correspondant
		PageId pageId= DiskManager.getInstance().addPage(relInfo.getFileIdx());
		byte[] bufferPageAjoutee= BufferManager.getInstance().GetPage(pageId);
		
		/* **** Actualiser les informations de la page ajouté **** */
		for(int i = 0; i<relInfo.getSlotCount(); i++) {
			bufferPageAjoutee[i] = 0;
		}
		
		// Libérer la page avec le flag dirty = 1
		BufferManager.getInstance().freePage(pageId,true);
		
		/* **** Actualiser les informations de la Header Page******** */

		// Chercher le HeaderPage situé à PageIdx= 0
		PageId headerPage= new PageId(relInfo.getFileIdx(), 0);
			
		// Obtenir sa page
		byte[] buffer= BufferManager.getInstance().GetPage(headerPage);

		ByteBuffer buff = ByteBuffer.wrap(buffer);
				
		// Ajout du nombre de slot disponible pour la page
		buff.putInt(pageId.getPageIdx()*4, relInfo.getSlotCount());
		
		// Actualiser le nombre de page
		buff.putInt(0, pageId.getPageIdx());
			
		// Libérer la page avec le flag dirty = 1
		BufferManager.getInstance().freePage(headerPage,true);
		
		
		return pageId;
			
	}
	
	/**
	 * Trouver une page libre
	 * 
	 * @return Page de données sur laquelle il reste des cases libres
	 */
	public PageId getFreeDataPageId() {		
		PageId dataPageId= null;
		
		// Chercher le HeaderPage situé à PageIdx= 0
		PageId headerPage= new PageId(relInfo.getFileIdx(), 0);
		
		// Obtenir sa page
		byte[] buffer= BufferManager.getInstance().GetPage(headerPage);
		ByteBuffer buff = ByteBuffer.wrap(buffer);
		
		// Récupérer le nombre de page dans HeaderPage
		int nbPage= buff.getInt(0);
		
		// Parcourir HeaderPage à la recherche d'une page avec un slot restant
		boolean t = false;
		for (int i=1; i<=nbPage && !t; i++) {
			// Si le nombre de slot n'est pas 0 alors on retourne cette page
			if (buff.getInt(i*4) > 0) {	// Multiplication par 4 : taille d'un int (4 octets)
				dataPageId= new PageId(relInfo.getFileIdx(), i);
				t = true;
			}
		}

		if(dataPageId == null)
			dataPageId = addDataPage();

		// Libérer la page avec le flag dirty = 0
		BufferManager.getInstance().freePage(headerPage, false);
		
		return dataPageId;
	}
	
	/**
	 * Ecrire l'enregistrement record dans la page de données identifiée par pageId
	 * 
	 * @param record Le record en question
	 * @param pageId Le pageId en question
	 * @return L'identifiant de l'enregistrement
	 */
	public Rid writeRecordToDataPage(Record record, PageId pageId) {
		
		Rid rid= new Rid();
		
		// Chercher le HeaderPage situé à PageIdx= 0 et charger son contenu
		PageId headerPage= new PageId(relInfo.getFileIdx(), 0);
		byte[] bufferHeaderPage= BufferManager.getInstance().GetPage(headerPage);
		ByteBuffer hp = ByteBuffer.wrap(bufferHeaderPage);
		
		// Récupère la page de données identifiée par pageId
		byte[] buffer= BufferManager.getInstance().GetPage(pageId);
		
		// Nombre de bitmap nécessaire dans une page
		int tailleBitmap = relInfo.getSlotCount();
		
		// Récupère tous les données des slots 
		ByteBuffer slot = ByteBuffer.wrap(buffer, tailleBitmap, buffer.length - tailleBitmap);

		// Parcourir le tableau de bitmap
		boolean placer = false;
		for (int i=0; i<relInfo.getSlotCount() && !placer; i++) {
			// Si bitmap = 0 alors c'est une case libres
			if (buffer[i] == 0) {
				
				// Ecrire le record dans la page de données libre
				record.writeToBuffer(slot, i);
				
				// Actualiser son bytemap à 1
				buffer[i] = 1;

				// Actualiser le nombre de slot restant de la page
				int index = pageId.getPageIdx() * 4;
				int value = hp.getInt(index);
				hp.putInt(index, --value);

				// Actualise l'identifiant du record
				rid.setSlotIdx(i);
				rid.setPageId(pageId);

				placer = true;
			}
		}

		// Libérer la page et le headerPage avec le flag dirty = 1
		BufferManager.getInstance().freePage(pageId, true);
		BufferManager.getInstance().freePage(headerPage, true);

		// Forcé l'arrêt dès qu'on a fini d'écrire
		return rid;
	}
	
	/**
	 * Retourner la liste de records stockés dans la page identifiée par pageId
	 * 
	 * @param pageId La pageId en question 
	 * 
	 * @return La liste de records stockés dans la page
	 */
	public List<Record> getRecordsInDataPage(PageId pageId) {
		List<Record> records= new ArrayList<Record>();
		
		// Récupère la page de données
		byte[] buffer= BufferManager.getInstance().GetPage(pageId);

		// Utilisation de ByteBuffer
		int tailleBitmap = relInfo.getSlotCount();
		ByteBuffer slot = ByteBuffer.wrap(buffer, tailleBitmap, buffer.length - tailleBitmap);

		// Parcourir le tableau de bitmaps
		for(int i = 0; i<relInfo.getSlotCount(); i++) {
			// Si le slot est occupé (1)
			if(buffer[i] == 1) {
				Record rec = new Record(relInfo);
				rec.readFromBuffer(slot, i);
				records.add(rec);
			}
		}

		BufferManager.getInstance().freePage(pageId, false);

		return records;
	}
	
	/**
	 * Insertion d'un record 
	 * @param record Une record
	 * @return L'identifiant du record
	 */
	public Rid InsertRecord(Record record) {
		Rid rid = new Rid();
		PageId pageId = getFreeDataPageId();
		rid = writeRecordToDataPage(record, pageId);
		return rid;
	}

	/**
	 * Sélection tous les records dans le heapFile
	 * @return La liste des records 
	 */
	public List<Record> GetAllRecords() {
		List<Record> records= new ArrayList<Record>();

		// Chercher le HeaderPage situé à PageIdx= 0
		PageId headerPage= new PageId(relInfo.getFileIdx(), 0);
		
		// Obtenir sa page
		byte[] buffer= BufferManager.getInstance().GetPage(headerPage);
		ByteBuffer buff = ByteBuffer.wrap(buffer);

		int nbPage = buff.getInt(0);

		// Parcourir toutes les pages
		for(int i = 1; i<=nbPage; i++) {	// i à utiliser comme pageIdx
			records.addAll(getRecordsInDataPage(new PageId(relInfo.getFileIdx(),i)));
		}

		BufferManager.getInstance().freePage(headerPage, false);

		return records;
	}

}
