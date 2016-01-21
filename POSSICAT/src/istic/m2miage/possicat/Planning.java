package istic.m2miage.possicat;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Planning implements Initializable {

	@FXML
	MenuButton help1;
	@FXML
	MenuButton help2;
	@FXML
	MenuButton help3;
	@FXML
	Text ok1;
	@FXML
	Text nok1;
	@FXML
	Text ok2;
	@FXML
	Text nok2;
	@FXML
	Text ok3;
	@FXML
	Text nok3;
	@FXML
	Group step1;
	@FXML
	Group step2;
	@FXML
	Group step3;
	@FXML
	Group step4;
	@FXML
	Group step5;
	@FXML
	Button generateBtn;


	@FXML
	DatePicker 	dateDebut = new DatePicker(), 
				dateFin = new DatePicker();

	int nbJours, // Nombre de jours ouvrés
		nbPeriodesParJour,
		nbPeriodesEnTout,
		nbSalles; // Nombre de salles disponibles
	boolean isFinised = false;
	int nbInserted = 0;
	int log = 1;
	boolean fastInsert = false;

	@FXML
	private ListView<String> listSalles;

	protected ListProperty<String> listProperty = new SimpleListProperty<>();
	protected List<String> salles = new ArrayList<>();
	
	ListActeur enseignants = new ListActeur();
	ListActeur tuteurs = new ListActeur();
	List<Student> etudiants = new ArrayList<Student>();
	Map<Integer, List<Creneau>> planning;
	List<Creneau> impossibleAInserer;
	
	private String pathDonnees = "/Users/fesnault/POSSICAT/POSSICAT/data/donneesLite2.csv";
	private String pathContraintesEns = "/Users/fesnault/POSSICAT/POSSICAT/data/contraintesEnsLite2.csv";
	private String pathContraintesTut = "/Users/fesnault/POSSICAT/POSSICAT/data/contraintesTuteurLite3.csv";

	private Stage stage;
	private Desktop desktop = Desktop.getDesktop();
	
	final FileChooser fileChooser = new FileChooser();
	

	public Planning(Stage primaryStage) throws IOException {
		this.stage = primaryStage;
	};

	public void readCSV() throws IOException {
		
		if(pathDonnees.isEmpty() || pathContraintesEns.isEmpty() || pathContraintesTut.isEmpty()) {
			return;
		}
		
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
		
		impossibleAInserer = new ArrayList<Creneau>();

		int nbSoutenances = parser.readCSV(pathDonnees, enseignants, tuteurs, etudiants, nbPeriodesEnTout);
		if(log==0) {
			System.err.println(nbSoutenances + " soutenances");
			for(Student s : etudiants) {
				System.err.println(s.getName() + " " + s.getEnseignant() + " " + s.getTuteur());
			}
		}

		for(int i = 0; i < nbSoutenances; i++) {
			insertData();
		}
		
		if(log==0) {
			System.err.println(impossibleAInserer);
		}
		
		Calendar c = Calendar.getInstance();
		c.set(dateDebut.getValue().getYear(), dateDebut.getValue().getMonthValue(), dateDebut.getValue().getDayOfMonth());
		
		parser.writeData(planning, sallesSelectionnees, c, nbPeriodesParJour, impossibleAInserer);

		desktop.open(new File(System.getProperty("user.home")+"/Downloads/generatedCSV.csv"));

		
	}
	
	public void insertData() {
		boolean inserted = false;
		
		
		Acteur act = getActeurLeMoinsDisponible();
		if(log==0) {
			System.err.println("On récupère l'acteur le moins disponible (enseignant ou tuteur)" + act);
		}
		Acteur tmp = act;
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
		
		insertion:
		while(!inserted) {
			if(l.list.isEmpty()) {
				if(log==0) {
					System.err.println("Impossible d'insérer l'acteur. On le place dans une liste complémentaire");
				}
				if(tmp instanceof Enseignant) {
					e = (Enseignant)tmp;
				} else {
					t = (Tuteur)tmp;
				}
				l = new ListActeur(tmp.getRelations());
				if(log==0) {
					System.err.println("Liste " + l);
				}
				act = l.getActeurLeMoinsDisponible();
				
				if(act instanceof Enseignant) {
					e = (Enseignant)act;
				} else {
					t = (Tuteur)act;
				}
				
				if(log==0) {
					System.err.println("Enseignant à supprimer : " + e);
					System.err.println("Tuteur à supprimer : " + t);
				}
				
				e.decNbSoutenance();
				t.decNbSoutenance();
				
				e.removeRelation(t);
				t.removeRelation(e);
				
				if(e.aFaitToutesLesSoutenances()) {
					enseignants.list.remove(e);
				}
				if(t.aFaitToutesLesSoutenances()) {
					tuteurs.list.remove(t);
				}
				
				if(log==0) {
					System.err.println("SOUTENANCES DU TUTEURS " + t.getNbSoutenances());
				}
				
				Student s = getStudent(e, t);
				removeStudent(s);
				if(log==0) {
					System.err.println("Etudiant " + s);
				}
				impossibleAInserer.add(new Creneau(-1, e, null, t, s));
				
				inserted = true;
				break insertion;
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
				removeStudent(s);
				
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
			if(log==0) {
				System.err.println(s.getEnseignant() + " " + e + " ET " + s.getTuteur() + " " + t);
			}
			if(s.getEnseignant() == e && s.getTuteur() == t) {
				return s;
			}
		}
		return null;
	}
	
	private void removeStudent(Student s) {
		etudiants.remove(s);
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

		List<Acteur> listeCandide = new ArrayList<Acteur>(enseignants.list);
		listeCandide.remove(e);
		Map<Acteur, Integer> dispoCandide = new HashMap<Acteur, Integer>();
		for(Acteur candide : listeCandide) {
			Enseignant ens = (Enseignant)candide;
			if(ens.getNbSoutenancesCandide()>0) {
				dispoCandide.put(ens, ens.getNbSoutenancesCandide());
			}
		}
		
		Enseignant c = null;

		if(log==0) {
			System.err.println(listeCandide);
		}
		
		Map<Integer, Integer> creneauxPonderations = new HashMap<Integer, Integer>();

		Set<Integer> dispoEns = dispoEnseignant.keySet();
		for(int p : dispoEns) {
			if(dispoEnseignant.get(p) && dispoTuteur.get(p)) {
				creneauxPonderations.put(p, 10);
			}
		}
		
		Set<Integer> periodes = creneauxPonderations.keySet();
		for(int periode : periodes) {
			int value = creneauxPonderations.get(periode);
			
			int res = periode%8;
			if(res==3 || res==4) {
				creneauxPonderations.put(periode, value-6);
			} else if (res==2 || res==5) {
				creneauxPonderations.put(periode, value-4);
			} else if (res==1 || res==6) {
				creneauxPonderations.put(periode, value-2);
			} else if (res==0 || res==7) {
				creneauxPonderations.put(periode, value+1);
			}
			
			value = creneauxPonderations.get(periode);
			
			if((periode%8)!=0) {
				List<Creneau> avant = planning.get(periode-1);
				for(Creneau creneau : avant) {
					if(creneau.getTuteur() == t) {
						creneauxPonderations.put(periode, value-14);
					}
				}
			} else if((periode%8)!=7) {
				List<Creneau> apres = planning.get(periode+1);
				for(Creneau creneau : apres) {
					if(creneau.getTuteur() == t) {
						creneauxPonderations.put(periode, value-14);
					}
				}
			}
		}
		
		if(log==0) {
			System.err.println("Les creneaux communs entre " + e + " et " + t + " sont " + creneauxPonderations.keySet());
		}
		
		creneauxPonderations = sortByValue(creneauxPonderations);
		dispoCandide = sortByValue(dispoCandide);

		if(log==0) {
			System.err.println("Les creneaux communs entre " + e + " et " + t + " sont " + creneauxPonderations.keySet());
		}
		
		for(int periode : creneauxPonderations.keySet()) {
			for(Acteur act: dispoCandide.keySet()) {
				if(log==0) {
					//System.err.println(act);
				}
				c = (Enseignant)act;
				if(c.getDisponibilites().get(periode)) {
					if(planning.get(periode).size()<nbSalles) {
						return new Creneau(periode, e, c, t, s);
					}
				}
			}
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
		Image imgDonnees = new Image(getClass().getResource("/donnees.png").toString());
		ImageView helpDonnees = new ImageView(imgDonnees);
		Image imgContraintesEns = new Image(getClass().getResource("/contraintesEns.png").toString());
		ImageView helpContraintesEns = new ImageView(imgContraintesEns);
		Image imgContraintesTut = new Image(getClass().getResource("/contraintesTut.png").toString());
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
		
		if(fastInsert) {
			launchStep5();
			launchStep6();
			step3.setDisable(false);
			step4.setDisable(false);
		}
	}
	
	public void validDate() {
		Calendar c = Calendar.getInstance();
		c.set(dateDebut.getValue().getYear(), dateDebut.getValue().getMonthValue(), dateDebut.getValue().getDayOfMonth());
		c.add(Calendar.DATE, 4);
		dateFin.setValue(LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
	}
	
	public void openJeuDonnees() {

		//System.err.println(listSalles.getItems());
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            //System.err.println(file.getAbsolutePath());
            CSVParser parser = new CSVParser();
            int checkData = parser.checkData(file.getAbsolutePath());
            if(checkData > 0) {
            	if(log==0) {
            		System.err.println("OK " + checkData + " insertions");
            	}
                pathDonnees = file.getAbsolutePath();
				FadeTransition ft = new FadeTransition(Duration.millis(1000), ok1);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
				ok1.setText("Succès, "+checkData+" soutenances importées");

				// On passe à l'étape 2
				FadeTransition ft2 = new FadeTransition(Duration.millis(1000), step1);
				ft2.setFromValue(1.0);
				ft2.setToValue(0.5);
				ft2.play();
				step2.setDisable(false);
				FadeTransition ft3 = new FadeTransition(Duration.millis(1000), step2);
				ft3.setFromValue(0.5);
				ft3.setToValue(1.0);
				ft3.play();
            }
            else {
            	if(log==0) {
            		System.err.println("NOK " + checkData);
            	}
            	// If < 0, shows a mistake
				nok1.setText("Échec, fichier non valide");
				FadeTransition ft = new FadeTransition(Duration.millis(1000), nok1);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
				new java.util.Timer().schedule(
						new java.util.TimerTask() {
							@Override
							public void run() {
								FadeTransition ft = new FadeTransition(Duration.millis(1000), nok1);
								ft.setFromValue(1.0);
								ft.setToValue(0.0);
								ft.play();
							}
						},
						3000
				);
            }

        }
	}
	
	public void openContraintesEns() {
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
        	if(log==0) {
        		System.err.println(file.getAbsolutePath());
        	}
            CSVParser parser = new CSVParser();
            int checkData = parser.checkContraintes(file.getAbsolutePath());
            if(checkData < 0) {
            	if(log==0) {
            		System.err.println("OK pour les contraintes enseignants");
            	}
                pathContraintesEns = file.getAbsolutePath();
				FadeTransition ft = new FadeTransition(Duration.millis(1000), ok2);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
				ok2.setText("Succès");

				// On passe à l'étape 3
				FadeTransition ft3 = new FadeTransition(Duration.millis(1000), step2);
				ft3.setFromValue(1.0);
				ft3.setToValue(0.5);
				ft3.play();
				step3.setDisable(false);
				FadeTransition ft4 = new FadeTransition(Duration.millis(1000), step3);
				ft4.setFromValue(0.5);
				ft4.setToValue(1.0);
				ft4.play();

            }
            else {
            	if(log==0) {
            		System.err.println("NOK ligne " + checkData+1);
            	}
            	// If > 0, it means error on this line, we add one because count starts from zero in dev
				FadeTransition ft = new FadeTransition(Duration.millis(1000), nok2);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
				nok2.setText("Échec, erreur à la ligne "+checkData);
				new java.util.Timer().schedule(
						new java.util.TimerTask() {
							@Override
							public void run() {
								FadeTransition ft = new FadeTransition(Duration.millis(1000), nok2);
								ft.setFromValue(1.0);
								ft.setToValue(0.0);
								ft.play();
							}
						},
						3000
				);
            }

        }
	}
	
	public void openContraintesTut() {
		File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
        	if(log==0) {
        		System.err.println(file.getAbsolutePath());
        	}
            CSVParser parser = new CSVParser();
            int checkData = parser.checkContraintes(file.getAbsolutePath());
            if(checkData < 0) {
            	if(log==0) {
            		System.err.println("OK pour les contraintes tuteurs");
            	}
                pathContraintesTut = file.getAbsolutePath();
				FadeTransition ft = new FadeTransition(Duration.millis(1000), ok3);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
				ok3.setText("Succès");

				// On passe à l'étape 4
				FadeTransition ft2 = new FadeTransition(Duration.millis(2000), step3);
				ft2.setFromValue(1.0);
				ft2.setToValue(0.5);
				ft2.play();
				step4.setDisable(false);
				FadeTransition ft3 = new FadeTransition(Duration.millis(2000), step4);
				ft3.setFromValue(0.5);
				ft3.setToValue(1.0);
				ft3.play();
            }
            else {
            	if(log==0) {
            		System.err.println(checkData+1);
            	}
            	// If > 0, it means error on this line, we add one because count starts from zero in dev
				FadeTransition ft = new FadeTransition(Duration.millis(1000), nok3);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
				nok3.setText("Échec, erreur à la ligne "+checkData);
				new java.util.Timer().schedule(
						new java.util.TimerTask() {
							@Override
							public void run() {
								FadeTransition ft = new FadeTransition(Duration.millis(1000), nok3);
								ft.setFromValue(1.0);
								ft.setToValue(0.0);
								ft.play();
							}
						},
						3000
				);
            }
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

	public void launchStep5() {
		step5.setDisable(false);
		step5.setOpacity(1.0);
	}

	public void launchStep6() {
		generateBtn.setDisable(false);
		generateBtn.setOpacity(1.0);
	}
	
	static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
	{
	  Map<K,V> result = new LinkedHashMap<>();
	 Stream <Entry<K,V>> st = map.entrySet().stream();

	 st.sorted(Comparator.comparing(e -> e.getValue()))
	      .forEachOrdered(e ->result.put(e.getKey(),e.getValue()));

	 return result;
	}

}
