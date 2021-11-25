package up.mi.HuangDai.Main;

import java.util.ArrayList;
import java.util.List;

import up.mi.HuangDai.GestionDuDisque.DBParams;

public class DBInfoTest {
	public static void main(String[] args) {
		
		DBParams.DBPath = "../DB/";
		
		DBInfo.getInstance().setCompteur(2);
		
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
		
		DBInfo.getInstance().getRelationAllInfo().add(relationInfo);
		DBInfo.getInstance().finish();
		DBInfo.getInstance().init();
	}
}
