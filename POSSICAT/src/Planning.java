import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Planning {

	int N, E, T, S;
	boolean isFinised = false;
	int nbInserted = 0;
	
	Map<String, Map<Integer, Boolean>> enseignants;
	Map<String, Integer> nbSoutenancesEnseignants;
	Map<String, Integer> nbSoutenancesInitEnseignants;
	Map<String, List<Acteur>> relationsEnseignants;
	
	Map<String, Map<Integer, Boolean>> tuteurs;
	Map<String, Integer> nbSoutenancesTuteurs;
	Map<String, List<Acteur>> relationsTuteurs;
	
	Map<Integer, List<Creneau>> planning;

	public Planning() {
		readCSV();
	};

	public void readCSV() {
		enseignants = new HashMap<String, Map<Integer, Boolean>>();
		nbSoutenancesEnseignants = new HashMap<String, Integer>();
		nbSoutenancesTuteurs = new HashMap<String, Integer>();
		
		tuteurs = new HashMap<String, Map<Integer, Boolean>>();
		relationsEnseignants = new HashMap<String, List<Acteur>>();
		relationsTuteurs = new HashMap<String, List<Acteur>>();
		
		planning = new HashMap<Integer, List<Creneau>>();
		
		// Ouvrir le fichier CSV
		CSVParser parser = new CSVParser();
		parser.readDispo(Role.Enseignant, enseignants, 8);
		parser.readDispo(Role.Tuteur, tuteurs, 8);
		
		N = 8*5;
		
		for(int periode = 0; periode < N ; periode++) {
			List<Creneau> salles = new ArrayList<Creneau>();
			salles.add(null);
			salles.add(null);
			planning.put(periode, salles);
		}
		
		System.out.println(tuteurs);
		parser.readCSV(enseignants, tuteurs, nbSoutenancesEnseignants, nbSoutenancesTuteurs, relationsEnseignants, relationsTuteurs, N);
		
		nbSoutenancesInitEnseignants = new HashMap<String, Integer>();
		Set<String> ensName = nbSoutenancesEnseignants.keySet();
		for(String s: ensName) {
			nbSoutenancesInitEnseignants.put(s, nbSoutenancesEnseignants.get(s)/2);
		}

		insertData();
		insertData();
		insertData();
		insertData();
	}
	
	public void insertData() {
		boolean inserted = false;
		Acteur a = getActeurMoinsDisponible();
		System.err.println("ACTEUR LE MOINS DISPO " + a);
		List<Acteur> l = new ArrayList<Acteur>(getActeursEnRelation(a));
		System.err.println("ACTEURS EN RELATION " + l);
		loop:
		while(!inserted) {
			Acteur b = getActeurEnRelationLeMoinsDispo(l);
			
			System.err.println("ACTEUR EN RELATION LE MOINS DISPO " + b);
			List<Integer> creneauxCommuns = creneauCommun(a, b);
			System.err.println("LISTE DES CRENEAUX COMMUNS " + creneauxCommuns);
			if(creneauxCommuns.isEmpty()) {
				System.err.println("PAS DE CRENEAUX COMMUNS");
				l.remove(b);
			}
			while(!creneauxCommuns.isEmpty() && !inserted) {
				Creneau c = getCreneau(a, b, creneauxCommuns);
				if(c != null) {
					System.err.println("CRENEAU TROUVE");
					System.err.println("ON INSERE :");
					System.err.println("\t" + c.getA().getRole() + " " + c.getA().getName());
					System.err.println("\t" + c.getB().getRole() + " " + c.getB().getName());
					System.err.println("\t" + c.getC().getRole() + " " + c.getC().getName());
					System.err.println("\tA la période " + c.getP());
					
					inserted = true;
					Acteur ens = getActeur(Role.Enseignant, c.getA(), c.getB(), c.getC());
					Acteur tut = getActeur(Role.Tuteur, c.getA(), c.getB(), c.getC());
					Acteur can = getActeur(Role.Candide, c.getA(), c.getB(), c.getC());
					
					enseignants.get(ens.getName()).put(c.getP(), false);
					int nbSoutEns = nbSoutenancesEnseignants.get(ens.getName())-1;
					nbSoutenancesEnseignants.put(ens.getName(), nbSoutEns);
					int nbSoutEnsInit = nbSoutenancesInitEnseignants.get(ens.getName())-1;
					nbSoutenancesInitEnseignants.put(ens.getName(), nbSoutEnsInit);
					List<Acteur> relEns = relationsEnseignants.get(ens.getName());
					relEns.remove(tut);
					if(nbSoutEns==0) {
						System.err.println(ens + " A FAIT TOUTES LES SOUTENANCES");
					}
					
					tuteurs.get(tut.getName()).put(c.getP(), false);
					int nbSoutTut = nbSoutenancesTuteurs.get(tut.getName())-1;
					nbSoutenancesTuteurs.put(tut.getName(), nbSoutTut);
					List<Acteur> relTut = relationsTuteurs.get(tut.getName());
					relTut.remove(ens);
					if(nbSoutTut==0) {
						System.err.println(tut + " A FAIT TOUTES LES SOUTENANCES");
					}
					
					enseignants.get(can.getName()).put(c.getP(), false);
					int nbSoutCan = nbSoutenancesEnseignants.get(can.getName())-1;
					nbSoutenancesEnseignants.put(can.getName(), nbSoutCan);
					if(nbSoutEns==0) {
						System.err.println(can + " A FAIT TOUTES LES SOUTENANCES");
					}
					
					List<Creneau> salles = planning.get(c.getP());
					if(salles.get(0) == null) {
						System.err.println("\tSalle 1");
						salles.remove(0);
						salles.add(c);
					} else if(salles.get(1) == null) {
						System.err.println("\tSalle 2");
						salles.remove(1);
						salles.add(c);
					}
					
					nbInserted++;
					
				} else {
					System.err.println("ERREUR");
				}
			}
			if(nbInserted==3) {
				break loop;
			}
		}
	}
	
	public Acteur getActeur(Role r, Acteur a, Acteur b, Acteur c) {
		if(a.getRole() == r) {
			return a;
		}
		if(b.getRole() == r) {
			return b;
		}
		if(c.getRole() == r) {
			return c;
		}
		return null;
	}
	
	public boolean salleLibre(int p) {
		List<Creneau> a = planning.get(p);
		for(Creneau c : a) {
			if(c == null) {
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
		
		//System.err.println(dispoA);
		//System.err.println(dispoB);
		
		if(dispoA == null || dispoB == null) {
			System.err.println("IL N'Y A PLUS DE POSSIBILITES");
			return creneauxCommuns;
		}
		
		Set<Integer> periodes = dispoA.keySet();
		for(int p : periodes) {
			//System.err.println(dispoA.get(p) + " " + dispoB.get(p));
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
			if(dispo<acteur.getDispo() && resteDesSoutenancesAFaire(name)) {
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
	
	public boolean resteDesSoutenancesAFaire(String name) {
		return nbSoutenancesInitEnseignants.get(name)>0;
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
			return nbPeriodeLibre-nbSoutenance;
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
}
