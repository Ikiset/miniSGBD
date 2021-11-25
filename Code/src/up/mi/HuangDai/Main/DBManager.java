package up.mi.HuangDai.Main;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import java.util.List;

import up.mi.HuangDai.GestionDesBuffers.BufferManager;
import up.mi.HuangDai.GestionDesBuffers.Frame;
import up.mi.HuangDai.GestionDesFiles.FileManager;
import up.mi.HuangDai.GestionDuDisque.DBParams;
import up.mi.HuangDai.GestionDuDisque.PageId;
import up.mi.HuangDai.HeapFile.HeapFile;
import up.mi.HuangDai.GestionDesRecords.Record;

/**
 * Point d'entrée de la logique spécifique SGBD
 * 
 * @author Huihui Huang, Julien Dai
 *
 */
public class DBManager {
	
	/**
	 * Instance de DBManager
	 */
	private static DBManager INSTANCE;
	
	/**
	 * Instanciation de l'instance DBManager
	 * 
	 * @return L'instance de DBManager
	 */
	public static DBManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DBManager();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Initialisation de l'instance 
	 */
	public void init() {
		DBInfo.getInstance().init();
		FileManager.getInstance().Init();
	}
	
	/**
	 * S'occupe du "ménage"
	 */
	public void finish() {
		DBInfo.getInstance().finish();
		BufferManager.getInstance().flushAll();
	}
	
	/**
	 * Procéder à une commande du SGBD
	 * @param chaine Chaîne de caractère qui correspond à une commande
	 */
	public void processCommand(String chaine) {
		
		String[] commande = chaine.split(" ");
		
		switch(commande[0]) {
			case "CREATEREL":
				if(commande.length > 2)
					cmdCreatel(chaine);
				else
					System.out.println("Instruction incomplet");
				break;
				
			case "RESET":
				cmdReset();
				break;
				
			case "INSERT":
				if(commande.length == 5 )
					cmdInsert(chaine);
				else
					System.out.println("Instruction incomplet");
				break;
				
			case "BATCHINSERT":
				cmdBatchinsert(chaine);
				break;
				
			case "SELECTALL":
				if(commande.length == 3 )
					cmdSelectall(chaine);
				else
					System.out.println("Instruction incomplet");
				break;
				
			case "SELECTS":
				if(commande.length == 5 )
					cmdSelects(chaine);
				else
					System.out.println("Instruction incomplet");
				break;
				
			case "SELECTC":
				if(commande.length > 4 )
					cmdSelectc(chaine);
				else
					System.out.println("Instruction incomplet");
				break;
			case "UPDATE":
				if(commande.length == 6)
					cmdUpdate(chaine);
				else
					System.out.println("Instruction incomplet");
				break;
				
			default:
				System.out.println("Mauvaise commande\n");
				break;	
		}
			
	}
	
	/**
	 * Crée une relation, c'est-à-dire les informations "de schéma"
	 * 
	 * @param nomRelation 	Nom de la relation
	 * @param nombre 		Nombre de colonne
	 * @param nomColonne 	Noms de colonne
	 * @param typeColonne 	Types de colonne
	 */
	public void createRelation(String nomRelation, int nombre, List<String> nomColonne, List<String> typeColonne) {
		int recordSize= calculRecordSize(typeColonne);
		int slotCount= calculSlotCount(recordSize);
		
		RelationInfo relationInfo= new RelationInfo(nomRelation, nombre, nomColonne, typeColonne, DBInfo.getInstance().getCompteur(), recordSize, slotCount);		
		
		// Ajoute une RelationInfo
		DBInfo.getInstance().addRelation(relationInfo);
		
		// Création d'une relation
		FileManager.getInstance().CreateRelationFile(relationInfo);
	}
	
	
	/**
	 * Gestion de la commande CREATEL:
	 * Creation d'une relation
	 * 
	 * @param chaine Une chaine de caractère
	 */
	private void cmdCreatel(String chaine) {
		
		String[] commande = chaine.split(" ");
		
		String nomRelation= commande[1];
		
		Boolean creeRelation= true;
		
		List<String> nomColonne= new ArrayList<String>();
		List<String> typeColonne= new ArrayList<String>();
		
		// Liste des heapFiles existant
		List<HeapFile> heapFiles = FileManager.getInstance().getHeapFiles();
		
		for (HeapFile heapFile: heapFiles) {
			if (heapFile.getRelation().getNom().equals(nomRelation)) {
				creeRelation= false;
			}
		}
		
		if (creeRelation) {
			for (int i=2; i<commande.length; i++) {
				
				// Séparateur :
				String[] mot= commande[i].split(":");					
				
				// Ajout du nom de colonne dans la liste nomColonne
				nomColonne.add(mot[0]);
				
				// Ajout du type de colonne dans la lisye typeColonne
				typeColonne.add(mot[1]);
			}
			
			createRelation(commande[1], commande.length-2, nomColonne, typeColonne);
		} else {
			System.out.println("Relation \"" + nomRelation + "\" existant. \n");
		}
	}
	
	/**
	 * Gestion de la commande RESET: 
	 * Tout remettre dans un état qui correspond à un premier lancement de votre application
	 */
	private void cmdReset() {

		// Supression des fichiers .rf et le fichier Catalog.def
		File path= new File(DBParams.DBPath);
		if (path.exists()) {
			File [] files= path.listFiles();
			
			for (int i=0; i< files.length; i++) {
				files[ i ].delete();
			}
		}
		
		// Reset BufferManager
		// Parcours de tous les frames dans le BufferManager
		for(Frame frame :BufferManager.getInstance().getFrames()) {
				// Remise à 0 de tous informations et flags du frame (buffer pool = vide)
				frame.setPinCount(0);
				frame.setDirty(false);
				frame.setPageId(null);
				frame.setCompteur(0);
				frame.setBuffer(new byte[DBParams.pageSize]);
		}

		// Reset DBInfo
		DBInfo.getInstance().reset();

		// Reset FileManager
		FileManager.getInstance().reset();
	}
	
	/**
	 * Gestion de la commande INSERT:
	 * Insertion d'un record dans une relation
	 * 
	 * @param chaine Une chaine de caractère
	 */
	private void cmdInsert(String chaine) {
		String[] commande = chaine.split(" ");
		
		Boolean insert= false;
		
		if(commande[1].equals("INTO")) {
			if (commande[3].equals("RECORD")) {
				// Nom de la relation
				String nomRelation= commande[2];
				
				// Liste des heapFiles existant
				List<HeapFile> heapFiles = FileManager.getInstance().getHeapFiles();
				
				for (HeapFile heapFile: heapFiles) {
						// Si la relation trouvé 
						if (heapFile.getRelation().getNom().equals(nomRelation)) {
							
							insert= true;
							
							String valeurs= commande[4];
							
							// Valeurs du record
							valeurs= commande[4].replaceAll("[()]", "");
							String[] valeursRecord= valeurs.split(",");
							
							if (valeursRecord.length == heapFile.getRelation().getNbreColonne()) {
								
								// Liste des values du Record
								List<String> values= new ArrayList<String>();
									
								for (int i=0; i<valeursRecord.length; i++) {
									values.add(valeursRecord[i]);
								}
									
								Record record= new Record(heapFile.getRelation(), values);
									
								// Insertion d'un record dans une relation
								FileManager.getInstance().InsertRecordInRelation(record, nomRelation);
								
							} else {
								System.out.println("INSERT impossible ! Il y a "+heapFile.getRelation().getNbreColonne()+" colonne dans la relation \""+heapFile.getRelation().getNom()+"\". \n");
							}
							
						}
				} 
				
				if (!insert) {
					System.out.println("Relation \""+nomRelation+"\" introuvable. \n");
				}
			
				
			} else {
				System.out.println("Commande \""+commande[3]+"\" inexistant après \"INSERT INTO "+ commande[2] + "\", voulez-vous dire \"RECORD\" ?\n");
			}
			
		} else {
			System.out.println("Commande \""+commande[1]+"\" inexistant après \"INSERT\", voulez-vous dire \"INTO\" ? \n");
		}
			
	}
	
	/**
	 * Gestion de la commande BATCHINSERT:
	 * Insertion de plusieurs records dans une relation
	 * 
	 * @param chaine Une chaine de caractère
	 */
	private void cmdBatchinsert(String chaine) {
		String[] commande = chaine.split(" ");
		
		if(commande[1].equals("INTO")) {
			if (commande[3].equals("FROM") && commande[4].equals("FILE")) {
				String fichier= "../"+commande[5];
				
				String nomRelation= commande[2];
				
				Boolean relationTrouve= false;
				
				// Liste des heapFiles existant
				List<HeapFile> heapFiles = FileManager.getInstance().getHeapFiles();
				
				for (HeapFile heapFile: heapFiles) {
						// Si la relation trouvé 
						if (heapFile.getRelation().getNom().equals(nomRelation)) {
							
							relationTrouve= true;
							
							// Lecture du fichier
							try (BufferedReader br= new BufferedReader(new FileReader(fichier))){
								
								String records= null;
								
								while ((records= br.readLine()) != null) {
									String[] r= records.split(",");
									
									if (r.length == heapFile.getRelation().getNbreColonne()) {
										// Liste des values du Record
										List<String> values= new ArrayList<String>();
										
										for (int i=0; i<r.length; i++) {
											values.add(r[i]);
										}
										
										Record record= new Record(heapFile.getRelation(), values);
										
										// Insertion des record dans une relation
										FileManager.getInstance().InsertRecordInRelation(record, nomRelation);
									}
									else {
										System.out.println("INSERT impossible ! Il y a "+heapFile.getRelation().getNbreColonne()+" colonne dans la relation \""+heapFile.getRelation().getNom()+"\". \n");
									}
									
								}
								
							} catch (FileNotFoundException e) {
								System.out.println("Le fichier "+fichier+" n'a pas été trouvé. \n");
							} catch (IOException e) {
								System.err.println(e);
							}
						} 
				}
				
				if (!relationTrouve) {
					System.out.println("Relation \""+nomRelation+"\" introuvable. \n");
				}
				
			} else {
				System.out.println("Commande \""+commande[3]+" "+commande[4]+"\" inexistant. \n");
			}
		} else {
			System.out.println("Commande \""+commande[1]+"\" inexistant. \n");
		}
		
	}
	
	/**
	 * Affiche tous les records de la relation passer en paramètre, 
	 * Si la relation n'est pas trouvé alors il indique la relation n'est introuvable
	 * 
	 * @param nomRelation La relation a cherché
	 */
	private void cmdSelectall(String chaine) {
		String[] commande = chaine.split(" ");
		
		if(commande[1].equals("FROM")) {
			String nomRelation= commande[2];
			
			Boolean relationTrouve= false;
			
			// Liste des heapFiles existant
			List<HeapFile> heapFiles = FileManager.getInstance().getHeapFiles();
			
			for (HeapFile heapFile: heapFiles) {
				
				// Si la relation trouvé 
				if (heapFile.getRelation().getNom().equals(nomRelation)) {
					
					relationTrouve= true;
					
					// Affecte tous les records de la relation à res, null si la relation n'existe pas
					List<Record> res = FileManager.getInstance().SelectAllFromRelation(nomRelation);
					if(res != null) {

						// Affiche tout les records avec toString redéfinie
						for(Record rec : res) {
							System.out.println(rec.toString());
						}

						// Affiche le nombre total de record
						System.out.println("Total records="+res.size()+".\n");
					} else {
						
						System.out.println("Total records=0. \n");
					}
					
				}
			}
			
			if (!relationTrouve) {
				System.out.println("Relation \""+nomRelation+"\" introuvable. \n");
			}
			
		} else {
			System.out.println("Commande \""+commande[1]+"\" inexistant. \n");
		}
	}
	
	/**
	 * Affiche tous les records de la relation avec la valeur de la colonne égale la valeur passer en paramètre, 
	 * Si la relation n'est pas trouvé alors il indique la relation n'est introuvable, pareil pour la colonne
	 * 
	 * @param nomRelation 	La relation a cherché
	 * @param nomColonne 	La colonne a comparé
	 * @param valeur 		La valeur de la colonne a cherché
	 */
	private void cmdSelects(String chaine) {
		
		String[] commande = chaine.split(" ");
		
		if(commande[1].equals("FROM")) {
			
			if(commande[3].equals("WHERE")) {
				
				String[] valeurs = commande[4].split("=");
				
				if(valeurs.length == 2) {
					String nomRelation= commande[2];
					String nomColonne= valeurs[0];
					String valeur= valeurs[1];
					
					Boolean relationTrouve= false;
					
					// Liste des heapFiles existant
					List<HeapFile> heapFiles = FileManager.getInstance().getHeapFiles();
					
					for (HeapFile heapFile: heapFiles) {
						// Si la relation trouvé 
						if (heapFile.getRelation().getNom().equals(nomRelation)) {
							
							relationTrouve= true;
							
							List<Record> res = FileManager.getInstance().SelectAllFromRelation(nomRelation);
							
							boolean recordNull= false;
							
							for (Record record: res) {
								if (record != null) {
									recordNull = true;
								}
							}
							
							if(recordNull) {
								int totalRecord = 0;
								int indiceColonne = -1;

								boolean t = false;
								for(int i = 0; i<res.get(0).getRelInfo().getNbreColonne() && !t; i++) {
									if(nomColonne.equals(res.get(0).getRelInfo().getNomColonne().get(i))) {
										indiceColonne = i;
										t = true;
									}
								}

								if(!(indiceColonne < 0)) {
									for(Record rec : res) {
										if(rec.getValues().get(indiceColonne).equals(valeur)) {
											System.out.println(rec.toString());
											totalRecord++;
										}
									}
									System.out.println();
									System.out.println("Total records="+totalRecord+". \n");
								} else {
									System.out.println("Colonne \""+nomColonne+"\" introuvable. \n");
								}
							} else {
								System.out.println("Total records=0.\n");
							}
							
						}
					}
					
					if (!relationTrouve) {
						System.out.println("Relation \""+nomRelation+"\" introuvable. \n");
					}
	
				} else {
					System.out.println("La structure de \""+commande[4]+"\" doit être nomColonne=values (sans espace et un simple \"=\"). \n");
				}
			} else {
				System.out.println("Commande \""+commande[3]+"\" inexistant après \"SELECTC FROM\", voulez-vous dire \"WHERE\" ?\n");
			}
		} else {
			System.out.println("Commande \""+commande[1]+"\" inexistant après \"SELECTC\", voulez-vous dire \"FROM\" ?\n");
		}

	}
	
	/**
	 * Traite la commande SELECTC du mini sgbd
	 * 
	 * @param commande Commande entré
	 */
	private void cmdSelectc(String chaine) {
		
		String[] commande = chaine.split(" ");
		
		if(commande[1].equals("FROM")) {
			if(commande[3].equals("WHERE")) {
				// SELECTC FROM R WHERE A>0 AND A<10 AND CC=2
				String nomRelation = commande[2];
				int tailleCritere = ((commande.length-4)+1)/2;
				String[] colonne = new String[tailleCritere];	// Liste des colonnes
				String[] value = new String[tailleCritere];		// Liste des valeurs
				String[] operation = new String[tailleCritere];	//1- "=" | 2- "<" |3- ">" |4- "<=" | 5- ">="
				
				// Indice des critères (i * 2 + 4)
				// Indice des AND (i * 2 + 5) et nombre de AND = tailleCritere - 1

				//Vérificartion des AND
				boolean b = false;
				for(int i = 0; i<tailleCritere-1 && !b; i++) {
					if(!commande[i*2+5].equals("AND")) {
						b = true;	// Mauvais syntaxe
						System.out.println("Mauvais syntaxe \""+commande[i*2+5]+"\". \n");
					}
				}
				if(!b) {
					for(int i = 0; i<tailleCritere && !b; i++) {
						String critere = commande[i*2+4];
						if(critere.contains("<=")) {
							String[] split = critere.split("<=");
							colonne[i] = split[0];
							operation[i] = "<=";
							value[i] = split[1];
						}
						else if(critere.contains(">=")) {
							String[] split = critere.split(">=");
							colonne[i] = split[0];
							operation[i] = ">=";
							value[i] = split[1];
						}
						else if(critere.contains("<")) {
							String[] split = critere.split("<");
							colonne[i] = split[0];
							operation[i] = "<";
							value[i] = split[1];
						}
						else if(critere.contains(">")) {
							String[] split = critere.split(">");
							colonne[i] = split[0];
							operation[i] = ">";
							value[i] = split[1];
						}
						else if(critere.contains("=")) {
							String[] split = critere.split("=");
							colonne[i] = split[0];
							operation[i] = "=";
							value[i] = split[1];
						}
						else {
							System.out.println("On ne trouve pas d'opérateur sur le critère numéro "+(i+1)+" : "+critere);
							b = true;
						}
					}
					if(!b) {
						selectc(nomRelation, colonne, value, operation);
					}
				}
			}
			else
				System.out.println("Commande \""+commande[3]+"\" inexistant après \"SELECTC FROM\", voulez-vous dire \"WHERE\" ?\n");
		}
		else
			System.out.println("Commande \""+commande[1]+"\" inexistant après \"SELECTC\", voulez-vous dire \"FROM\" ?\n");
	}
	
	/**
	 * Affiche tous les records de la relation avec les critères respectés, 
	 * Si la relation n'est pas trouvé alors il indique la relation n'est introuvable, pareil pour la colonne
	 * 
	 * @param nomRelation 	Nom de la relation à chercher
	 * @param nomColonne 	Noms des colonnes des critères
	 * @param valeur 		Les valeurs des critères a comparé
	 * @param operation 	Les opérateurs des critères
	 */
	private void selectc(String nomRelation, String[] nomColonne, String[] valeur, String[] operation) {
		
		// Liste des heapFiles existant
		List<HeapFile> heapFiles = FileManager.getInstance().getHeapFiles();
		
		Boolean relationTrouve= false;
		
		for (HeapFile heapFile: heapFiles) {
			// Si la relation trouvé 
			if (heapFile.getRelation().getNom().equals(nomRelation)) {
				
				relationTrouve= true;
				
				// Liste des Records de la relation en question
				List<Record> res = FileManager.getInstance().SelectAllFromRelation(nomRelation);

				// Vérifie si la relation a donné des records (Soit c la relation mal écrit, soit la relation n'a pas de valeur)
				if(res != null) {
					
					boolean recordNull= false;
					
					for (Record record: res) {
						if (record != null) {
							recordNull = true;
						}
					}
					
					if (recordNull) {
						int totalRecord = 0;	// Nombre total de Record (à incrementer)
						int indiceColonne[] = new int[nomColonne.length];	// Indice de la colonne à comparer
						for(int i = 0; i<nomColonne.length; i++) {
							indiceColonne[i] = -1;
						}
	
						
						boolean t = true;		// Condition d'arrêt des boucles && indication des nom de colonne trouvé
						for(int j = 0; j<nomColonne.length && t; j++) {	// Pour chaque indiceColonne il doit trouvé un indice
							t = false;
							for(int i = 0; i<res.get(0).getRelInfo().getNbreColonne() && !t; i++) {	// Recherche dans tout les relations
								if(res.get(0).getRelInfo().getNomColonne().get(i).equals(nomColonne[j])) {
									// Colonne trouver
									indiceColonne[j] = i;
									t = true;
								}
							}
							
							if(!t) {
								System.out.println("Colonne \""+nomColonne[j]+"\" du critère "+(j+1)+" introuvable.\n");
							}
						}
	
						// Vérifie si les colonnes sont trouvés
						if(t) {
							// Parcourir la liste de record de la relatio
							for(Record rec : res) {
								t = true;	// Réutilisation du boolean t
								for(int i = 0; i<nomColonne.length && t; i++) {
									switch(operation[i]) {
									case "<=":
										if(!(rec.getValues().get(indiceColonne[i]).compareTo(valeur[i]) <= 0))
											t = false;
										break;
									case ">=":
										if(!(rec.getValues().get(indiceColonne[i]).compareTo(valeur[i]) >= 0))
											t = false;
										break;
									case "<":
										if(!(rec.getValues().get(indiceColonne[i]).compareTo(valeur[i]) < 0))
											t = false;
										break;
									case ">":
										if(!(rec.getValues().get(indiceColonne[i]).compareTo(valeur[i]) > 0))
											t = false;
										break;
									case "=":
										if(!(rec.getValues().get(indiceColonne[i]).compareTo(valeur[i]) == 0))
											t = false;
										break;
									}
								}
								
								if(t) {
									System.out.println(rec.toString());
									totalRecord++;
								}
							}
							
						
							System.out.println();
							System.out.println("Total records="+totalRecord+".\n");
						}
					}
					else
						System.out.println("\nTotal records=0.");
				
				}
				
			}
		}
		
		if (!relationTrouve) {
			System.out.println("Relation \""+nomRelation+"\" introuvable. \n");
		}
		
	}

	/**
	 * Traite la commande UPDATE du mini SGBD
	 * @param commande Commande entré
	 */
	private void cmdUpdate(String chaine) {
		
		String[] commande = chaine.split(" ");
		
		// UPDATE R SET BB=rrr WHERE CC=2
		if(commande[2].equals("SET")) {
			if(commande[4].equals("WHERE")) {
				update(commande[1], commande[3], commande[5]);
			}
			else
				System.out.println("Commande \""+commande[4]+"\" inexistant après \"UPDATE"+ commande[1] +" SET\", voulez-vous dire \"WHERE\" ?\n");
		}
		else
			System.out.println("Commande \""+commande[1]+"\" inexistant après \"UPDATE\", voulez-vous dire \"SET\" ?\n");

	}

	/**
	 * Mise à jour d'une relation
	 * @param nomRelation Nom de la relation
	 * @param set Commande SET
	 * @param critere Critère de la mise à jour 
	 */
	private void update(String nomRelation, String set, String critere) {
		
		// Liste des heapFiles existant
		List<HeapFile> heapFiles = FileManager.getInstance().getHeapFiles();
		
		Boolean relationTrouve= false;
		
		for (HeapFile heapFile: heapFiles) {
			// Si la relation trouvé 
			if (heapFile.getRelation().getNom().equals(nomRelation)) {
				
				relationTrouve= true;
				
				// Prend tout les records de la relations donnée 
				List<Record> res = FileManager.getInstance().SelectAllFromRelation(nomRelation);
			
				// Vérifie si la relation a donné des records (Soit c la relation mal écrit, soit la relation n'a pas de valeur)
				if(res != null) {
					
					boolean recordNull= false;
					
					for (Record record: res) {
						if (record != null) {
							recordNull = true;
						}
					}
					
					if (recordNull) {
						
					
						int totalRecord = 0;
						int indiceColonneSet = -1;
						int indiceColonneCritere = -1;
						String[] splitSet = set.split("=");
						String[] splitCritere = critere.split("=");
				
						boolean trouveColonneSet = false;
						boolean trouveColonneCritere = false;
						
						for(int i = 0; i<res.get(0).getRelInfo().getNbreColonne() && !(trouveColonneSet && trouveColonneCritere); i++) {	// Recherche dans tout les relations
							if(res.get(0).getRelInfo().getNomColonne().get(i).equals(splitSet[0])) {
								// Colonne trouver
								indiceColonneSet = i;
								trouveColonneSet = true;
							}
							if(res.get(0).getRelInfo().getNomColonne().get(i).equals(splitCritere[0])) {
								// Colonne trouver
								indiceColonneCritere = i;
								trouveColonneCritere = true;
							}
						}
						
						// Vérifie si les colonnes sont trouvés
						if(trouveColonneSet && trouveColonneCritere) {
							// Parcourir la liste de record de la relation
							int i = 0;
							for(Record rec : res) {
								if((rec.getValues().get(indiceColonneCritere).equals(splitCritere[1]))) {
									rec.getValues().set(indiceColonneSet, splitSet[1]);
									byte[] pageAUpdate = BufferManager.getInstance().GetPage(new PageId(rec.getRelInfo().getFileIdx(), (i/rec.getRelInfo().getSlotCount()) + 1 ));
									ByteBuffer slot = ByteBuffer.wrap(pageAUpdate, rec.getRelInfo().getSlotCount(), pageAUpdate.length - rec.getRelInfo().getSlotCount());
									rec.writeToBuffer(slot, i%rec.getRelInfo().getSlotCount());
									BufferManager.getInstance().freePage(new PageId(rec.getRelInfo().getFileIdx(), (i/rec.getRelInfo().getSlotCount()) + 1 ), true);
									totalRecord++;
								}
								i++;
							}
							
							System.out.println();
							System.out.println("Total records="+totalRecord+".\n");
						}
						else {
							if(!trouveColonneSet)
								System.out.println("Colonne \""+splitSet[0]+"\" non retrouvé. \n");
							if(!trouveColonneCritere)
								System.out.println("Colonne \""+splitCritere[0]+"\" non retrouvé. \n");
						}
					}
					else
						System.out.println("\nTotal records=0");
					
				}
			}
		}
		
		if (!relationTrouve) {
			System.out.println("Relation \""+nomRelation+"\" introuvable. \n");
		}
	
	}
	
	
	/**
	 * Calcul la taille d'un record
	 * @param typeColonne Types de colonne
	 */
	private int calculRecordSize(List<String> typeColonne) {
		int taille=0;
		
		for (String type: typeColonne) {
			if (type.contains("int")) {
				taille +=4;
			// Si type= float
			} else if (type.contains("float")) {
				taille +=4;
			// Si type= string
			} else if (type.contains("string")) {
				// Récupère la longueur du string
				StringBuilder sb= new StringBuilder();
				for (int j=6; j<type.length(); j++) {
					char longueur = type.charAt(j);
					sb.append(longueur);
				}
				int longueur= Integer.parseInt(sb.toString());
				taille += 2 * longueur;
			}
		}
		
		return taille;
	}
	
	/**
	 * Calcul du nombre de cases (slots) sur une page
	 * @param recordSize
	 * @return Le nombre de cases sur une page
	 */
	private int calculSlotCount(int recordSize) {
		int nombreCase;
		// 1 record= recordSize + 1 bytemap 
		int tailleRecord= recordSize + 1;
	
		nombreCase= DBParams.pageSize/tailleRecord;
		
		return nombreCase;
	}
	
}
