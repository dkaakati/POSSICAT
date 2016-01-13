import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Planning {

	int N, E, T, S;
	boolean isFinised = false;
	int nbInserted = 0;
	
	ListActeur enseignants = new ListActeur();
	ListActeur tuteurs = new ListActeur();
	Map<Integer, List<Creneau>> planning;
	

	public Planning() {
		readCSV();
	};

	public void readCSV() {
		
		N = 8*5;
		
		planning = new HashMap<Integer, List<Creneau>>();
		for(int periode = 0; periode < N ; periode++) {
			List<Creneau> salles = new ArrayList<Creneau>();
			salles.add(null);
			salles.add(null);
			planning.put(periode, salles);
		}
		
		CSVParser parser = new CSVParser();
		parser.readDispo(Role.Enseignant, enseignants, 8);
		parser.readDispo(Role.Tuteur, tuteurs, 8);
		//System.err.println(enseignants.list.size() + " enseignants");
		//System.err.println(tuteurs.list.size() + " tuteurs");

		int nbSoutenance = parser.readCSV(enseignants, tuteurs, N);
		//System.err.println(nbSoutenance + " soutenances");
		
		
	

		for(int i = 0; i < nbSoutenance; i++) {
			insertData();
		}
		
	}
	
	public void insertData() {
		boolean inserted = false;
		
		//System.err.println("On récupère l'acteur le moins disponible (enseignant ou tuteur)");
		Acteur act = getActeurLeMoinsDisponible();
		Enseignant e = null;
		Tuteur t = null;
		if(act instanceof Enseignant) {
			e = (Enseignant)act;
		} else {
			t = (Tuteur)act;
		}
		//System.err.println("Acteur le moins disponible : " + act);
		//System.err.println("");
		//System.err.println("On récupère les acteurs en relations les moins disponibles");
		
		ListActeur l = new ListActeur(act.getRelations());
		//System.err.println("Liste des acteurs en relation avec " + act + " => " + l);
		
		while(!inserted) {
			//System.err.println(l);
			act = l.getActeurLeMoinsDisponible();
			if(act instanceof Enseignant) {
				e = (Enseignant)act;
			} else {
				t = (Tuteur)act;
			}
			Creneau c = creneauCommun(e, t);
			if(c==null) {
				l.list.remove(act);
			} else {
				inserted = true;
				System.err.println(nbInserted + "\n-----------------");
				System.err.println("\tEnseignant " + c.getEnseignant());
				System.err.println("\tTuteur " + c.getTuteur());
				System.err.println("\tCandide " + c.getCandide());
				System.err.println("\tA la période " + c.getPeriode());
				
				e.addDisponibilite(c.getPeriode());
				t.addDisponibilite(c.getPeriode());
				c.getCandide().addDisponibiliteCandide(c.getPeriode());
				
				e.removeRelation(t);
				t.removeRelation(e);
				
				if(e.aFaitToutesLesSoutenances()) {
					enseignants.list.remove(e);
				}
				if(t.aFaitToutesLesSoutenances()) {
					tuteurs.list.remove(t);
				}
				if(c.getCandide().aFaitToutesLesSoutenances()) {
					enseignants.list.remove(c.getCandide());
				}
				
				List<Creneau> salles = planning.get(c.getPeriode());
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
			}
		}
	}
	
	public Acteur getActeurLeMoinsDisponible() {
		
		Acteur e = enseignants.getActeurLeMoinsDisponible();
		Acteur t = tuteurs.getActeurLeMoinsDisponible();
		
		if(e.getDisponibilitesSoutenances()<t.getDisponibilitesSoutenances()) {
			return e;
		} else {
			return t;
		}
	}
	
	public Creneau creneauCommun(Enseignant e, Tuteur t) {
		//System.err.println(e + " " + t);
		Map<Integer, Boolean> dispoEnseignant = e.getDisponibilites();
		Map<Integer, Boolean> dispoTuteur = t.getDisponibilites();
		
		List<Integer> creneauxCommuns = new ArrayList<Integer>();
		
		if(dispoEnseignant == null || dispoTuteur == null) {
			return null;
		}
		
		Set<Integer> periodes = dispoEnseignant.keySet();
		for(int p : periodes) {
			if(dispoEnseignant.get(p) && dispoTuteur.get(p)) {
				creneauxCommuns.add(p);
			}
		}
		
		//System.err.println("Les creneaux communs entre " + e + " et " + t + " sont " + creneauxCommuns);
		
		Set<Acteur> listeCandide = new HashSet<Acteur>(enseignants.list);
		listeCandide.remove(e);
		Enseignant c = null;
		
		while(!listeCandide.isEmpty()) {
			for(Acteur act: listeCandide) {
				Enseignant a = (Enseignant)act;
				// On récupère le candide a qui il reste le plus de soutenances a voir
				if(c == null || a.getNbSoutenancesCandide()>c.getNbSoutenancesCandide()) {
					c = a;
				}
			}
			for(int periode : creneauxCommuns) {
				if(c.getDisponibilites().get(periode)) {
					return new Creneau(periode, e, c, t);
				}
			}
			listeCandide.remove(c);
		}

		return null;
	}
}
