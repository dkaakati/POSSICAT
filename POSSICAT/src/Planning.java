import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Planning implements Initializable {
	
	Button button;

	int N, E, T, S;
	boolean isFinised = false;
	int nbInserted = 0;
	int log = 2;
	
	@FXML
	private ListView listSalles;
	
	protected ListProperty<String> listProperty = new SimpleListProperty<>();
	protected List<String> salles = new ArrayList<>();
	
	ListActeur enseignants = new ListActeur();
	ListActeur tuteurs = new ListActeur();
	List<Student> etudiants = new ArrayList<Student>();
	Map<Integer, List<Creneau>> planning;
	
	private String pathDonnees = "";
	private String pathContraintesEns = "";
	private String pathContraintesTut = "";
	
	private Stage stage;
	private Desktop desktop = Desktop.getDesktop();
	
	final FileChooser fileChooser = new FileChooser();
	

	public Planning(Stage primaryStage) throws IOException {
		this.stage = primaryStage;
	};

	public void readCSV() throws IOException {
		ObservableList<String> sallesSelectionnees = listSalles.getSelectionModel().getSelectedItems();
		S = sallesSelectionnees.size();
		
		N = 8*5;
		
		planning = new HashMap<Integer, List<Creneau>>();
		for(int periode = 0; periode < N ; periode++) {
			List<Creneau> salles = new ArrayList<Creneau>();
			planning.put(periode, salles);
		}
		
		CSVParser parser = new CSVParser();
		parser.readDispo(pathContraintesEns, Role.Enseignant, enseignants, 8);
		parser.readDispo(pathContraintesTut, Role.Tuteur, tuteurs, 8);
		if(log==0) {
			System.err.println(enseignants.list.size() + " enseignants");
			System.err.println(tuteurs.list.size() + " tuteurs");
		}

		int nbSoutenance = parser.readCSV(pathDonnees, enseignants, tuteurs, etudiants, N);
		if(log==0) {
			System.err.println(nbSoutenance + " soutenances");
			System.err.println(etudiants);
		}

		for(int i = 0; i < nbSoutenance; i++) {
			insertData();
		}
		
		parser.writeData(planning, sallesSelectionnees);
		desktop.open(new File(System.getProperty("user.dir") + "/data/output.csv"));
		
		
	}
	
	public void insertData() {
		boolean inserted = false;
		
		if(log==0) {
			System.err.println("On récupère l'acteur le moins disponible (enseignant ou tuteur)");
		}
		Acteur act = getActeurLeMoinsDisponible();
		Enseignant e = null;
		Tuteur t = null;
		if(act instanceof Enseignant) {
			e = (Enseignant)act;
		} else {
			t = (Tuteur)act;
		}
		if(log==0) {
			System.err.println("");
			System.err.println("On récupère les acteurs en relations les moins disponibles");
		}
		
		ListActeur l = new ListActeur(act.getRelations());
		if(log==0) {
			System.err.println("Liste des acteurs en relation avec " + act + " => " + l);
		}
		
		while(!inserted) {
			if(l.list.isEmpty()) {
				if(log==0) {
					System.err.println("On génère une exception");
				}
				new Exception("On génère une exception");
			}
			
			act = l.getActeurLeMoinsDisponible();
			if(log==0) {
				System.err.println("On teste avec " + act);
			}
			if(act instanceof Enseignant) {
				e = (Enseignant)act;
			} else {
				t = (Tuteur)act;
			}
			if(log==0) {
				System.err.println("Acteur " + act);
				System.err.println("Tuteur " + t);
			}
			Student s = getStudent(e, t);
			Creneau c = creneauCommun(e, t, s);
			if(log==0) {
				System.err.println(c);
			}
			if(c==null) {
				l.list.remove(act);
			} else {
				inserted = true;
				
				if(log==0 || log==1) {
					System.err.println(nbInserted + "\n-----------------");
					System.err.println("\tEtudiant " + c.getStudent());
					System.err.println("\tEnseignant " + c.getEnseignant());
					System.err.println("\tTuteur " + c.getTuteur());
					System.err.println("\tCandide " + c.getCandide());
					System.err.println("\tA la période " + c.getPeriode());
				}
				
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
				
				insertCreneauInPlanning(c);
				
				nbInserted++;
			}
		}
	}
	
	private void insertCreneauInPlanning(Creneau c) {
		List<Creneau> salles = planning.get(c.getPeriode());
		
		int size = salles.size();
		c.setSalle(size+1);
		salles.add(c);
	}

	private Student getStudent(Enseignant e, Tuteur t) {
		for(Student s : etudiants) {
			if(s.getEnseignant() == e && s.getTuteur() == t) {
				etudiants.remove(s);
				return s;
			}
		}
		return null;
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
	
	public Creneau creneauCommun(Enseignant e, Tuteur t, Student s) {
		if(log==0) {
			System.err.println(e + " " + t);
		}
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
		
		if(log==0) {
			System.err.println("Les creneaux communs entre " + e + " et " + t + " sont " + creneauxCommuns);
		}
		
		if(creneauxCommuns.isEmpty()) {
			return null;
		}
		
		List<Acteur> listeCandide = new ArrayList<Acteur>(enseignants.list);
		listeCandide.remove(e);
		Enseignant c = null;
		
		if(log==0) {
			System.err.println(listeCandide);
		}
		
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
					// Vérifier si une salle est disponible
					System.err.println("SALLES DISPO " + planning.get(periode).size());
					if(planning.get(periode).size()<S) {
						return new Creneau(periode, e, c, t, s);
					}
				}
			}
			listeCandide.remove(c);
			c = null;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		salles.add("i50");
		salles.add("i51");
		salles.add("Jersey");
		salles.add("Genersey");
		listSalles.itemsProperty().bind(listProperty);
		listSalles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listProperty.set(FXCollections.observableArrayList(salles));
	}
	
	public void openJeuDonnees() {
			
		System.err.println(listSalles.getItems());
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.err.println(file.getAbsolutePath());
            pathDonnees = file.getAbsolutePath();
        }
	}
	
	public void openContraintesEns() {
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.err.println(file.getAbsolutePath());
            pathContraintesEns = file.getAbsolutePath();
        }
	}
	
	public void openContraintesTut() {
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.err.println(file.getAbsolutePath());
            pathContraintesTut = file.getAbsolutePath();
        }
	}
	
}
