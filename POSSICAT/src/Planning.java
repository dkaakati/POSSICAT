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
	enum Role {Enseignant, Tuteur, Candide};

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
			Acteur b = getActeurEnRelationLeMoinsDispo(l);
			List<Integer> creneauxCommuns = creneauCommun(a, b);
			while(!creneauxCommuns.isEmpty()) {
				Creneau c = getCreneau(a, b, creneauxCommuns);
				if(c != null) {
					System.err.println("CRENEAU TROUVE");
					System.err.println("ON INSERE :");
					System.err.println("\t" + c.getA().getRole() + " " + c.getA().getName());
					System.err.println("\t" + c.getB().getRole() + " " + c.getB().getName());
					System.err.println("\t" + c.getC().getRole() + " " + c.getC().getName());
					System.err.println("\tA la période " + c.getP());
				}
			}
		}
	}
	
	public boolean salleLibre(int p) {
		List<Boolean> a = planning.get(p);
		for(boolean b : a) {
			if(b == true) {
				return true;
			}
		}
		return false;
	}
	
	public Creneau getCreneau(Acteur a, Acteur b, List<Integer> creneauxCommuns) {
		
		Acteur enseignant;
		if(a.getRole() == Role.Enseignant) {
			enseignant = a;
		} else {
			enseignant = b;
		}
		
		Set<String> keys = enseignants.keySet();
		for(String name : keys) {
			if(name != enseignant.getName()) {
				Map<Integer, Boolean> map = enseignants.get(name);
				for(int p : creneauxCommuns) {
					if(map.get(p)) {
						if(salleLibre(p)) {
							return new Creneau(p, a, b, new Acteur(Role.Candide, name));
						}
					}
				}
			}
		}
		return null;
	}
	
	public List<Integer> creneauCommun(Acteur a, Acteur b) {
		Map<Integer, Boolean> dispoA;
		Map<Integer, Boolean> dispoB;
		
		List<Integer> creneauxCommuns = new ArrayList<Integer>();
		
		if(a.getRole() == Role.Enseignant) {
			dispoA = enseignants.get(a.getName());
			dispoB = tuteurs.get(b.getName());
		} else {
			dispoB = enseignants.get(b.getName());
			dispoA = tuteurs.get(a.getName());
		}
		
		Set<Integer> periodes = dispoA.keySet();
		for(int p : periodes) {
			if(dispoA.get(p) && dispoB.get(p)) {
				creneauxCommuns.add(p);
			}
		}
		return creneauxCommuns;
	}
	
	public Acteur getActeurEnRelationLeMoinsDispo(List<Acteur> l) {
		Acteur acteur = new Acteur();
		acteur.setDispo(999);

		for(Acteur a : l) {
			int dispo = getDispo(a.getRole(), a.getName());
			if(dispo<acteur.getDispo()) {
				acteur = a;
			}
		}
		return acteur;
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
	
	class Creneau {
		Acteur a, b, c;
		int p;
		public Creneau(int p,Acteur a, Acteur b, Acteur c) {
			this.p = p;
			this.a = a;
			this.b = b;
			this.c = c;
		}
		public Acteur getA() {
			return a;
		}
		public void setA(Acteur a) {
			this.a = a;
		}
		public Acteur getB() {
			return b;
		}
		public void setB(Acteur b) {
			this.b = b;
		}
		public Acteur getC() {
			return c;
		}
		public void setC(Acteur c) {
			this.c = c;
		}
		public int getP() {
			return p;
		}
		public void setP(int p) {
			this.p = p;
		}
	}
}
