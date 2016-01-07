import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CSVParser {

    public void readEnseignants(int periodesParJour) {
        Map<String, Map<Integer, Boolean>> enseignants;
        String donnees = "C:/Users/hvallee/Documents/POSSICAT/POSSICAT/data/donneesLite.csv";
        String contraintesEns = "C:/Users/hvallee/Documents/POSSICAT/POSSICAT/data/contraintesEnsLite.csv";
        String contraintesTut = "C:/Users/hvallee/Documents/POSSICAT/POSSICAT/data/contraintesTuteurLite.csv";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(contraintesEns));
            enseignants = new HashMap<>();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] enseignant = line.split(cvsSplitBy);
                if(!enseignant[0].equals("")) {
                    Map<Integer, Boolean> contraintes = new HashMap<>();
                    int periodeEnCours = 0;
                    for(int i = 1; i < enseignant.length; i++) {
                        for(int j = 0; j < periodesParJour; j++) {

                            if(enseignant[i].equals("X")) {
                                contraintes.put(periodeEnCours, true);
                            }
                            else if(enseignant[i].equals("M")){
                                if(j <= periodesParJour/2 -1) {
                                    contraintes.put(periodeEnCours, true);
                                }
                                else {
                                    contraintes.put(periodeEnCours, false);
                                }
                            }
                            else if(enseignant[i].equals("AM")){
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
                    enseignants.put(enseignant[0], contraintes);
                }

            }
            System.out.print(enseignants);
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

}