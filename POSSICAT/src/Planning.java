import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Planning implements Initializable {

	@FXML
	MenuButton help1;
	@FXML
	MenuButton help2;
	@FXML
	MenuButton help3;

	@FXML
	DatePicker 	dateDebut = new DatePicker(), 
				dateFin = new DatePicker();

	int nbJours, // Nombre de jours ouvrés
		nbPeriodesParJour,
		nbPeriodesEnTout,
		nbSalles; // Nombre de salles disponibles
	boolean isFinised = false;
	int nbInserted = 0;
	int log = 2;

	@FXML
	private ListView<String> listSalles;

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
		
		Date debut = Date.from(dateDebut.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date fin = Date.from(dateFin.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
		
		nbJours = getWorkingDaysBetweenTwoDates(debut, fin)+1;
		nbPeriodesParJour = 8;
		
		ObservableList<String> sallesSelectionnees = listSalles.getSelectionModel().getSelectedItems();
		nbSalles = sallesSelectionnees.size();

		nbPeriodesEnTout = nbPeriodesParJour*nbJours;
		
		planning = new HashMap<Integer, List<Creneau>>();
		for(int periode = 0; periode < nbPeriodesEnTout ; periode++) {
			List<Creneau> salles = new ArrayList<Creneau>();
			planning.put(periode, salles);
		}
		
		CSVParser parser = new CSVParser();
		parser.readDispo(pathContraintesEns, Role.Enseignant, enseignants, nbPeriodesParJour);
		parser.readDispo(pathContraintesTut, Role.Tuteur, tuteurs, nbPeriodesParJour);
		if(log==0) {
			System.err.println(enseignants.list.size() + " enseignants");
			System.err.println(tuteurs.list.size() + " tuteurs");
		}

		int nbSoutenances = parser.readCSV(pathDonnees, enseignants, tuteurs, etudiants, nbPeriodesEnTout);
		if(log==0) {
			System.err.println(nbSoutenances + " soutenances");
			System.err.println(etudiants);
		}

		for(int i = 0; i < nbSoutenances; i++) {
			insertData();
		}
		
		Calendar c = Calendar.getInstance();
		c.set(dateDebut.getValue().getYear(), dateDebut.getValue().getMonthValue(), dateDebut.getValue().getDayOfMonth());
		
		parser.writeData(planning, sallesSelectionnees, c, nbPeriodesParJour);

		desktop.open(new File(System.getProperty("user.home")+"/Downloads/generatedCSV.csv"));

		
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
					if(planning.get(periode).size()<nbSalles) {
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
		/**
		 * Gestion des salles
		 */
		salles.add("i50");
		salles.add("i51");
		salles.add("Jersey");
		salles.add("Guernesey");
		listSalles.itemsProperty().bind(listProperty);
		listSalles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listProperty.set(FXCollections.observableArrayList(salles));
		
		/**
		 * Gestion des dates
		 */
		dateDebut.setValue(LocalDate.now());

		/**
		 * Gestion des tooltips
		 */
		Image imgDonnees = new Image(getClass().getResource("donnees.png").toString());
		ImageView helpDonnees = new ImageView(imgDonnees);
		Image imgContraintesEns = new Image(getClass().getResource("contraintesEns.png").toString());
		ImageView helpContraintesEns = new ImageView(imgContraintesEns);
		Image imgContraintesTut = new Image(getClass().getResource("contraintesTut.png").toString());
		ImageView helpContraintesTut = new ImageView(imgContraintesTut);
		final MenuItem helpPopup1 = new MenuItem();
		final MenuItem helpPopup2 = new MenuItem();
		final MenuItem helpPopup3 = new MenuItem();
		helpPopup1.setGraphic(helpDonnees);
		helpPopup2.setGraphic(helpContraintesEns);
		helpPopup3.setGraphic(helpContraintesTut);
		help1.getItems().setAll(helpPopup1);
		help2.getItems().setAll(helpPopup2);
		help3.getItems().setAll(helpPopup3);

	}
	
	public void validDate() {
		Calendar c = Calendar.getInstance();
		c.set(dateDebut.getValue().getYear(), dateDebut.getValue().getMonthValue(), dateDebut.getValue().getDayOfMonth());
		c.add(Calendar.DATE, 4);
		dateFin.setValue(LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
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
	
	public int getWorkingDaysBetweenTwoDates(Date startDate, Date endDate) {
	    Calendar startCal;
	    Calendar endCal;
	    startCal = Calendar.getInstance();
	    startCal.setTime(startDate);
	    endCal = Calendar.getInstance();
	    endCal.setTime(endDate);
	    int workDays = 0;
	 
	    //Return 0 if start and end are the same
	    if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
	        return 0;
	    }
	 
	    if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
	        startCal.setTime(endDate);
	        endCal.setTime(startDate);
	    }
	 
	    do {
	        startCal.add(Calendar.DAY_OF_MONTH, 1);
	        if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY 
	       && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
	            ++workDays;
	        }
	    } while (startCal.getTimeInMillis() < endCal.getTimeInMillis());
	 
	    return workDays;
	}

}
