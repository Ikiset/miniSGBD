package up.mi.HuangDai.GestionDesRecords;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


import up.mi.HuangDai.Main.RelationInfo;

/**
 * Correspond à un enregistrement, c'est-à-dire à une ligne dans une table
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class Record {
	
	/**
	 * Une relation à laquelle appartient le record
	 */
	private RelationInfo relInfo;
	
	/**
	 * Liste des valeurs du Record
	 */
	private List<String> values;
	
	/**
	 * Construit un Record à partir d'une relation
	 * @param relInfo Une relation à laquelle appartient le record
	 */
	public Record (RelationInfo relInfo, List<String> values) {
		this.relInfo= relInfo;
		this.values= values;
	}

	 /**
	  * Construit un Record null
	  */
	 public Record(RelationInfo relInfo) {
		 this.relInfo = relInfo;
		 this.values= new ArrayList<String>();
	 }

	 /**
	  * Obtenir la relation à laquelle appartient le record
	  * @return Une relation
	  */
	 public RelationInfo getRelInfo() {
	  return relInfo;
	 }

	 /**
	  * Obtenir la liste des valeurs du Record
	  * @return La liste des valeurs
	  */
	 public List<String> getValues() {
	  return values;
	 }

	/**
	 * Ecrire les valeurs du Record dans le buffer, l'une après l'autre, à partir de position
	 * 
	 * @param buffer Un buffer
	 * @param position Un entier correspondant à une position dans le buffer
	 */
	public void writeToBuffer (ByteBuffer buff, int position) {
		// Se positionner à la bonne position
		int pos = 0; //Taille d'un Record Size
		
		for(String i : relInfo.getTypeColonne()) {
			if (i.contains("int")) {
				pos += 4;
			// Si type= float
			} else if (i.contains("float")) {
				pos+= 4;
			// Si type= string
			} else if (i.contains("string")) {
				// Récupère la longueur du string
				StringBuilder sb= new StringBuilder();
				for (int j=6; j<i.length(); j++) {
					char longueur = i.charAt(j);
					sb.append(longueur);
				}
				int longueur= Integer.parseInt(sb.toString());
				pos += 2 * longueur;
			}
		}
		buff.position(pos*position+buff.position());
		// Parcourir les types de colonnes de RelationInfo
		for (int i=0; i<relInfo.getTypeColonne().size(); i++) {
			
			// Si type= int
			if (relInfo.getTypeColonne().get(i).contains("int")) {
				buff.putInt(Integer.parseInt(values.get(i)));
				
			// Si type= float
			} else if (relInfo.getTypeColonne().get(i).contains("float")) {
				buff.putFloat(Float.parseFloat(values.get(i)));
				
			// Si type= string
			} else if (relInfo.getTypeColonne().get(i).contains("string")) {
				
				// Récupère la longueur du string
				StringBuilder sb= new StringBuilder();
				for (int j=6; j<relInfo.getTypeColonne().get(i).length(); j++) {
					char longueur= relInfo.getTypeColonne().get(i).charAt(j);
					sb.append(longueur);
				}
				
				
				int longueur= Integer.parseInt(sb.toString());
				
				// Si la longueur dans le type de colonne = longueur de values 
				// alors mettre le values les uns après les autres
				if (longueur == values.get(i).length()) {
					for (int j=0;j<values.get(i).length();j++) {
						char c2= values.get(i).charAt(j);
						buff.putChar(c2);
					}	
				}
				
			}
		}
	}
	
	/**
	 * Lire les valeurs du Record depuis le buffer et position
	 * 
	 * @param buffer Un buffer
	 * @param position Un entier correspondant à une position dans le buffer
	 */
	public void readFromBuffer (ByteBuffer buff, int position) {
		// Se positionner à la bonne position
		int pos = relInfo.getRecordSize(); //Taille d'un Record Size

		buff.position((pos*position)+relInfo.getSlotCount());

		 // Parcourir les types de colonnes de RelationInfo
		  for (int i=0; i<relInfo.getTypeColonne().size(); i++) {
		   // Si type= int
		   if (relInfo.getTypeColonne().get(i).contains("int")) {
		    values.add(Integer.toString(buff.getInt()));
		    
		   // Si type= float
		   } else if (relInfo.getTypeColonne().get(i).contains("float")) {
		    values.add(Float.toString(buff.getFloat()));
		    
		   // Si type= string
		   } else if (relInfo.getTypeColonne().get(i).contains("string")) {
		    
		    // Récupère la longueur du string
		    StringBuilder sb= new StringBuilder();
		    for (int j=6; j<relInfo.getTypeColonne().get(i).length(); j++) {
		     char longueur= relInfo.getTypeColonne().get(i).charAt(j);
		     sb.append(longueur);
		    }
		    
		    int longueur= Integer.parseInt(sb.toString());
		    
		    // Si la longueur dans le type de colonne = longueur de values 
		    // alors mettre le values les uns après les autres
		    // Récupère la longueur du string
		    StringBuilder sb2= new StringBuilder();
		    for (int j=0;j<longueur;j++) {
		     char c2= buff.getChar();
		     sb2.append(c2);
		    } 
		    
		    values.add(sb2.toString());
		    
		   }
		  }
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i<values.size(); i++) {
			str.append(values.get(i));
			if(i < values.size()-1)
				str.append(" ; ");
			else
				str.append(".");
		}
		return str.toString();
	}
}

