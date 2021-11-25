package up.mi.HuangDai.GestionDesFiles;

import up.mi.HuangDai.HeapFile.*;

import up.mi.HuangDai.Main.*;
import up.mi.HuangDai.GestionDesRecords.Record;

import java.util.ArrayList;
import java.util.List;

public class FileManager {

	/**
	 * Liste de HeapFile
	 */
	private List<HeapFile> heapFiles = new ArrayList<HeapFile>();

	/**
	 * Instance de FileManager
	 */
	private static FileManager INSTANCE;

	/**
	 * Instanciation de l'instance BufferManager
	 * 
	 * @return L'instance de BufferManager
	 */
	public static FileManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FileManager();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Obtenir la liste les heapFiles 
	 * @return la liste des heapFiles
	 */
	public List<HeapFile> getHeapFiles() {
		return heapFiles;
	}



	public static FileManager getINSTANCE() {
		return INSTANCE;
	}



	/**
	 * Initialisation de FileManager
	 */
	public void Init() {
		for(RelationInfo relInfo : DBInfo.getInstance().getRelationAllInfo()) {
			heapFiles.add(new HeapFile(relInfo));
		}
	}
	
	/**
	 * Création d'une relation
	 * @param relInfo
	 */
	public void CreateRelationFile (RelationInfo relInfo) {
		HeapFile nouv = new HeapFile(relInfo);
		if(!heapFiles.contains(nouv)) {
			heapFiles.add(nouv);
		}
		nouv.createNewOnDisk();
	}

	/**
	 * Insertion d'un Record dans une relation
	 * 
	 * @param record Un record
	 * @param relName Une chaine de carcatère
	 * 
	 * @return L'identifiant du record
	 */
	public Rid InsertRecordInRelation (Record record, String relName) {
		for(HeapFile hf : heapFiles) {
			if(hf.getRelation().getNom().equals(relName)) {
				return hf.InsertRecord(record);
			}
		}
		return null;
	}

	/**
	 * Lister tous les records d'une relation
	 * 
	 * @param relName Une chaine de caractère
	 * 
	 * @return La liste des records d'une relation
	 */
	public List<Record> SelectAllFromRelation (String relName) {
		List<Record> res = null;
		for(HeapFile hf : heapFiles) {
			if(hf.getRelation().getNom().equals(relName)) {
				res = hf.GetAllRecords();
			}
		}
		return res;
	}

	/**
	 * RESET des heapFiles
	 */
	public void reset() {
		heapFiles.clear();
	}
	
	
}
