import java.io.*;
import java.util.ArrayList;

public class Generator {

    public static void main(String[] args) {
        System.out.println("WORKING");
    }

    /**
     * @return array[data, teachersConstraints, companiesContraints]
     *
     */
    public void generateData() {

        String students= "C:/Users/hvallee/Documents/POSSICAT/src/data/students.csv";
        String teachers= "C:/Users/hvallee/Documents/POSSICAT/src/data/teachers.csv";
        String companies= "C:/Users/hvallee/Documents/POSSICAT/src/data/companies.csv";

        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> teachersConstraints = new ArrayList<>();
        ArrayList<String> companiesConstraints = new ArrayList<>();


       //String[][] dataset = new String[][] {data, teachersConstraints, companiesConstraints};


        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            FileWriter writer = new FileWriter("test.csv");
            writer.append("Etudiant,Nom,Prenom\n");
            br = new BufferedReader(new FileReader(students));
            br.readLine();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = line.split(cvsSplitBy);
                String name = row[0];
                String firstname = row[1];
            }
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