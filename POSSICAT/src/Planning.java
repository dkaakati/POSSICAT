import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 */

/**
 * @author François Esnault
 * @date 7 janv. 2016
 */
public class Planning {

	int N, E, T, S;
	
	Map<String, Map<Integer, Boolean>> enseignants;
	Map<String, Integer> nbSoutenancesEnseignants;
	Map<String, List<Acteur>> relationsEnseignants;
	
	Map<String, Map<Integer, Boolean>> tuteurs;
	Map<String, Integer> nbSoutenancesTuteurs;
	Map<String, List<Acteur>> relationsTuteurs;
	
	Map<Integer, List<Boolean>> planning;
	enum Role {Enseignant, Tuteur};

	public Planning() {
		readCSV();
	};

	public void readCSV() {
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
		
		while(true) {
			insertData();
		}
	}
	
	public void insertData() {
		boolean inserted = false;
		Acteur a = getActeurMoinsDisponible();
		System.err.println("ACTEUR LE MOINS DISPO ["+a.getRole()+ " " + a.getName() + "]");
		List<Acteur> l = getActeursEnRelation(a);
		while(!inserted) {
			//Acteur b 
		}
	}
	
	public List<Acteur> getActeursEnRelation(Acteur a) {
		if(a.getRole() == Role.Enseignant) {
			return relationsEnseignants.get(a.getName());
		} else {
			return relationsTuteurs.get(a.getName());
		}
	}
	
	public Acteur getActeurMoinsDisponible() {
		Acteur acteur = new Acteur();
		acteur.setDispo(999);
		
		Set<String> keys = enseignants.keySet();
		for(String name : keys) {
			int dispo = getDispo(Role.Enseignant, name);
			if(dispo<acteur.getDispo()) {
				acteur.setDispo(dispo);
				acteur.setName(name);
				acteur.setRole(Role.Enseignant);
			}
		}
		
		keys = tuteurs.keySet();
		for(String name : keys) {
			int dispo = getDispo(Role.Tuteur, name);
			if(dispo<acteur.getDispo()) {
				acteur.setDispo(dispo);
				acteur.setName(name);
				acteur.setRole(Role.Tuteur);
			}
		}
		
		return acteur;
	}
	
	public int getDispo(Role a, String name) {
		if(a == a.Enseignant) {
			int nbSoutenance = nbSoutenancesEnseignants.get(name);
			
			Map<Integer, Boolean> m = enseignants.get(name);
			int nbPeriodeLibre = 0;
			Set<Integer> keys = m.keySet();
			for(int i : keys) {
				if(m.get(i)) {
					nbPeriodeLibre++;
				}
			}
			return nbPeriodeLibre-nbSoutenance*2;
		} else {
			int nbSoutenance = nbSoutenancesTuteurs.get(name);
			
			Map<Integer, Boolean> m = tuteurs.get(name);
			int nbPeriodeLibre = 0;
			Set<Integer> keys = m.keySet();
			for(int i : keys) {
				if(m.get(i)) {
					nbPeriodeLibre++;
				}
			}
			return nbPeriodeLibre-nbSoutenance;
		}
	}

	class Acteur {
		Role role;
		String name;
		int dispo;
		public Acteur() {};
		public Acteur(Role role, String name) {
			this.role = role;
			this.name = name;
		}
		public Role getRole() {
			return role;
		}
		public void setRole(Role role) {
			this.role = role;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getDispo() {
			return dispo;
		}
		public void setDispo(int dispo) {
			this.dispo = dispo;
		}
	}
}
