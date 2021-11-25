package up.mi.HuangDai.GestionDesBuffers;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.io.RandomAccessFile;

import up.mi.HuangDai.GestionDuDisque.DBParams;
import up.mi.HuangDai.GestionDuDisque.DiskManager;
import up.mi.HuangDai.GestionDuDisque.PageId;

public class BufferManagerTests {

	public static void main(String[] args) throws IOException {
		DBParams.DBPath = "../DB/";
	    DBParams.pageSize = 4096;
	    DBParams.frameCount = 2;
	    
	    byte[] buff;
	    
		// Instanciation du DiskManager
		BufferManager BM = BufferManager.getInstance();
		
		DiskManager DM = DiskManager.getInstance();

		System.out.println("Travail sur le fichier Data_0.rf\n");
		
		// Création du fichier 
		DM.createFile(0);
		
		// Ajout des pages
		PageId pageId0 = new PageId(0,0);
		PageId pageId1 = new PageId(0,1);
		PageId pageId2 = new PageId(0,2);
		
		RandomAccessFile file = new RandomAccessFile("../DB/Data_0.rf", "r");
		
		while(file.length() < DBParams.pageSize*5) {
			DM.addPage(0);
		}
		
		System.out.println("********** ETAT ITNITIAL **********\n");
		BM.affiche();
		
		buff = BM.GetPage(pageId0);
		System.out.println("********* APRES GET(Page0) **********\n");
		BM.affiche();
		
		buff = BM.GetPage(pageId1);
		System.out.println("********* APRES GET(Page1) **********\n");
		BM.affiche();
		
		BM.freePage(pageId1, false);
		System.out.println("********* APRES FREE(Page1) **********\n");
		BM.affiche();
		
		buff = BM.GetPage(pageId2);
		System.out.println("********* APRES GET(Page2) **********\n");
		BM.affiche();
		
		BM.freePage(pageId0, true);
		System.out.println("********* APRES FREE(Page0) **********\n");
		BM.affiche();
		
		buff = BM.GetPage(pageId0);

		ByteBuffer buffer = ByteBuffer.wrap(buff);
		buffer.putInt(10);
		buffer.putChar('i');
		buff = BM.GetPage(pageId0);
		for(int i = 0; i<6; i++)
			System.out.println(i+" "+buff[i]);
		System.out.println("********* APRES GET(Page0) **********\n");
		BM.affiche();
		
		System.out.println("********* APRES FLUSH ALL **********\n");

		buff = BM.GetPage(pageId1);
		System.out.println("********* APRES GET(Page1) **********\n");
		BM.affiche();
		
		buff = BM.GetPage(pageId0);
		System.out.println("********* APRES GET(Page0) **********\n");
		BM.affiche();
		
		
		file.close();
		
	}
}
