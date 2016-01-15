import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Generator {

    public static void main(String[] args) {
        Generator generator = new Generator();
        generator.generateData(50);
        System.out.println("WORKING");
    }

    /**
     * @return array[data, teachersConstraints, companiesContraints]
     *
     */
    public void generateData(int number) {

        String students= "C:/Users/hvallee/Documents/POSSICAT/DATAGENERATOR/data/students.csv";
        String teachers= "C:/Users/hvallee/Documents/POSSICAT/DATAGENERATOR/data/teachers.csv";
        String companies= "C:/Users/hvallee/Documents/POSSICAT/DATAGENERATOR/data/companies.csv";

        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> teachersConstraints = new ArrayList<>();
        ArrayList<String> companiesConstraints = new ArrayList<>();


       //String[][] dataset = new String[][] {data, teachersConstraints, companiesConstraints};


        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            /* Parsing teachers and putting them in an array */
            String[][] teachersArray = new String[100][];
            br = new BufferedReader(new FileReader(teachers));
            br.readLine();
            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(cvsSplitBy);
                String[] instanceOfTeacher = {row[0], row[1]};
                teachersArray[i] = instanceOfTeacher;
                i++;
            }

             /* Parsing companies and putting them in an array */
            String[][] companiesArray = new String[100][3];
            br = new BufferedReader(new FileReader(companies));
            br.readLine();
            int j = 0;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(cvsSplitBy);
                String[] instanceOfCompany = {row[0], row[1]};
                companiesArray[j] = instanceOfCompany;
                j++;
            }

            FileWriter writer = new FileWriter("C:/Users/hvallee/Documents/POSSICAT/DATAGENERATOR/output/test.csv");
            writer.append("Etudiant,Nom,Prenom,Entreprise,enseignant,tuteur\n");
            br = new BufferedReader(new FileReader(students));
            br.readLine();

            int k = 0;
            int t = 0;
            int c = 0;
            /* We take a sample of all teachers and companies and we shuffle each array to have a unique solution */
            String[][] teachersForThisGeneration = Arrays.copyOfRange(teachersArray, 0, number/3);
            shuffleArray(teachersForThisGeneration);
            String[][] companiesForThisGeneration = Arrays.copyOfRange(teachersArray, 0, (number/5) * 4);
            shuffleArray(companiesForThisGeneration);
            while (k < number && (line = br.readLine()) != null) {


                // use comma as separator
                String[] row = line.split(cvsSplitBy);
                String name = row[0];
                String firstname = row[1];
                String email = firstname+"."+name+"@etudiant.univ-rennes1.fr";
                writer.append(email);
                writer.append(",");
                writer.append(name);
                writer.append(",");
                writer.append(firstname);
                writer.append(",");
                if(c%(companiesForThisGeneration.length-1) == 0) {
                    c = 0;
                }
                writer.append(companiesForThisGeneration[c][0]);
                writer.append(",");
                if(t%(teachersForThisGeneration.length-1) == 0) {
                    t = 0;
                }
                writer.append(teachersForThisGeneration[t][0]);
                writer.append(",");
                writer.append(companiesForThisGeneration[c][1]);

                writer.append("\n");
                c++;
                t++;
                k++;
            }
            writer.flush();
            writer.close();
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

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(String[][] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String[] a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}