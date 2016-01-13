import javax.management.relation.Role;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVParser {

    public void readDispo(Role r, ListActeur acteurs, int periodesParJour) {
        
        String donnees;
        if(r == Role.Enseignant) {
        	donnees = "C:/Users/hvallee/Documents/POSSICAT/POSSICAT/data/contraintesEnsLite.csv";
        } else {
        	donnees = "C:/Users/hvallee/Documents/POSSICAT/POSSICAT/data/contraintesTuteurLite.csv";
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
	 * @param nbSoutenancesEnseignants
	 * @param nbSoutenancesTuteurs
	 * @param relationsEnseignants
	 * @param relationsTuteurs
	 * @param N
	 */
	public int readCSV(ListActeur enseignants, ListActeur tuteurs, int N) {
        
        String donnees= "C:/Users/hvallee/Documents/POSSICAT/POSSICAT/data/donneesLite.csv";

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
                String ens_name = row[4];
                String tut_name = row[5];
                
                Enseignant e = (Enseignant)enseignants.get(ens_name);
                e.incNbSoutenances();
                e.incNbSoutenancesCandide();
                e.addRelation(tuteurs.get(tut_name));
                
                Tuteur t = (Tuteur)tuteurs.get(tut_name);
                t.incNbSoutenances();
                t.addRelation(enseignants.get(ens_name));
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

}