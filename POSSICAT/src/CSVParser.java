import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class CSVParser {

    public void readDispo(Role r, ListActeur acteurs, int periodesParJour) {
        
        String donnees;
        if(r == Role.Enseignant) {
        	donnees = "data/contraintesEnsLite2.csv";
        } else {
        	donnees = "data/contraintesTuteurLite2.csv";
        }
       

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(donnees));
            while ((line = br.readLine()) != null) {
                // use comma as separator
            	line += " ";
                String[] row = line.split(cvsSplitBy);
                if(!row[0].equals("")) {
                    Map<Integer, Boolean> contraintes = new HashMap<>();
                    int periodeEnCours = 0;
                    for(int i = 1; i < row.length; i++) {
                        for(int j = 0; j < periodesParJour; j++) {

                            if(row[i].contains("X")) {
                                contraintes.put(periodeEnCours, true);
                            }
                            else if(row[i].contains("M")){
                                if(j <= periodesParJour/2 -1) {
                                    contraintes.put(periodeEnCours, true);
                                }
                                else {
                                    contraintes.put(periodeEnCours, false);
                                }
                            }
                            else if(row[i].contains("AM")){
                                if(j > periodesParJour/2 -1) {
                                    contraintes.put(periodeEnCours, true);
                                }
                                else {
                                    contraintes.put(periodeEnCours, false);
                                }
                            }
                            else {
                                contraintes.put(periodeEnCours, false);
                            }
                            periodeEnCours++;
                        }
                    }
                    
            		
                    Acteur a = null;
                    if(r == Role.Enseignant) {
                    	a = new Enseignant(row[0]);
                    }
                    if(r == Role.Tuteur) {
                    	a = new Tuteur(row[0]);
                    }
                    a.setDisponibilites(contraintes);
                    acteurs.list.add(a);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	/**
	 * @param enseignants
	 * @param tuteurs
	 * @param etudiants 
	 * @param nbSoutenancesEnseignants
	 * @param nbSoutenancesTuteurs
	 * @param relationsEnseignants
	 * @param relationsTuteurs
	 * @param N
	 */
	public int readCSV(ListActeur enseignants, ListActeur tuteurs, List<Student> etudiants, int N) {
        
        String donnees= "data/donneesLite2.csv";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        int nbSoutenance = 0;
        
        try {

            br = new BufferedReader(new FileReader(donnees));
            br.readLine();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = line.split(cvsSplitBy);
                String stu_name = row[0];
                String ens_name = row[4];
                String tut_name = row[5];
                
                Enseignant e = (Enseignant)enseignants.get(ens_name);
                e.incNbSoutenances();
                e.incNbSoutenancesCandide();
                e.addRelation(tuteurs.get(tut_name));
                
                Tuteur t = (Tuteur)tuteurs.get(tut_name);
                t.incNbSoutenances();
                t.addRelation(enseignants.get(ens_name));
                
                etudiants.add(new Student(stu_name, e, t));
                
                nbSoutenance++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		
        return nbSoutenance;
	}

	/**
	 * @param planning
	 * @throws IOException 
	 */
	public void writeData(Map<Integer, List<Creneau>> planning) throws IOException {
		/*StringBuilder sb = new StringBuilder("Période" + ";" 
				+ "Etudiant" + ";"
				+ "Tuteur" + ";"
				+ "Enseignant" + ";"
				+ "Candide" + "\n");*/
		StringBuilder sb = new StringBuilder();
		Set<Integer> periodes = planning.keySet();
		for(int periode : periodes) {
			if(periode%8==0) {
				sb.append(",,,,,,,,,,,\n,,,,,,,,,,,\nJOUR " + ((periode/8)+1) + ",,,,,,,,,,,\n");
				sb.append(",SALLE 0,,,,,SALLE1,,,,,\n");
			}
			List<Creneau> creneaux = planning.get(periode);
			for(Creneau c : creneaux) {
					if(c.getSalle()==1) {
						sb.append(c.getHoraire() + ",");
					}
					sb.append(c.getStudent() + ","
							+ c.getTuteur() + ","
							+ c.getEnseignant() + ","
							+ c.getCandide() + ", ,");
			}
			if(creneaux.size()==1) {
				sb.append(",,,,,");
			}
			sb.append("\n");
		}
		System.out.println(sb.toString());
		Files.write(sb, new File("data/output.csv"), Charsets.UTF_8);
	}

}