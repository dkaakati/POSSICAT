/**
 * 
 */

/**
 * @author François Esnault
 * @date 7 janv. 2016
 */
public class Creneau {
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
