import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */

/**
 * @author François Esnault
 * @date 6 janv. 2016
 */
public class Main {

	static int N, E, T, S;
	static Map<String, Map<Integer, Boolean>> enseignants;
	static Map<String, Map<Integer, Boolean>> tuteurs;
	static Map<Integer, List<Boolean>> planning;
	
	public static void main(String[] args) {
		
		// Ouvrir le fichier CSV
		
		
		// On récupère N le nombre de période
		N = 10;
		
		// On récupère E le nombre d'enseignants
		E = 5;
		for(int i = 0; i < E; i++) {
			// On récupère le nom de l'enseignant
			String nom = "Test";
			// On créé une map pour chaque enseignant
			Map<Integer, Boolean> m = new HashMap<Integer, Boolean>();
			// On met à true ou false selon les disponibilités
			enseignants.put(nom, m);
		}
		
		// On récupère T le nombre de tuteurs
		T = 5;
		for(int i = 0; i < T; i++) {
			// On récupère le nom du tuteur
			String nom = "Test";
			// On créé une map pour chaque tuteur
			Map<Integer, Boolean> m = new HashMap<Integer, Boolean>();
			// On met à true ou false selon les disponibilités
			tuteurs.put(nom, m);
		}
		
		// On récupère le nombre de salle disponibles
		S = 2;
		
		// On créé le planning
		planning = new HashMap<Integer, List<Boolean>>();
		// Pour chaque période, on insère la liste des salles
		for(int i = 0; i < N; i++) {
			List<Boolean> l = new ArrayList<Boolean>();
			for(int s = 0; s < S; s++) {
				l.add(false);
			}
			planning.put(i, l);
		}
		
		
	}

}
