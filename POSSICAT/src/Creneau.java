/**
 * 
 */

/**
 * @author François Esnault
 * @date 7 janv. 2016
 */
public class Creneau {
	private Enseignant enseignant;
	private Enseignant candide;
	private Tuteur tuteur;
	private Student student;
	private int periode;
	private int salle;
	
	public Creneau(int periode, Enseignant e, Enseignant c, Tuteur t, Student s) {
		this.periode = periode;
		this.enseignant = e;
		this.tuteur = t;
		this.candide = c;
		this.student = s;
	}
	public Enseignant getEnseignant() {
		return enseignant;
	}
	public void setEnseignant(Enseignant enseignant) {
		this.enseignant = enseignant;
	}
	public Enseignant getCandide() {
		return candide;
	}
	public void setCandide(Enseignant candide) {
		this.candide = candide;
	}
	public Tuteur getTuteur() {
		return tuteur;
	}
	public void setTuteur(Tuteur tuteur) {
		this.tuteur = tuteur;
	}
	public int getPeriode() {
		return periode;
	}
	public void setPeriode(int periode) {
		this.periode = periode;
	}
	public Student getStudent() {
		return student;
	}
	public void setStudent(Student student) {
		this.student = student;
	}
	
	public int getSalle() {
		return salle;
	}
	public void setSalle(int salle) {
		this.salle = salle;
	}
	public String toString() {
		return this.periode + " " + this.enseignant + " " + this.tuteur + " " + this.candide + " " + this.student;
	}
	
}
