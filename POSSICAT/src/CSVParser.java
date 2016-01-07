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

    public void readDispo(Role r, Map<String, Map<Integer, Boolean>> acteurs, int periodesParJour) {
        
        String donnees;
        if(r == Role.Enseignant) {
        	donnees = "data/contraintesEnsLite.csv";
        } else {
        	donnees = "data/contraintesTuteurLite.csv";
        }
       

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(donnees));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = line.split(cvsSplitBy);
                if(!row[0].equals("")) {
                    Map<Integer, Boolean> contraintes = new HashMap<>();
                    int periodeEnCours = 0;
                    for(int i = 1; i < row.length; i++) {
                        for(int j = 0; j < periodesParJour; j++) {

                            if(row[i].equals("X")) {
                                contraintes.put(periodeEnCours, true);
                            }
                            else if(row[i].equals("M")){
                                if(j <= periodesParJour/2 -1) {
                                    contraintes.put(periodeEnCours, true);
                                }
                                else {
                                    contraintes.put(periodeEnCours, false);
                                }
                            }
                            else if(row[i].equals("AM")){
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
                    acteurs.put(row[0], contraintes);
                }

            }
            System.out.print(acteurs);
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

        System.out.println("Done");
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
	public void readCSV(Map<String, Map<Integer, Boolean>> enseignants,
			Map<String, Map<Integer, Boolean>> tuteurs,
			Map<String, Integer> nbSoutenancesEnseignants,
			Map<String, Integer> nbSoutenancesTuteurs,
			Map<String, List<Acteur>> relationsEnseignants,
			Map<String, List<Acteur>> relationsTuteurs, int N) {
		
		Set<String> nomsEnseignants = enseignants.keySet();
		for(String name : nomsEnseignants) {
			nbSoutenancesEnseignants.put(name, 0);
			relationsEnseignants.put(name, new ArrayList<Acteur>());
		}
		
		Set<String> nomsTuteurs = tuteurs.keySet();
		for(String name : nomsTuteurs) {
			nbSoutenancesTuteurs.put(name, 0);
			relationsTuteurs.put(name, new ArrayList<Acteur>());
		}
        
        String donnees= "data/donneesLite.csv";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        
        try {

            br = new BufferedReader(new FileReader(donnees));
            br.readLine();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = line.split(cvsSplitBy);
                String enseignant = row[4];
                String tuteur = row[5];
                
                int nbSoutenanceEns = nbSoutenancesEnseignants.get(enseignant);
                nbSoutenancesEnseignants.put(enseignant, nbSoutenanceEns+2);
                
                List<Acteur> relationsEns = relationsEnseignants.get(enseignant);
                relationsEns.add(new Acteur(Role.Tuteur, tuteur));
                
                int nbSoutenanceTut = nbSoutenancesTuteurs.get(tuteur);
                nbSoutenancesTuteurs.put(enseignant, nbSoutenanceTut++);
                
                List<Acteur> relationsTut = relationsTuteurs.get(tuteur);
                relationsTut.add(new Acteur(Role.Enseignant, enseignant));
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

}