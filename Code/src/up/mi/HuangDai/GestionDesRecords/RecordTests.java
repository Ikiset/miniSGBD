package up.mi.HuangDai.GestionDesRecords;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import up.mi.HuangDai.Main.RelationInfo;

public class RecordTests {
		public static void main(String[] args) {
  
			byte [] buff = new byte[4096];
			ByteBuffer buffer = ByteBuffer.wrap(buff);
  
			List<String> nomColonne= new ArrayList<String>();
			nomColonne.add("C1");
			nomColonne.add("C2");
  
			List<String> typeColonne= new ArrayList<String>();
			typeColonne.add("int");
			typeColonne.add("string3");
  
			List<String> values1= new ArrayList<String>();
			values1.add("3");
			values1.add("abc");
  
			List<String> values2= new ArrayList<String>();
			values2.add("1");
			values2.add("xyz");
  
			RelationInfo relationInfo= new RelationInfo("R", 2, nomColonne, typeColonne);
			Record record1= new Record(relationInfo, values1);
			Record record2= new Record(relationInfo, values2);
			Record record3= new Record(relationInfo);

  
			record1.writeToBuffer(buffer, 0);
			record2.writeToBuffer(buffer, 0);
			record3.readFromBuffer(buffer, 0);

			System.out.println(record3.toString());

		}

}