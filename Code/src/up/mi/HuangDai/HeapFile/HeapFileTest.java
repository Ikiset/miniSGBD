package up.mi.HuangDai.HeapFile;
import java.util.ArrayList;


import java.util.List;
import up.mi.HuangDai.GestionDuDisque.DBParams;
import up.mi.HuangDai.GestionDuDisque.PageId;
import up.mi.HuangDai.Main.DBInfo;
import up.mi.HuangDai.Main.DBManager;
import up.mi.HuangDai.Main.RelationInfo;
import up.mi.HuangDai.GestionDesBuffers.BufferManager;
import up.mi.HuangDai.GestionDesRecords.Record;

public class HeapFileTest {

	public static void main(String[] args) {
		
		// Chemin vers le dossier DB
		DBParams.DBPath = "../DB/";
						
		// Taille d'une page
		DBParams.pageSize= 4096;
		
		// Case
		DBParams.frameCount=2;
		
		List<String> nomColonne= new ArrayList<String>();
		nomColonne.add("C1");
		nomColonne.add("C2");
		nomColonne.add("C3");
		
		List<String> typeColonne= new ArrayList<String>();
		typeColonne.add("int");
		typeColonne.add("string3");
		typeColonne.add("float");
	

		DBManager.getInstance().createRelation("R", 3, nomColonne, typeColonne);
		
		
		List<RelationInfo> relations= DBInfo.getInstance().getRelationAllInfo();
		
		for (RelationInfo relInfo: relations ) {
			
			List<String> values= new ArrayList<String>();
			values.add("3");
			values.add("abc");
			values.add("3.3");
			
			BufferManager BM = BufferManager.getInstance();
			HeapFile heapFile= new HeapFile(relInfo);
		   
			// Création du fichier disque correspondant au HeapFile
			// Rajout d'une Header Page "vide" à ce fichier
			heapFile.createNewOnDisk();

			// Ajout d'une page
			heapFile.addDataPage();
			
			System.out.println("\nTaille de la relation: "+relInfo.getRecordSize());
			System.out.println("\nNombre de cases sur la page: "+relInfo.getSlotCount());

			// Affiche contenu des Frames des trois premiers valeurs
			System.out.println("\nAffichage buffer frames dans le POOL: ");
			for(int i = 0; i<12; i++) {
				System.out.print(BM.getFrames()[0].getBuffer()[i]);
				if(i%4 == 3) {
					System.out.println();
				}
			}

			// Chercher une page libre
			PageId pageLibre= heapFile.getFreeDataPageId();
		   
			if (pageLibre != null) {
				System.out.print("\nPage libre: ");
				System.out.println(pageLibre.getPageIdx());
			} else {
				System.out.print("\nIl n'y pas de page libre");
			}
		   
			PageId pageId= new PageId(relInfo.getFileIdx(), 1);
		  
			System.out.println("\n********** Ecriture du record dans la page " +pageId.getPageIdx()+" **********");
			Record record= new Record(relInfo, values);
			Rid rid= heapFile.writeRecordToDataPage(record, pageId);
			System.out.println("L'identifiant du record: "+rid.getSlotIdx());
			System.out.println("Page à laquelle appartient le record: "+rid.getPageId().getPageIdx());
			System.out.println("Values 1st : "+ record.getValues().get(0));
			
			List<Record> records= heapFile.getRecordsInDataPage(pageId);
			
			System.out.println("\n***** Liste des record stockés sur la page "+pageId.getPageIdx()+" ******* ");
			int i=1;
			for (Record r: records) {
				System.out.print("Record "+i+":");
				System.out.println(r.getValues());
				i++;
			}
		
			System.out.println();
			
			System.out.println("***** Après insertion du record ancien ******");
			Rid rid2= heapFile.InsertRecord(record);
			System.out.println("L'identifiant du record: "+rid2.getSlotIdx());
			System.out.println("Page à laquelle appartient le record: "+rid2.getPageId().getPageIdx());
			
			List<Record> recordAll= heapFile.GetAllRecords();
			
			System.out.println("\n***** Liste des records stockés sur le headerPage ******* ");
			
			int j=1;
			for (Record r: recordAll) {
				System.out.print("Record "+j+":");
				System.out.println(r.getValues());
				j++;
			}
			
			   
		  } 
		
		BufferManager.getInstance().flushAll();
		
	}
}
