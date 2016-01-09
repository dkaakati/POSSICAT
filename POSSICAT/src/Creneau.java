/**
 * 
 */

/**
 * @author François Esnault
 * @date 7 janv. 2016
 */
public class Creneau {
	Enseignant enseignant;
	Enseignant candide;
	Tuteur tuteur;
	int periode;
	public Creneau(int periode, Enseignant e, Enseignant c, Tuteur t) {
		this.periode = periode;
		this.enseignant = e;
		this.tuteur = t;
		this.candide = c;
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
}
